from __future__ import annotations
import json
import logging
import time
from decimal import Decimal
import httpx
from am_oms.core.config import Settings, get_settings
from am_oms.session import MarketStatusView, status_from_payload

log = logging.getLogger("am_oms.clients")


def _pick_price(row: dict, *, last_price_only: bool) -> Decimal | None:
    ohlc = row.get("ohlc") if isinstance(row.get("ohlc"), dict) else {}
    if last_price_only:
        candidates = (row.get("lastPrice"), row.get("last_price"))
    else:
        candidates = (
            row.get("lastPrice"),
            row.get("last_price"),
            row.get("previousClose"),
            ohlc.get("close"),
            row.get("close"),
        )
    for v in candidates:
        if v is None:
            continue
        try:
            if float(v) > 0:
                return Decimal(str(v))
        except (TypeError, ValueError):
            continue
    return None


def _quotes_map(payload: dict) -> dict:
    data = payload.get("data") if isinstance(payload.get("data"), dict) else payload
    quotes = data.get("quotes") if isinstance(data, dict) and isinstance(data.get("quotes"), dict) else data
    return quotes if isinstance(quotes, dict) else {}


def _row_for_symbol(quotes: dict, symbol: str) -> dict | None:
    row = quotes.get(symbol) or quotes.get(symbol.split(":")[-1])
    if isinstance(row, dict):
        return row
    return next(
        (v for k, v in quotes.items() if isinstance(v, dict) and symbol.upper() in str(k).upper()),
        None,
    )


class Clients:
    def __init__(
        self,
        settings: Settings | None = None,
        http: httpx.AsyncClient | None = None,
        matcher_http: httpx.AsyncClient | None = None,
    ):
        self.s = settings or get_settings()
        self.http = http or httpx.AsyncClient(timeout=httpx.Timeout(10.0, connect=3.0))
        self.matcher_http = matcher_http or httpx.AsyncClient(
            timeout=httpx.Timeout(self.s.matcher_http_timeout, connect=1.0)
        )
        self._status_cache: dict[str, tuple[float, MarketStatusView]] = {}

    async def create_journal_portfolio(self, token: str, seed: str) -> tuple[str, str]:
        r = await self.http.post(
            f"{self.s.journal_base_url.rstrip('/')}/v1/portfolios",
            json={"name": "Paper", "currency": "INR", "initialCapital": seed, "kind": "PAPER"},
            headers={"Authorization": f"Bearer {token}", "Content-Type": "application/json"},
        )
        if r.status_code >= 400:
            log.error("journal_portfolio_create_failed", extra={"status": r.status_code})
            raise httpx.HTTPStatusError("journal create failed", request=r.request, response=r)
        body = r.json()
        return body["portfolioId"], body.get("name") or "Paper"

    async def market_status(self, exchange: str = "NSE") -> MarketStatusView:
        ex = (exchange or "NSE").upper()
        now = time.monotonic()
        hit = self._status_cache.get(ex)
        if hit and (now - hit[0]) < self.s.calendar_cache_seconds:
            return hit[1]
        try:
            r = await self.http.get(
                f"{self.s.market_base_url.rstrip('/')}{self.s.market_calendar_status_path}",
                params={"exchange": ex},
                timeout=httpx.Timeout(3.0, connect=1.0),
            )
            r.raise_for_status()
            view = status_from_payload(r.json())
            self._status_cache[ex] = (now, view)
            return view
        except Exception:
            log.warning("calendar_unavailable", extra={"exchange": ex})
            miss = MarketStatusView(open=False, reason="CALENDAR_UNAVAILABLE", available=False)
            self._status_cache[ex] = (now, miss)
            return miss

    async def ltp(self, symbol: str, *, last_price_only: bool = False, refresh: bool = True) -> Decimal | None:
        prices = await self.ltp_many([symbol], last_price_only=last_price_only, refresh=refresh, matcher=False)
        return prices.get(symbol.upper()) or prices.get(symbol)

    async def ltp_many(
        self,
        symbols: list[str],
        *,
        last_price_only: bool = False,
        refresh: bool = False,
        matcher: bool = True,
    ) -> dict[str, Decimal]:
        uniq = []
        seen = set()
        for s in symbols:
            u = s.strip().upper()
            if u and u not in seen:
                seen.add(u)
                uniq.append(u)
        if not uniq:
            return {}
        client = self.matcher_http if matcher else self.http
        joined = ",".join(uniq)
        try:
            r = await client.post(
                f"{self.s.market_base_url.rstrip('/')}{self.s.market_ohlc_path}",
                json={"symbols": joined, "timeFrame": "1D", "refresh": refresh, "indexSymbol": False},
            )
            r.raise_for_status()
            quotes = _quotes_map(r.json())
            out: dict[str, Decimal] = {}
            for sym in uniq:
                row = _row_for_symbol(quotes, sym)
                if not isinstance(row, dict):
                    continue
                px = _pick_price(row, last_price_only=last_price_only)
                if px is not None:
                    out[sym] = px
            return out
        except Exception:
            log.warning("ltp_many_failed", extra={"symbols": len(uniq), "matcher": matcher})
            return {}

    async def publish_fill(self, event: dict) -> None:
        if not self.s.kafka_enabled:
            log.info("kafka_skipped", extra={"orderId": event.get("orderId")})
            return
        from aiokafka import AIOKafkaProducer
        p = AIOKafkaProducer(
            bootstrap_servers=self.s.kafka_bootstrap,
            security_protocol=self.s.kafka_security,
            sasl_mechanism=self.s.kafka_sasl if self.s.kafka_username else None,
            sasl_plain_username=self.s.kafka_username or None,
            sasl_plain_password=self.s.kafka_password or None,
        )
        await p.start()
        try:
            await p.send_and_wait(self.s.fills_topic, json.dumps(event).encode())
        except Exception:
            log.error("fills_publish_failed", extra={"orderId": event.get("orderId")})
        finally:
            await p.stop()
