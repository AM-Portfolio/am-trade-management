from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Any

from pydantic import BaseModel, Field, field_validator


SUPPORTED_ENGINE_IDS = frozenset({"LINE_LEVEL", "ZONE_SCALE", "SUPER_BRACKET"})


class JobStatus(str, Enum):
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    DONE = "DONE"
    FAILED = "FAILED"


class Bar(BaseModel):
    ts: datetime
    open: float
    high: float
    low: float
    close: float
    volume: float = 0.0


class LayerDef(BaseModel):
    sizePct: float
    orderType: str = "LIMIT"
    price: float | None = None
    targetOffsetPct: float | None = None
    stopOffsetPct: float | None = None
    targetPrice: float | None = None
    stopPrice: float | None = None


class StrategyCreate(BaseModel):
    strategyId: str
    version: int = 1
    label: str | None = None
    definition: dict[str, Any]

    @field_validator("strategyId")
    @classmethod
    def non_empty(cls, v: str) -> str:
        if not v.strip():
            raise ValueError("strategyId required")
        return v.strip()


class StrategyDoc(BaseModel):
    strategyId: str
    version: int = 1
    label: str | None = None
    definition: dict[str, Any]
    createdAt: datetime
    updatedAt: datetime


class BacktestCreate(BaseModel):
    symbol: str
    fromDate: str = Field(alias="from", description="yyyy-MM-dd")
    toDate: str = Field(alias="to", description="yyyy-MM-dd")
    timeframe: str = "1d"
    useFixture: bool | None = None

    model_config = {"populate_by_name": True}


class TradeFill(BaseModel):
    ts: datetime
    symbol: str
    side: str
    qty: float
    price: float
    layerIndex: int | None = None
    orderType: str = "LIMIT"
    kind: str = "ENTRY"  # ENTRY | EXIT_TARGET | EXIT_STOP
    pnl: float | None = None


class ReportSummary(BaseModel):
    totalPnl: float
    trades: int
    winRate: float
    avgEntryPrice: float | None = None
    avgEntryVolume: float | None = None


class CalendarBucket(BaseModel):
    date: str
    pnl: float
    trades: int
    winRate: float


class BacktestReport(BaseModel):
    summary: ReportSummary
    calendar: dict[str, list[CalendarBucket]]
    trades: list[TradeFill]
    byTimeframe: list[dict[str, Any]] = Field(default_factory=list)


class BacktestJob(BaseModel):
    jobId: str
    strategyId: str
    status: JobStatus
    symbol: str
    fromDate: str
    toDate: str
    timeframe: str
    error: str | None = None
    createdAt: datetime
    updatedAt: datetime
    report: BacktestReport | None = None
