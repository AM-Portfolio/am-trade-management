"""Session phases from am-market-data calendar status (SoT)."""
from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, time, timezone
from enum import Enum
from zoneinfo import ZoneInfo

IST = ZoneInfo("Asia/Kolkata")


class SessionPhase(str, Enum):
    PREOPEN = "PREOPEN"
    OPEN = "OPEN"
    CLOSED = "CLOSED"


@dataclass(frozen=True)
class MarketStatusView:
    open: bool
    reason: str
    session_start: time | None = None
    session_end: time | None = None
    available: bool = True  # False when calendar HTTP failed


def _parse_hhmm(value) -> time | None:
    if value is None:
        return None
    if isinstance(value, time):
        return value
    s = str(value).strip()
    if not s:
        return None
    # "09:15:00" or "09:15"
    parts = s.split(":")
    try:
        h, m = int(parts[0]), int(parts[1]) if len(parts) > 1 else 0
        sec = int(parts[2]) if len(parts) > 2 else 0
        return time(h, m, sec)
    except (TypeError, ValueError, IndexError):
        return None


def status_from_payload(payload: dict) -> MarketStatusView:
    data = payload.get("data") if isinstance(payload.get("data"), dict) else payload
    if not isinstance(data, dict):
        return MarketStatusView(open=False, reason="UNKNOWN", available=False)
    return MarketStatusView(
        open=bool(data.get("open")),
        reason=str(data.get("reason") or ("OPEN" if data.get("open") else "CLOSED")),
        session_start=_parse_hhmm(data.get("sessionStart") or data.get("session_start")),
        session_end=_parse_hhmm(data.get("sessionEnd") or data.get("session_end")),
        available=True,
    )


def phase_from_status(status: MarketStatusView, now: datetime | None = None) -> SessionPhase:
    """Map market-calendar/status → OMS phase."""
    if not status.available:
        return SessionPhase.CLOSED
    if status.open or status.reason == "OPEN":
        return SessionPhase.OPEN
    if status.reason in ("WEEKEND", "HOLIDAY", "HOLIDAY_FALLBACK"):
        return SessionPhase.CLOSED
    dt = (now or datetime.now(timezone.utc)).astimezone(IST)
    t = dt.time()
    if status.reason == "OUTSIDE_SESSION" and status.session_start and t < status.session_start:
        return SessionPhase.PREOPEN
    return SessionPhase.CLOSED


def market_reject_code(phase: SessionPhase) -> str | None:
    if phase == SessionPhase.OPEN:
        return None
    if phase == SessionPhase.PREOPEN:
        return "MARKET_PREOPEN"
    return "MARKET_CLOSED"
