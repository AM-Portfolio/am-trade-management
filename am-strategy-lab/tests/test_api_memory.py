import asyncio

import pytest
from httpx import ASGITransport, AsyncClient

# Force memory mode before app import
import os

os.environ["AM_STRATEGY_LAB_MEMORY_DB"] = "true"
os.environ["AM_STRATEGY_LAB_USE_FIXTURE_BARS"] = "true"

from am_strategy_lab.core.config import get_settings

get_settings.cache_clear()

from am_strategy_lab.main import app  # noqa: E402


@pytest.fixture
async def client():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # lifespan
        async with app.router.lifespan_context(app):
            yield ac


@pytest.mark.asyncio
async def test_health_and_zone_backtest(client):
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
        json={"symbol": "RELIANCE", "from": "2024-01-01", "to": "2024-01-20", "timeframe": "1d", "useFixture": True},
    )
    assert bt.status_code == 200, bt.text
    job_id = bt.json()["jobId"]

    # Wait for background task
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
