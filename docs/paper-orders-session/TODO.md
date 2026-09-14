# TODO — `paper-orders-session`

Pack: [`docs/paper-orders-session/`](./).  
Branches (no new branch from main): `feature/paper-trading` (am-oms, am-portfolio), `feature/market-paper-desk` (am-modern-ui).

Resume from the first unchecked item under **Execute**.

---

## Plan loop (docs pack)

- [x] Find existing packs (`paper-trading`, `paper-trading-desk`) — new slug, do not overwrite
- [x] Branch: continue existing paper feature branches only
- [x] World-class feature map P0/P1/P2
- [x] First-run / virtual value UX
- [x] Latency & consistency
- [x] Corner-case matrix mapped to tests
- [x] Review roles
- [x] PLAN.md enriched (full design + market-calendar SoT)
- [x] images/ UI previews (ticket, Pending+Cancel all, Activity)
- [x] architecture.drawio — 7 sheets + **BusinessFlow / Sequence flow diagrams** (calendar status, MARKET vs LIMIT, cancel-all)
- [x] UI preview gate pass
- [x] Design sections; open questions decided (calendar = `/v1/market-calendar/status`; AMO reject; Investing-only)
- [x] Delivery / Design / scorecard ≥9.5 (docs); Agent satisfied for pack
- [x] Test Plan rows listed in PLAN
- [ ] **User reviewed** enriched PLAN + architecture.drawio + this TODO and confirmed **Execute**

---

## Execute — am-oms (`am-trade-management`)

### Session / calendar (SoT = market-data)

- [x] `Clients.market_status(exchange)` → `GET {AM_MARKET_BASE_URL}/v1/market-calendar/status`
- [x] Map status → PREOPEN / OPEN / CLOSED (OUTSIDE_SESSION before `sessionStart` = PREOPEN)
- [x] Cache status ~15–30s (matcher must not stampede)
- [x] Fail-safe: calendar HTTP down → MARKET reject; LIMIT rest; log `calendar_unavailable`
- [x] **Remove / stop using** `am_oms/data/holidays_in.json` as SoT
- [x] MARKET: reject `MARKET_CLOSED` / `MARKET_PREOPEN`; OPEN fill **lastPrice only** (never prevClose)
- [x] LIMIT/STOP/TRAIL/SUPER: rest when not OPEN; no off-hours touch fill
- [x] Matcher idle when phase ≠ OPEN

### Execution seam

- [x] `ExecutionPort` + `PaperExecutionEngine` + `LiveBrokerGateway` stub (`LIVE_NOT_ENABLED`)
- [x] Matcher filter `venue=PAPER` only
- [x] Always set `filledQuantity` (`0.00` unless FILLED)

### Matcher latency

- [x] Dedicated matcher httpx client (connect ~1s / read ~3s)
- [x] `ltp_many` batch; `refresh=false`
- [x] Single-flight tick + `wait_for` budget; `matcher_tick` logs
- [x] `list_orders`: no wallet N+1; support `from` / `to` (UI 7d)
- [x] Index note `(venue, status, symbol)` for ACCEPTED scans

### Cancel-all + schema + prefs

- [x] `POST /v1/orders/cancel-all` — conditional ACCEPTED→CANCELLED (race-safe)
- [x] Release BUY limit reserves; OCO sibling cancel + reserve fix
- [x] Idempotent retry → `cancelled: 0`
- [x] Request `amo: bool` → reject `AMO_NOT_SUPPORTED`
- [x] `productMode` Investing/CNC only → else `PRODUCT_NOT_SUPPORTED`
- [x] `GET/PUT /v1/prefs` — `orderTypeFavorite`
- [x] Helm/config: matcher timeouts; calendar path if needed

### Unit tests (am-oms)

- [x] Fake market status: CLOSED / PREOPEN / OPEN / HOLIDAY
- [x] MARKET lastPrice-only; no prevClose fill
- [x] Closed LIMIT rests; matcher idle when closed
- [x] Calendar down fail-safe
- [x] Batch matcher one OHLC call for N symbols
- [x] Cancel-all race + OCO reserves
- [x] amo / productMode rejects
- [x] Prefs round-trip
- [x] `am test` / pytest green for this slice

---

## Execute — am_paper_ui (`am-modern-ui`)

**Target UI:** [`order.png`](./order.png) / [`UI-ORDERS.md`](./UI-ORDERS.md) — replace split Today+Working with unified filtered table.

- [x] Today rows = FILLED + ACCEPTED + REJECTED + CANCELLED (today); map ACCEPTED → display **OPEN**
- [x] Header stats: Total / Filled / Working chips
- [x] Filter chips: All / Pending·Working / Executed / Failed·Cancelled + counts
- [x] Search symbol + All Types dropdown
- [x] Table: BUY/SELL chips, NSE badge, status pills, qty `filled/qty` for open, Actions column
- [x] Actions: Cancel (open); Re-order (filled); Details + Retry (rejected); **no Modify** until replace API
- [x] **Cancel all** when pending &gt; 0
- [x] Mobile cards use same filters (remove separate Working footer)
- [x] Type-aware CTA on ticket: Buy/Sell **at market** vs **Place buy/sell**
- [x] Reject toasts: MARKET_CLOSED / MARKET_PREOPEN / LTP_UNAVAILABLE
- [x] Order-type favorite: prefs API + SharedPreferences; first-time **MARKET** (not SUPER)
- [x] Helper copy: paper touch fill ≠ exchange book
- [x] `shared_preferences` dep if missing

---

## Execute — am-market-data

- [x] **No new endpoint** — consume existing `GET /v1/market-calendar/status`
- [ ] Verify multi-symbol OHLC for matcher batch (fix only if broken)
- [ ] Smoke: status returns `open` / `reason` / session window

---

## Postman / deploy / report

- [x] Extend OMS Postman (git): cancel-all, prefs, amo/MIS reject, orders from/to
- [x] Sync Postman cloud collection (MCP) if needed
- [ ] Test Plan row: `GET /v1/market-calendar/status` (market-data)
- [ ] Test Plan rows: MARKET closed, LIMIT closed, cancel-all, prefs, orders from/to
- [x] Deploy order: **am-oms → am-trade-management → am-portfolio** (preprod via auto/helm); modern-ui local with `AM_ENV=preprod` (not cluster UI deploy)
- [ ] Unit + Test Plan verified *(unit: 14 pytest green; calendar smoke / deploy still open)*
- [ ] Write `REPORT.md` only after verified
- [ ] PR only if user asks
- [ ] `/review` only if user asks

---

## Out of scope (do not implement this slice)

- Live broker / SPAN / MIS enablement / AMO accept path
- New market-calendar endpoint
- Separate matcher Deployment
- Muhurat / auction matching / exchange partials
