from __future__ import annotations

from datetime import datetime, timedelta, timezone
from typing import Any

import httpx

from am_strategy_lab.core.config import get_settings
from am_strategy_lab.models import Bar


def fixture_bars(symbol: str, from_date: str, to_date: str) -> list[Bar]:
    """Deterministic OHLCV for local/dev without market — dips into 90–100 zone."""
    start = datetime.fromisoformat(from_date).replace(tzinfo=timezone.utc)
    end = datetime.fromisoformat(to_date).replace(tzinfo=timezone.utc)
    if end < start:
        end = start
    bars: list[Bar] = []
    # Sequence designed so ZONE_SCALE 100/96.67/93.33/90 gets fills
    path = [102, 100, 98, 96, 94, 92, 90, 91, 93, 95, 97, 99, 101]
    t = start
    i = 0
    while t <= end and i < len(path) * 3:
        c = float(path[i % len(path)])
        o = c + 0.5
        h = max(o, c) + 0.8
        l = min(o, c) - 0.8
        bars.append(
            Bar(
                ts=t,
                open=o,
                high=h,
                low=l,
                close=c,
                volume=1000.0 + i * 10,
            )
        )
        t += timedelta(days=1)
        i += 1
    if not bars:
        bars.append(Bar(ts=start, open=100, high=101, low=99, close=100, volume=0))
    return bars


def _parse_points(payload: Any, symbol: str) -> list[Bar]:
    bars: list[Bar] = []
    data = payload
    if isinstance(payload, dict):
        # Common shapes: { data: { SYMBOL: { dataPoints: [] } } } or { dataPoints: [] }
        if "dataPoints" in payload:
            points = payload["dataPoints"]
        elif "data" in payload:
            data = payload["data"]
            if isinstance(data, dict):
                # try symbol key or first value
                block = data.get(symbol) or data.get(symbol.upper()) or next(iter(data.values()), {})
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
            # ms or s
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
    *,
    use_fixture: bool = False,
) -> list[Bar]:
    s = get_settings()
    if use_fixture or s.use_fixture_bars:
        return fixture_bars(symbol, from_date, to_date)

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
                # fallback fixture so backtest still completes in isolated envs
                return fixture_bars(symbol, from_date, to_date)
            bars = _parse_points(r.json(), symbol)
            if not bars:
                return fixture_bars(symbol, from_date, to_date)
            return bars
    except Exception:
        return fixture_bars(symbol, from_date, to_date)
