from __future__ import annotations

from collections import defaultdict

from am_strategy_lab.models import BacktestReport, CalendarBucket, ReportSummary, TradeFill


def build_report(fills: list[TradeFill]) -> BacktestReport:
    exits = [f for f in fills if f.kind.startswith("EXIT") and f.pnl is not None]
    entries = [f for f in fills if f.kind == "ENTRY"]
    # If only entries (LIMIT fills without exit), treat as open — PnL 0 for calendar
    closed = exits
    total_pnl = sum(f.pnl or 0.0 for f in closed)
    wins = sum(1 for f in closed if (f.pnl or 0) > 0)
    n = len(closed)
    win_rate = (wins / n) if n else 0.0
    avg_entry = (sum(f.price for f in entries) / len(entries)) if entries else None

    by_day: dict[str, list[TradeFill]] = defaultdict(list)
    for f in closed:
        by_day[f.ts.date().isoformat()].append(f)

    daily: list[CalendarBucket] = []
    for day in sorted(by_day.keys()):
        day_fills = by_day[day]
        pnl = sum(x.pnl or 0 for x in day_fills)
        tw = sum(1 for x in day_fills if (x.pnl or 0) > 0)
        daily.append(
            CalendarBucket(
                date=day,
                pnl=pnl,
                trades=len(day_fills),
                winRate=(tw / len(day_fills)) if day_fills else 0.0,
            )
        )

    # weekly / monthly coarse buckets
    weekly: dict[str, list[float]] = defaultdict(list)
    monthly: dict[str, list[float]] = defaultdict(list)
    for b in daily:
        y, m, d = b.date.split("-")
        monthly[f"{y}-{m}"].append(b.pnl)
        # ISO-ish week key: year-week from date string via datetime
        from datetime import date

        dt = date.fromisoformat(b.date)
        iso = dt.isocalendar()
        weekly[f"{iso.year}-W{iso.week:02d}"].append(b.pnl)

    def _agg(m: dict[str, list[float]]) -> list[CalendarBucket]:
        out = []
        for k in sorted(m.keys()):
            vals = m[k]
            out.append(
                CalendarBucket(
                    date=k,
                    pnl=sum(vals),
                    trades=len(vals),
                    winRate=sum(1 for v in vals if v > 0) / len(vals) if vals else 0.0,
                )
            )
        return out

    return BacktestReport(
        summary=ReportSummary(
            totalPnl=total_pnl,
            trades=n if n else len(entries),
            winRate=win_rate,
            avgEntryPrice=avg_entry,
            avgEntryVolume=None,
        ),
        calendar={"daily": daily, "weekly": _agg(weekly), "monthly": _agg(monthly)},
        trades=fills,
        byTimeframe=[],
    )
