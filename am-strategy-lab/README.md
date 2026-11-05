# am-strategy-lab

Strategy store + FillEngine backtests + calendar reports (MVP).

## Engines (v1)

- `LINE_LEVEL` — single LIMIT/SUPER
- `ZONE_SCALE` — N-layer scale-in (sizePct sum 100)
- `SUPER_BRACKET` — SUPER with target/stop offsets

Other catalog ids can be stored; backtest returns **501**.

## APIs

- `GET /health`, `/health/ready`, `/v1/health`
- `POST /v1/strategies` — upsert strategy JSON
- `GET /v1/strategies`, `GET /v1/strategies/{id}`
- `POST /v1/strategies/{id}/backtests` — `{ symbol, from, to, timeframe, useFixture? }`
- `GET /v1/backtests/{jobId}`
- `GET /v1/backtests/{jobId}/report`

## Local

```bash
pip install -r requirements.txt
set AM_STRATEGY_LAB_MEMORY_DB=true
set AM_STRATEGY_LAB_USE_FIXTURE_BARS=true
uvicorn am_strategy_lab.main:app --reload --port 8090
pytest -q
```

Volume missing from market → treated as `0`; market fetch failure → fixture bars fallback.
