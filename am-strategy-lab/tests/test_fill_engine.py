from datetime import datetime, timezone

from am_strategy_lab.fill_engine import run_fill_engine
from am_strategy_lab.market_client import fixture_bars
from am_strategy_lab.models import Bar
from am_strategy_lab.report import build_report


def test_zone_scale_fills_layers_on_fixture_path():
    bars = fixture_bars("TEST", "2024-01-01", "2024-01-20")
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
    report = build_report(fills)
    assert report.summary.trades == 3  # entries counted when no exits
    assert "daily" in report.calendar


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
