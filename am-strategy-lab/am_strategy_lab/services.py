from __future__ import annotations

import uuid
from datetime import datetime, timezone
from typing import Any

from am_strategy_lab.fill_engine import UnsupportedStrategyError, run_fill_engine
from am_strategy_lab.market_client import fetch_historical
from am_strategy_lab.models import (
    SUPPORTED_ENGINE_IDS,
    BacktestCreate,
    BacktestJob,
    JobStatus,
    StrategyCreate,
    StrategyDoc,
)
from am_strategy_lab.report import build_report


def _now() -> datetime:
    return datetime.now(timezone.utc)


class StrategyLabService:
    def __init__(self, db: Any):
        self.db = db

    async def upsert_strategy(self, body: StrategyCreate) -> StrategyDoc:
        now = _now()
        existing = await self.db.strategies.find_one({"strategyId": body.strategyId})
        doc = {
            "strategyId": body.strategyId,
            "version": body.version,
            "label": body.label,
            "definition": {**body.definition, "strategyId": body.strategyId},
            "updatedAt": now,
        }
        if existing:
            doc["createdAt"] = existing.get("createdAt", now)
            await self.db.strategies.update_one({"strategyId": body.strategyId}, {"$set": doc})
        else:
            doc["createdAt"] = now
            await self.db.strategies.insert_one(doc)
        return StrategyDoc(**doc)

    async def get_strategy(self, strategy_id: str) -> StrategyDoc | None:
        row = await self.db.strategies.find_one({"strategyId": strategy_id}, {"_id": 0})
        return StrategyDoc(**row) if row else None

    async def list_strategies(self) -> list[StrategyDoc]:
        cur = self.db.strategies.find({}, {"_id": 0})
        return [StrategyDoc(**r) async for r in cur]

    async def create_backtest(self, strategy_id: str, body: BacktestCreate) -> BacktestJob:
        strat = await self.get_strategy(strategy_id)
        if not strat:
            raise KeyError(f"strategy not found: {strategy_id}")
        sid = str(strat.definition.get("strategyId") or strategy_id).upper()
        if sid not in SUPPORTED_ENGINE_IDS:
            raise UnsupportedStrategyError(
                f"strategyId={sid} stored but engine unsupported; use LINE_LEVEL|ZONE_SCALE|SUPER_BRACKET"
            )

        job_id = str(uuid.uuid4())
        now = _now()
        job = {
            "jobId": job_id,
            "strategyId": strategy_id,
            "status": JobStatus.PENDING.value,
            "symbol": body.symbol,
            "fromDate": body.fromDate,
            "toDate": body.toDate,
            "timeframe": body.timeframe,
            "error": None,
            "createdAt": now,
            "updatedAt": now,
            "report": None,
            "useFixture": body.useFixture,
        }
        await self.db.backtests.insert_one(job)
        return BacktestJob(**{k: v for k, v in job.items() if k != "useFixture"})

    async def run_backtest_job(self, job_id: str) -> None:
        row = await self.db.backtests.find_one({"jobId": job_id})
        if not row:
            return
        await self.db.backtests.update_one(
            {"jobId": job_id},
            {"$set": {"status": JobStatus.RUNNING.value, "updatedAt": _now()}},
        )
        try:
            strat = await self.get_strategy(row["strategyId"])
            if not strat:
                raise KeyError("strategy missing")
            use_fixture = bool(row.get("useFixture"))
            bars = await fetch_historical(
                row["symbol"],
                row["fromDate"],
                row["toDate"],
                row.get("timeframe") or "1d",
                use_fixture=use_fixture,
            )
            fills = run_fill_engine(strat.definition, bars, row["symbol"])
            report = build_report(fills)
            await self.db.backtests.update_one(
                {"jobId": job_id},
                {
                    "$set": {
                        "status": JobStatus.DONE.value,
                        "report": report.model_dump(mode="json"),
                        "updatedAt": _now(),
                        "error": None,
                    }
                },
            )
        except Exception as e:
            await self.db.backtests.update_one(
                {"jobId": job_id},
                {
                    "$set": {
                        "status": JobStatus.FAILED.value,
                        "error": str(e),
                        "updatedAt": _now(),
                    }
                },
            )

    async def get_backtest(self, job_id: str) -> BacktestJob | None:
        row = await self.db.backtests.find_one({"jobId": job_id}, {"_id": 0, "useFixture": 0})
        if not row:
            return None
        return BacktestJob(**row)
