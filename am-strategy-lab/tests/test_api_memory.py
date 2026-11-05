import asyncio
import os

import pytest
from httpx import ASGITransport, AsyncClient

os.environ["AM_STRATEGY_LAB_MEMORY_DB"] = "true"
os.environ.pop("AM_STRATEGY_LAB_USE_FIXTURE_BARS", None)

from am_strategy_lab.core.config import get_settings

get_settings.cache_clear()

from am_strategy_lab.main import app  # noqa: E402
from am_strategy_lab.market_client import BarsResult, MarketBarsError
from am_strategy_lab.models import Bar
from datetime import datetime, timezone


def _reliance_bars() -> list[Bar]:
    """Realistic post-split RELIANCE-like daily band for tests (not fixture path)."""
    closes = [
        1295.1,
        1305.85,
        1291.65,
        1298.3,
        1303.85,
        1293.7,
        1290.25,
        1325.05,
        1359.9,
        1370.7,
        1394.1,
        1374.6,
        1361.6,
        1367.95,
        1367.45,
    ]
    out: list[Bar] = []
    for i, c in enumerate(closes):
        out.append(
            Bar(
                ts=datetime(2024, 1, 1 + i, tzinfo=timezone.utc),
                open=c - 5,
                high=c + 10,
                low=c - 12,
                close=c,
                volume=1_000_000 + i,
            )
        )
    return out


@pytest.fixture
async def client():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        async with app.router.lifespan_context(app):
            yield ac


@pytest.mark.asyncio
async def test_health_and_zone_backtest(monkeypatch, client):
    import am_strategy_lab.services as services_mod

    async def fake_market(*_a, **_k):
        return BarsResult(bars=_reliance_bars(), source="market", note=None)

    monkeypatch.setattr(services_mod, "fetch_historical", fake_market)

    h = await client.get("/health")
    assert h.status_code == 200
    assert h.json()["service"] == "am-strategy-lab"

    create = await client.post(
        "/v1/strategies",
        json={
            "strategyId": "ZONE_SCALE",
            "label": "test zone",
            "definition": {
                "strategyId": "ZONE_SCALE",
                "side": "BUY",
                "totalQty": 100,
                "zone": {"high": 100, "low": 90},
                "layers": [
                    {"sizePct": 25, "orderType": "LIMIT"},
                    {"sizePct": 25, "orderType": "LIMIT"},
                    {"sizePct": 50, "orderType": "LIMIT"},
                ],
            },
        },
    )
    assert create.status_code == 200, create.text

    bt = await client.post(
        "/v1/strategies/ZONE_SCALE/backtests",
        json={"symbol": "RELIANCE", "from": "2024-01-01", "to": "2024-01-20", "timeframe": "1d"},
    )
    assert bt.status_code == 200, bt.text
    job_id = bt.json()["jobId"]

    for _ in range(50):
        st = await client.get(f"/v1/backtests/{job_id}")
        assert st.status_code == 200
        if st.json()["status"] in ("DONE", "FAILED"):
            break
        await asyncio.sleep(0.05)
    assert st.json()["status"] == "DONE", st.json()

    rep = await client.get(f"/v1/backtests/{job_id}/report")
    assert rep.status_code == 200, rep.text
    body = rep.json()
    assert "summary" in body
    assert "calendar" in body and "daily" in body["calendar"]
    assert "trades" in body
    assert body["summary"]["trades"] >= 1
    assert body.get("barsSource") == "market"
    prices = [t["price"] for t in body["trades"]]
    assert min(prices) >= 1200
    assert max(prices) <= 1500


@pytest.mark.asyncio
async def test_unsupported_strategy_501(client):
    await client.post(
        "/v1/strategies",
        json={"strategyId": "ORB", "definition": {"strategyId": "ORB"}},
    )
    bt = await client.post(
        "/v1/strategies/ORB/backtests",
        json={"symbol": "NIFTY", "from": "2024-01-01", "to": "2024-01-05"},
    )
    assert bt.status_code == 501


@pytest.mark.asyncio
async def test_market_failure_fails_job(monkeypatch, client):
    import am_strategy_lab.services as services_mod

    async def boom(*_a, **_k):
        raise MarketBarsError("market historical HTTP 503 for RELIANCE")

    monkeypatch.setattr(services_mod, "fetch_historical", boom)

    create = await client.post(
        "/v1/strategies",
        json={
            "strategyId": "LINE_LEVEL",
            "definition": {
                "strategyId": "LINE_LEVEL",
                "orderRule": {
                    "side": "BUY",
                    "qty": 1,
                    "orderType": "LIMIT",
                    "price": 1300,
                },
            },
        },
    )
    assert create.status_code == 200, create.text

    bt = await client.post(
        "/v1/strategies/LINE_LEVEL/backtests",
        json={
            "symbol": "RELIANCE",
            "from": "2024-01-01",
            "to": "2024-01-05",
        },
    )
    assert bt.status_code == 200, bt.text
    job_id = bt.json()["jobId"]

    for _ in range(50):
        st = await client.get(f"/v1/backtests/{job_id}")
        assert st.status_code == 200
        if st.json()["status"] in ("DONE", "FAILED"):
            break
        await asyncio.sleep(0.05)
    body = st.json()
    assert body["status"] == "FAILED"
    assert "market" in (body.get("error") or "").lower()
