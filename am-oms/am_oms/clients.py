from __future__ import annotations
import json
import logging
from decimal import Decimal
import httpx
from am_oms.core.config import Settings, get_settings

log = logging.getLogger("am_oms.clients")


class Clients:
    def __init__(self, settings: Settings | None = None, http: httpx.AsyncClient | None = None):
        self.s = settings or get_settings()
        self.http = http or httpx.AsyncClient(timeout=httpx.Timeout(30.0, connect=3.0))

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

    async def ltp(self, symbol: str) -> Decimal | None:
        try:
            r = await self.http.post(
                f"{self.s.market_base_url.rstrip('/')}{self.s.market_ohlc_path}",
                json={"symbols": symbol, "timeFrame": "1D", "refresh": True, "indexSymbol": False},
            )
            r.raise_for_status()
            payload = r.json()
            data = payload.get("data") if isinstance(payload.get("data"), dict) else payload
            quotes = data.get("quotes") if isinstance(data, dict) and isinstance(data.get("quotes"), dict) else data
            if not isinstance(quotes, dict):
                return None
            row = quotes.get(symbol) or quotes.get(symbol.split(":")[-1])
            if not isinstance(row, dict):
                row = next((v for k, v in quotes.items() if isinstance(v, dict) and symbol.upper() in str(k).upper()), None)
            if not isinstance(row, dict):
                return None
            ohlc = row.get("ohlc") if isinstance(row.get("ohlc"), dict) else {}
            for v in (row.get("lastPrice"), row.get("last_price"), row.get("previousClose"), ohlc.get("close"), row.get("close")):
                if v is None:
                    continue
                try:
                    if float(v) > 0:
                        return Decimal(str(v))
                except (TypeError, ValueError):
                    continue
            return None
        except Exception:
            log.warning("ltp_timeout", extra={"symbol": symbol})
            return None

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
