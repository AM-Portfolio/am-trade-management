from datetime import datetime, timedelta, timezone

from am_strategy_lab.fill_engine import run_fill_engine
from am_strategy_lab.models import Bar
from am_strategy_lab.price_align import align_definition_to_bars
from am_strategy_lab.report import build_report


def _dip_bars(start: str, days: int, path: list[float]) -> list[Bar]:
    t0 = datetime.fromisoformat(start).replace(tzinfo=timezone.utc)
    out: list[Bar] = []
    for i in range(days):
        c = path[i % len(path)]
        out.append(
            Bar(
                ts=t0 + timedelta(days=i),
                open=c + 0.5,
                high=max(c + 0.5, c) + 0.8,
                low=min(c + 0.5, c) - 0.8,
                close=c,
                volume=1000 + i,
            )
        )
    return out


def test_zone_scale_fills_layers_on_hand_built_bars():
    bars = _dip_bars("2024-01-01", 20, [102, 100, 98, 96, 94, 92, 90, 91, 93, 95, 97, 99, 101])
    definition = {
        "strategyId": "ZONE_SCALE",
        "side": "BUY",
        "totalQty": 100,
        "zone": {"high": 100.0, "low": 90.0},
        "layers": [
            {"sizePct": 25, "orderType": "LIMIT"},
            {"sizePct": 25, "orderType": "LIMIT"},
            {"sizePct": 50, "orderType": "LIMIT"},
        ],
    }
    fills = run_fill_engine(definition, bars, "TEST")
    entries = [f for f in fills if f.kind == "ENTRY"]
    assert len(entries) == 3
    assert sum(f.qty for f in entries) == 100
    report = build_report(fills, bars_source="market")
    assert report.summary.trades == 3
    assert "daily" in report.calendar
    assert report.barsSource == "market"


def test_align_zone_to_market_bars():
    bars = [
        Bar(
            ts=datetime(2024, 1, 1, tzinfo=timezone.utc),
            open=1300,
            high=1310,
            low=1290,
            close=1305,
            volume=1,
        ),
        Bar(
            ts=datetime(2024, 1, 2, tzinfo=timezone.utc),
            open=1305,
            high=1320,
            low=1295,
            close=1310,
            volume=1,
        ),
    ]
    definition = {
        "strategyId": "ZONE_SCALE",
        "side": "BUY",
        "totalQty": 100,
        "zone": {"high": 100.0, "low": 90.0},
        "layers": [
            {"sizePct": 50, "orderType": "LIMIT"},
            {"sizePct": 50, "orderType": "LIMIT"},
        ],
    }
    aligned = align_definition_to_bars(definition, bars)
    assert aligned["zone"]["high"] > 1200
    assert aligned["zone"]["low"] > 1200
    fills = run_fill_engine(aligned, bars, "RELIANCE")
    assert any(f.kind == "ENTRY" for f in fills)
    assert all(f.price > 1000 for f in fills if f.kind == "ENTRY")


def test_line_level_limit_fill():
    bars = [
        Bar(
            ts=datetime(2024, 1, 1, tzinfo=timezone.utc),
            open=105,
            high=106,
            low=99,
            close=100,
            volume=500,
        )
    ]
    definition = {
        "strategyId": "LINE_LEVEL",
        "orderRule": {"side": "BUY", "qty": 10, "orderType": "LIMIT", "price": 100},
    }
    fills = run_fill_engine(definition, bars, "RELIANCE")
    assert len(fills) == 1
    assert fills[0].price == 100
    assert fills[0].qty == 10


def test_super_bracket_exit_target():
    bars = [
        Bar(ts=datetime(2024, 1, 1, tzinfo=timezone.utc), open=100, high=101, low=99, close=100, volume=1),
        Bar(ts=datetime(2024, 1, 2, tzinfo=timezone.utc), open=100, high=110, low=100, close=108, volume=1),
    ]
    definition = {
        "strategyId": "SUPER_BRACKET",
        "orderRule": {
            "side": "BUY",
            "qty": 5,
            "orderType": "SUPER",
            "price": 100,
            "targetOffsetPct": 5,
            "stopOffsetPct": 2,
        },
    }
    fills = run_fill_engine(definition, bars, "X")
    kinds = [f.kind for f in fills]
    assert "ENTRY" in kinds
    assert "EXIT_TARGET" in kinds
    report = build_report(fills)
    assert report.summary.totalPnl > 0
    assert abs(sum(b.pnl for b in report.calendar["daily"]) - report.summary.totalPnl) < 1e-6
