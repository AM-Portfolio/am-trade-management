"""Paper vs live execution seam. Live broker must not call the paper matcher."""
from __future__ import annotations

from typing import Protocol

from am_oms.session import SessionPhase, market_reject_code


class ExecutionPort(Protocol):
    def session_phase(self) -> SessionPhase: ...


class PaperExecutionEngine:
    """Simulated fills only for Venue.PAPER — phase comes from calendar status."""

    def __init__(self, phase: SessionPhase = SessionPhase.OPEN):
        self._phase = phase

    def session_phase(self) -> SessionPhase:
        return self._phase

    def market_reject_code(self) -> str | None:
        return market_reject_code(self._phase)

    def can_immediate_touch_fill(self) -> bool:
        return self._phase == SessionPhase.OPEN


class LiveBrokerGateway:
    def session_phase(self) -> SessionPhase:
        return SessionPhase.CLOSED

    def reject_reason(self) -> str:
        return "LIVE_NOT_ENABLED"
