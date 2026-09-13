"""Resting paper order matcher (LIMIT / STOP / TRAIL) — batched LTP, venue=PAPER only."""
from __future__ import annotations
import asyncio
import logging
import time
from decimal import Decimal
from typing import Any

from am_oms.schemas import OrderSide, OrderStatus, OrderType, Venue
from am_oms.session import SessionPhase, phase_from_status

log = logging.getLogger(__name__)

_tick_lock = asyncio.Lock()


def limit_touchable(side: OrderSide, ltp: Decimal, limit: Decimal) -> bool:
    return ltp <= limit if side == OrderSide.BUY else ltp >= limit


def stop_touchable(side: OrderSide, ltp: Decimal, trigger: Decimal) -> bool:
    return ltp >= trigger if side == OrderSide.BUY else ltp <= trigger


async def match_once(oms: Any) -> int:
    """Fill ACCEPTED PAPER resting orders when LTP crosses. Returns fill count."""
    status = await oms.clients.market_status("NSE")
    phase = phase_from_status(status)
    if phase != SessionPhase.OPEN:
        log.info("matcher_idle phase=%s reason=%s", phase.value, status.reason)
        return 0

    rows = await oms.db.orders.find({
        "status": OrderStatus.ACCEPTED.value,
        "venue": Venue.PAPER.value,
    }).to_list(200)
    if not rows:
        return 0

    symbols = list({d.get("symbol", "").upper() for d in rows if d.get("symbol")})
    t0 = time.perf_counter()
    prices = await oms.clients.ltp_many(symbols, last_price_only=True, refresh=False, matcher=True)
    filled = 0
    for d in rows:
        try:
            sym = (d.get("symbol") or "").upper()
            ltp = prices.get(sym)
            if ltp is None:
                continue
            if await oms.try_match_order(d, ltp=ltp):
                filled += 1
        except Exception:
            log.exception("match failed orderId=%s", d.get("orderId"))
    ms = int((time.perf_counter() - t0) * 1000)
    log.info(
        "matcher_tick symbols=%s orders=%s filled=%s ms=%s",
        len(symbols), len(rows), filled, ms,
    )
    return filled


async def matcher_loop(oms: Any, interval: float = 2.0):
    from am_oms.core.config import get_settings
    budget = get_settings().matcher_tick_budget
    while True:
        try:
            if _tick_lock.locked():
                log.warning("matcher_tick_skipped overlapping")
            else:
                async with _tick_lock:
                    await asyncio.wait_for(match_once(oms), timeout=budget)
        except asyncio.TimeoutError:
            log.warning("matcher_tick_timeout budget=%s", budget)
        except Exception:
            log.exception("matcher tick failed")
        await asyncio.sleep(interval)
