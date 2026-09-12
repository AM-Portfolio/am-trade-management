"""Resting paper order matcher (LIMIT / STOP / TRAIL)."""
from __future__ import annotations
import asyncio
import logging
from decimal import Decimal
from typing import Any

from am_oms.schemas import OrderSide, OrderStatus, OrderType

log = logging.getLogger(__name__)


def limit_touchable(side: OrderSide, ltp: Decimal, limit: Decimal) -> bool:
    return ltp <= limit if side == OrderSide.BUY else ltp >= limit


def stop_touchable(side: OrderSide, ltp: Decimal, trigger: Decimal) -> bool:
    return ltp >= trigger if side == OrderSide.BUY else ltp <= trigger


async def match_once(oms: Any) -> int:
    """Fill ACCEPTED resting orders when LTP crosses. Returns fill count."""
    filled = 0
    rows = await oms.db.orders.find({"status": OrderStatus.ACCEPTED.value}).to_list(200)
    for d in rows:
        try:
            if await oms.try_match_order(d):
                filled += 1
        except Exception:
            log.exception("match failed orderId=%s", d.get("orderId"))
    return filled


async def matcher_loop(oms: Any, interval: float = 2.0):
    while True:
        try:
            await match_once(oms)
        except Exception:
            log.exception("matcher tick failed")
        await asyncio.sleep(interval)
