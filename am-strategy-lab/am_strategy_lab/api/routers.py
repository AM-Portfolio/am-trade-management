from __future__ import annotations

from fastapi import APIRouter, BackgroundTasks, HTTPException, Request

from am_strategy_lab.fill_engine import UnsupportedStrategyError
from am_strategy_lab.models import BacktestCreate, StrategyCreate
from am_strategy_lab.services import StrategyLabService

strategies = APIRouter(prefix="/v1/strategies", tags=["strategies"])
backtests = APIRouter(prefix="/v1/backtests", tags=["backtests"])


def _svc(request: Request) -> StrategyLabService:
    return request.app.state.lab


@strategies.post("")
async def create_strategy(body: StrategyCreate, request: Request):
    doc = await _svc(request).upsert_strategy(body)
    return doc.model_dump(mode="json")


@strategies.get("")
async def list_strategies(request: Request):
    rows = await _svc(request).list_strategies()
    return [r.model_dump(mode="json") for r in rows]


@strategies.get("/{strategy_id}")
async def get_strategy(strategy_id: str, request: Request):
    doc = await _svc(request).get_strategy(strategy_id)
    if not doc:
        raise HTTPException(status_code=404, detail="strategy not found")
    return doc.model_dump(mode="json")


@strategies.post("/{strategy_id}/backtests")
async def start_backtest(
    strategy_id: str,
    body: BacktestCreate,
    request: Request,
    background: BackgroundTasks,
):
    try:
        job = await _svc(request).create_backtest(strategy_id, body)
    except KeyError as e:
        raise HTTPException(status_code=404, detail=str(e)) from e
    except UnsupportedStrategyError as e:
        raise HTTPException(status_code=501, detail=str(e)) from e

    background.add_task(_svc(request).run_backtest_job, job.jobId)
    return job.model_dump(mode="json")


@backtests.get("/{job_id}")
async def get_backtest(job_id: str, request: Request):
    job = await _svc(request).get_backtest(job_id)
    if not job:
        raise HTTPException(status_code=404, detail="backtest not found")
    return job.model_dump(mode="json")


@backtests.get("/{job_id}/report")
async def get_report(job_id: str, request: Request):
    job = await _svc(request).get_backtest(job_id)
    if not job:
        raise HTTPException(status_code=404, detail="backtest not found")
    if job.status.value == "FAILED":
        raise HTTPException(status_code=400, detail=job.error or "backtest failed")
    if job.status.value != "DONE" or job.report is None:
        raise HTTPException(status_code=409, detail=f"backtest not ready: {job.status.value}")
    return job.report.model_dump(mode="json")
