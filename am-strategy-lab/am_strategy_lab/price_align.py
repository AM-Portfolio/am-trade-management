"""Align catalog toy price levels to live bar ranges so market backtests fill."""

from __future__ import annotations

import copy
from statistics import median
from typing import Any

from am_strategy_lab.models import Bar


def _bar_mid(bars: list[Bar]) -> float:
    closes = [b.close for b in bars if b.close > 0]
    if not closes:
        return 0.0
    return float(median(closes))


def _zone_outside(high: float, low: float, bars: list[Bar]) -> bool:
    bar_hi = max(b.high for b in bars)
    bar_lo = min(b.low for b in bars)
    return high < bar_lo or low > bar_hi


def align_definition_to_bars(definition: dict[str, Any], bars: list[Bar]) -> dict[str, Any]:
    """If absolute prices sit outside the bar range, place levels inside bars.

    Catalog templates often ship with toy zones (90–100). For market runs we
    remap into the observed OHLC band so ZONE_SCALE / LINE can still fill.
    """
    if not bars:
        return definition

    bar_mid = _bar_mid(bars)
    if bar_mid <= 0:
        return definition

    bar_hi = max(b.high for b in bars)
    bar_lo = min(b.low for b in bars)
    span = bar_hi - bar_lo
    if span <= 0:
        span = bar_mid * 0.02
        bar_hi = bar_mid + span / 2
        bar_lo = bar_mid - span / 2

    out = copy.deepcopy(definition)
    changed = False

    zone = out.get("zone")
    if isinstance(zone, dict) and zone.get("high") is not None and zone.get("low") is not None:
        high = float(zone["high"])
        low = float(zone["low"])
        if _zone_outside(high, low, bars):
            # Sit inside the observed range so layer limits get touched.
            out["zone"] = {
                "high": bar_lo + span * 0.85,
                "low": bar_lo + span * 0.15,
            }
            changed = True

    rule = out.get("orderRule")
    if isinstance(rule, dict) and rule.get("price") is not None:
        px = float(rule["price"])
        if px < bar_lo or px > bar_hi:
            out["orderRule"] = {**rule, "price": bar_mid}
            changed = True

    if isinstance(out.get("layers"), list):
        for layer in out["layers"]:
            if not isinstance(layer, dict):
                continue
            if layer.get("price") is not None:
                px = float(layer["price"])
                if px < bar_lo or px > bar_hi:
                    layer["price"] = bar_mid
                    changed = True

    return out if changed else definition
