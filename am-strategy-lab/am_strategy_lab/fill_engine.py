"""Simulate LIMIT / SUPER fills for LINE_LEVEL and ZONE_SCALE strategies."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any

from am_strategy_lab.models import Bar, TradeFill


class UnsupportedStrategyError(ValueError):
    pass


@dataclass
class _Working:
    side: str
    qty: float
    price: float
    order_type: str
    layer_index: int | None
    target: float | None = None
    stop: float | None = None
    filled: bool = False
    closed: bool = False
    entry_ts: datetime | None = None
    entry_price: float | None = None


def _layer_prices(side: str, high: float, low: float, n: int) -> list[float]:
    if n <= 0:
        return []
    if n == 1:
        return [(high + low) / 2.0]
    # BUY: layer0 near high, last at low; SELL mirrored
    out: list[float] = []
    for i in range(n):
        t = i / (n - 1)
        if side.upper() == "BUY":
            out.append(high + (low - high) * t)
        else:
            out.append(low + (high - low) * t)
    return out


def _qty_split(total: float, pcts: list[float]) -> list[float]:
    raw = [total * p / 100.0 for p in pcts]
    ints = [int(x) for x in raw]
    rem = int(total) - sum(ints)
    if rem != 0 and ints:
        ints[-1] += rem
    # keep fractional if total not int
    if abs(total - int(total)) > 1e-9:
        return raw
    return [float(x) for x in ints]


def _touch_fills(side: str, bar: Bar, limit: float) -> bool:
    if side.upper() == "BUY":
        return bar.low <= limit <= bar.high
    return bar.low <= limit <= bar.high


def _super_targets(entry: float, side: str, layer: dict[str, Any]) -> tuple[float | None, float | None]:
    if layer.get("targetPrice") is not None:
        tgt = float(layer["targetPrice"])
    elif layer.get("targetOffsetPct") is not None:
        off = float(layer["targetOffsetPct"]) / 100.0
        tgt = entry * (1 + off) if side.upper() == "BUY" else entry * (1 - off)
    else:
        tgt = None
    if layer.get("stopPrice") is not None:
        stp = float(layer["stopPrice"])
    elif layer.get("stopOffsetPct") is not None:
        off = float(layer["stopOffsetPct"]) / 100.0
        stp = entry * (1 - off) if side.upper() == "BUY" else entry * (1 + off)
    else:
        stp = None
    return tgt, stp


def _exit_hit(side: str, bar: Bar, target: float | None, stop: float | None) -> tuple[str | None, float | None]:
    if side.upper() == "BUY":
        if stop is not None and bar.low <= stop:
            return "EXIT_STOP", stop
        if target is not None and bar.high >= target:
            return "EXIT_TARGET", target
    else:
        if stop is not None and bar.high >= stop:
            return "EXIT_STOP", stop
        if target is not None and bar.low <= target:
            return "EXIT_TARGET", target
    return None, None


def run_fill_engine(definition: dict[str, Any], bars: list[Bar], symbol: str) -> list[TradeFill]:
    sid = str(definition.get("strategyId") or definition.get("type") or "").upper()
    if sid not in {"LINE_LEVEL", "ZONE_SCALE", "SUPER_BRACKET"}:
        raise UnsupportedStrategyError(f"engine does not support strategyId={sid}")

    if sid == "ZONE_SCALE":
        return _run_zone(definition, bars, symbol)
    return _run_line(definition, bars, symbol)


def _run_line(definition: dict[str, Any], bars: list[Bar], symbol: str) -> list[TradeFill]:
    rule = definition.get("orderRule") or {}
    side = str(rule.get("side") or definition.get("side") or "BUY").upper()
    qty = float(rule.get("qty") or definition.get("totalQty") or 1)
    order_type = str(rule.get("orderType") or "LIMIT").upper()
    price = rule.get("price")
    if price is None:
        zone = definition.get("zone") or {}
        price = zone.get("high") if side == "BUY" else zone.get("low")
    if price is None and bars:
        price = bars[0].close
    price = float(price)
    layer = {
        "targetOffsetPct": rule.get("targetOffsetPct"),
        "stopOffsetPct": rule.get("stopOffsetPct"),
        "targetPrice": rule.get("targetPrice"),
        "stopPrice": rule.get("stopPrice"),
    }
    working = [
        _Working(
            side=side,
            qty=qty,
            price=price,
            order_type=order_type,
            layer_index=0,
        )
    ]
    if order_type == "SUPER":
        tgt, stp = _super_targets(price, side, layer)
        working[0].target = tgt
        working[0].stop = stp
    return _simulate(working, bars, symbol)


def _run_zone(definition: dict[str, Any], bars: list[Bar], symbol: str) -> list[TradeFill]:
    side = str(definition.get("side") or "BUY").upper()
    total_qty = float(definition.get("totalQty") or 100)
    zone = definition.get("zone") or {}
    high = float(zone.get("high") if zone.get("high") is not None else 100)
    low = float(zone.get("low") if zone.get("low") is not None else 90)
    layers = definition.get("layers") or []
    if len(layers) < 2:
        raise ValueError("ZONE_SCALE requires at least 2 layers")
    pcts = [float(l.get("sizePct") or 0) for l in layers]
    if abs(sum(pcts) - 100.0) > 0.01:
        raise ValueError(f"layer sizePct must sum to 100, got {sum(pcts)}")
    prices = []
    for i, layer in enumerate(layers):
        if layer.get("price") is not None:
            prices.append(float(layer["price"]))
        else:
            prices = _layer_prices(side, high, low, len(layers))
            break
    if len(prices) != len(layers):
        prices = _layer_prices(side, high, low, len(layers))
    qtys = _qty_split(total_qty, pcts)
    working: list[_Working] = []
    for i, layer in enumerate(layers):
        ot = str(layer.get("orderType") or "LIMIT").upper()
        w = _Working(
            side=side,
            qty=qtys[i],
            price=prices[i],
            order_type=ot,
            layer_index=i,
        )
        if ot == "SUPER":
            tgt, stp = _super_targets(prices[i], side, layer)
            w.target = tgt
            w.stop = stp
        working.append(w)
    return _simulate(working, bars, symbol)


def _simulate(working: list[_Working], bars: list[Bar], symbol: str) -> list[TradeFill]:
    fills: list[TradeFill] = []
    for bar in bars:
        for w in working:
            if w.closed:
                continue
            if not w.filled:
                if w.order_type in ("LIMIT", "SUPER", "STOP") and _touch_fills(w.side, bar, w.price):
                    w.filled = True
                    w.entry_ts = bar.ts
                    w.entry_price = w.price
                    if w.order_type == "SUPER" and (w.target is None and w.stop is None):
                        # recompute from entry if offsets stored via price only
                        pass
                    fills.append(
                        TradeFill(
                            ts=bar.ts,
                            symbol=symbol,
                            side=w.side,
                            qty=w.qty,
                            price=w.price,
                            layerIndex=w.layer_index,
                            orderType=w.order_type,
                            kind="ENTRY",
                        )
                    )
                    if w.order_type != "SUPER":
                        w.closed = True
                continue
            # manage SUPER exits
            if w.order_type == "SUPER" and w.entry_price is not None:
                kind, px = _exit_hit(w.side, bar, w.target, w.stop)
                if kind and px is not None:
                    pnl = (px - w.entry_price) * w.qty if w.side == "BUY" else (w.entry_price - px) * w.qty
                    fills.append(
                        TradeFill(
                            ts=bar.ts,
                            symbol=symbol,
                            side="SELL" if w.side == "BUY" else "BUY",
                            qty=w.qty,
                            price=px,
                            layerIndex=w.layer_index,
                            orderType=w.order_type,
                            kind=kind,
                            pnl=pnl,
                        )
                    )
                    w.closed = True
    return fills
