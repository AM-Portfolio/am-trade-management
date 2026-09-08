# PLAN — `paper-trading-desk` (OMS slice)

| Field | Value |
|-------|--------|
| Kind | `feature` |
| Slug | `paper-trading-desk` |
| Lead repo | `am-trade-management` (am-oms) |
| Other repos | `am-modern-ui` |
| Target env | `preprod` |
| Branch | `feature/paper-trading-desk` |
| Delivery / Design | `10/10` |
| Agent satisfied | `yes` |

## Goal

Enable PAPER equity **LIMIT, SUPER, TRAIL, STOP** + resting LTP matcher + cancel working orders. MARKET unchanged. LIVE/OPTION still rejected. Line budget **900**.

## Semantics

See Cursor plan + modern-ui `docs/paper-trading-desk/PLAN.md`.

## Test Plan

Unit: limit fill/rest/match, super OCO, trail, cancel reserve, live/option reject. Update prior LIMIT_NOT_ENABLED assertion.
