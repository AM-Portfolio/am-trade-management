from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any, Literal

import httpx

from am_strategy_lab.core.config import get_settings
from am_strategy_lab.models import Bar


class MarketBarsError(RuntimeError):
    """Market historical unavailable — no synthetic fallback."""


@dataclass(frozen=True)
class BarsResult:
    bars: list[Bar]
    source: Literal["market"] = "market"
    note: str | None = None


def _resolve_block(data: dict[str, Any], symbol: str) -> Any:
    sym = symbol.strip()
    upper = sym.upper()
    candidates = [
        sym,
        upper,
        f"NSE_EQ:{upper}",
        f"NSE:{upper}",
    ]
    for key in candidates:
        if key in data:
            return data[key]
    # Case-insensitive / suffix match (e.g. NSE_EQ:RELIANCE)
    for key, val in data.items():
        if str(key).upper().endswith(":" + upper) or str(key).upper() == upper:
            return val
    return next(iter(data.values()), {})


def _parse_points(payload: Any, symbol: str) -> list[Bar]:
    bars: list[Bar] = []
    if isinstance(payload, dict):
        if "dataPoints" in payload:
            points = payload["dataPoints"]
        elif "data" in payload:
            data = payload["data"]
            if isinstance(data, dict):
                block = _resolve_block(data, symbol)
                points = block.get("dataPoints") if isinstance(block, dict) else []
            elif isinstance(data, list):
                points = data
            else:
                points = []
        else:
            points = []
    elif isinstance(payload, list):
        points = payload
    else:
        points = []

    for p in points or []:
        if not isinstance(p, dict):
            continue
        ts_raw = p.get("timestamp") or p.get("time") or p.get("date") or p.get("ts")
        if ts_raw is None:
            continue
        if isinstance(ts_raw, (int, float)):
            ts = datetime.fromtimestamp(ts_raw / 1000.0 if ts_raw > 1e12 else ts_raw, tz=timezone.utc)
        else:
            ts = datetime.fromisoformat(str(ts_raw).replace("Z", "+00:00"))
        vol = p.get("volume")
        if vol is None:
            vol = 0
        bars.append(
            Bar(
                ts=ts,
                open=float(p.get("open") or p.get("o") or 0),
                high=float(p.get("high") or p.get("h") or 0),
                low=float(p.get("low") or p.get("l") or 0),
                close=float(p.get("close") or p.get("c") or 0),
                volume=float(vol),
            )
        )
    bars.sort(key=lambda b: b.ts)
    return bars


_TF_MAP = {
    "1m": "MINUTE",
    "5m": "MINUTE",
    "15m": "MINUTE",
    "1h": "HOUR",
    "1d": "DAY",
    "d": "DAY",
    "day": "DAY",
}


async def fetch_historical(
    symbol: str,
    from_date: str,
    to_date: str,
    timeframe: str = "1d",
) -> BarsResult:
    """Load real market bars only. Never invent OHLC."""
    s = get_settings()
    interval = _TF_MAP.get(timeframe.lower(), timeframe.upper())
    url = s.market_base_url.rstrip("/") + s.market_historical_path
    body = {
        "symbols": symbol,
        "from": from_date,
        "to": to_date,
        "interval": interval,
    }
    try:
        async with httpx.AsyncClient(timeout=s.market_http_timeout) as client:
            r = await client.post(url, json=body)
            if r.status_code >= 400:
                raise MarketBarsError(
                    f"market historical HTTP {r.status_code} for {symbol}; "
                    "check AM_MARKET_BASE_URL and date range"
                )
            bars = _parse_points(r.json(), symbol)
            if not bars:
                raise MarketBarsError(
                    f"market historical returned no bars for {symbol}; "
                    "widen or shift the date range"
                )
            return BarsResult(bars=bars, source="market", note=None)
    except MarketBarsError:
        raise
    except Exception as e:
        raise MarketBarsError(
            f"market historical failed for {symbol}: {e}; "
            "check AM_MARKET_BASE_URL"
        ) from e
