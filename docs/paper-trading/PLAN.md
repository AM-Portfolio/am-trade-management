# PLAN — `paper-trading`

| Field | Value |
|-------|--------|
| Kind | `feature` |
| Slug | `paper-trading` |
| Lead repo | **am-trade-management** (new Python deployable `am-oms` in this monorepo; Java journal stays) |
| Other repos | `am-portfolio` (PAPER kind + TRADE_SYNC), `am-modern-ui` (wallet + Place order). `am-market` read-only. **No new GitHub repo.** |
| Target env | `preprod` |
| Branch | `feature/paper-trading` (lead + other touched repos) |
| Delivery rating | **10/10** |
| Design rating | **10/10** |
| Scorecard overall | **9.5** |
| Agent satisfied | **yes** — user confirmed Execute; `architecture.drawio` is Python `am-oms` |

Execute gate passed. P0 backend + Test Plan **20/20** on preprod (see `REPORT.md`). Remaining: modern-ui image (quill 11.5.1), slow `--via actions` / GitOps / PR / prod if asked.

User locked: Python OMS+wallet as a **new service in this repo** (not a Java module, not `am-trading` repo). P0 = paper **equity** through real OMS + wallet. OPTION fields on OpenAPI now; paper options = P1; live broker = P2. No preview image generation this pass.

## Goal

Users practice buy/sell with **virtual cash** and **live am-market LTP** on the **same order and wallet APIs** that will later place live equity and options. Paper is `venue=PAPER`, not a fake Add Trade path. No real money, no broker deposit, no Upstox in P0.

Trade-management **Java journal** keeps full paper fill history for existing analytics. am-portfolio keeps a **PAPER** holdings book. Cash lives only on the Python wallet.

## Images

- User attachments: none
- UI preview gate: **n/a this pass** — user forbade generating preview images. P0 still names Place-order UI; Execute uses existing Flutter surfaces. Stale files under `images/` are **not** approval artifacts.

## Prerequisites (done / pending / blocked)

| Check | Status | Notes |
|-------|--------|--------|
| MCP | pending | Postman at Execute via **am-postman** |
| Downstream APIs | done | Journal `POST /v1/portfolios`, `POST /v1/trades/details`; holdings `GET /v1/portfolios/holdings`; LTP via market (Java [`MarketDataApiClient`](../../am-trade-api/src/main/java/am/trade/api/client/MarketDataApiClient.java) pattern: connect 3s / read 30s) |
| Postman | done | **Trade Management API** (`3526384-56e233f4-8b65-4ee6-ad5c-1f0ba710fe2e`); **AM Portfolio - Complete API** (`3526384-edb33b13-f470-48ab-b1e6-5b463ede9752`). Create **AM OMS** at Execute. Env: `AM-preprod` or related. |
| New service | decided | Python FastAPI in-repo `am-oms/`. Own Mongo DB name `am_oms`. Same cluster as other AM Mongo (Vault). |

## World-class feature map

Peer lens: paper and live share **one order ticket, blotter, and cash ledger**; venue/adapter switches execution. Cash is reserved then settled. Journal analytics stay on `trade_details`. Holdings stay on existing `TRADE_SYNC`.

### P0 — this slice

- New Python service `am-oms`: PAPER wallet (seed ₹10,00,000), order state machine, MARKET equity fill at LTP
- OpenAPI: `instrumentType=EQUITY\|OPTION` + option legs; P0 **rejects OPTION** (`OPTIONS_NOT_ENABLED`)
- `venue=LIVE` / `LIMIT` rejected with reason codes (schema present)
- Wallet create → Java `POST /v1/portfolios kind=PAPER` → `TRADE_SYNC` CREATE → am-portfolio PAPER book
- FILLED order → Kafka `am-oms-fills` → Java `TradeDetails` + `applyTradesDelta` → `TRADE_SYNC` BUY/SELL → PAPER holdings
- UI: create paper wallet, Paper banner, **Place order** (not Add Trade), blotter, available vs reserved
- Isolation vs BROKER/BASKET; owner-wide Java analytics **exclude PAPER** unless asked
- Postman + corner matrix

### P1 — next

- Paper options (premium/margin), LIMIT, cancel/replace, wallet ledger GET, outbox for Kafka, reset wallet
- GrowthBook flag; option ticket in UI; preview regen if asked

### P2 — later

- `BrokerVenue` (Upstox), LIVE wallet = real funds, step-up auth, fees/SPAN, deposits/withdrawals, extract wallet microservice if other products share cash

## First-run / virtual value UX

- **Zero broker linkage.** User never deposits or connects Upstox.
- Virtual value: `POST /v1/wallets` `kind=PAPER` seeds **₹10,00,000** (`seedAmount` default; not a payment). Copy: *Practice with virtual cash — not a live broker order.*
- Empty: no PAPER wallet → primary CTA **Create paper wallet**; after create, 0 orders → banner + full **available** + empty blotter + empty PAPER holdings + empty journal trades.
- CTA: Create wallet → Place order → blotter FILLED → holdings/analytics catch up if Kafka lags.
- Cash chrome: Python `available`/`reserved`, never Java `currentCapital`.
- Add Trade stays for **journal / imported broker fills**, not paper execution.
- **One PAPER wallet per owner** (unique `ownerId+kind=PAPER`). Repeat create returns the existing wallet (200).

## Latency & consistency

- LTP: Python HTTP client same timeouts as [`MarketDataApiClient`](../../am-trade-api/src/main/java/am/trade/api/client/MarketDataApiClient.java) (connect 3s, read 30s). Timeout / empty / missing symbol → `REJECTED` `LTP_UNAVAILABLE`; wallet unchanged; UI toast. **No silent fill.**
- P0 MARKET fill is **synchronous** in `POST /v1/orders` (reserve → fill or reject).
- Journal + holdings via Kafka are **eventually consistent**. UI: submit spinner; blotter/cash from order response; holdings/analytics may need refresh. No invented p99.
- Kafka publish fail after FILLED: order stays FILLED; log + metric `fills_publish_failed`; retry/outbox P1.
- Java consumer lag: blotter correct; `GET` journal/summary/holdings may miss newest fill until consume.

## Current system (verified)

- Journal, not OMS: [`TradeController.addTrade`](../../am-trade-api/src/main/java/am/trade/api/controller/TradeController.java) `POST /v1/trades/details` → [`TradeApiServiceImpl.addTrade`](../../am-trade-api/src/main/java/am/trade/api/service/impl/TradeApiServiceImpl.java) saves, [`applyTradesDelta`](../../am-trade-services/src/main/java/am/trade/services/service/impl/TradeProcessingServiceImpl.java), Kafka `TRADE_SYNC`.
- [`OrderType`](../../am-trade-models/src/main/java/am/trade/models/enums/OrderType.java) / [`OrderStatus`](../../am-trade-models/src/main/java/am/trade/models/enums/OrderStatus.java) are journal metadata, not a live book.
- Create book: [`PortfolioController`](../../am-trade-api/src/main/java/am/trade/api/controller/PortfolioController.java) `POST /v1/portfolios`; [`PortfolioCreateRequest`](../../am-trade-api/src/main/java/am/trade/api/dto/PortfolioCreateRequest.java) has `initialCapital`, **no `kind` today**. [`PortfolioApiServiceImpl`](../../am-trade-api/src/main/java/am/trade/api/service/impl/PortfolioApiServiceImpl.java) sets `currentCapital=initialCapital`.
- [`PortfolioSyncEvent`](../../am-trade-models/src/main/java/am/trade/models/kafka/PortfolioSyncEvent.java): `id`=journal UUID, `portfolioId`=name; topic `am-portfolio`. **No `portfolioKind` today.**
- [`PortfolioKind`](../../../am-portfolio/am-common-data/am-common-data-model/src/main/java/com/am/common/amcommondata/model/enums/PortfolioKind.java) = BROKER \| BASKET \| DELETED. `isBroker` hides non-broker holdings — **audit before PAPER**.
- Holdings: `GET /v1/portfolios/holdings`. am-portfolio consumes `eventType=TRADE_SYNC` on topic `am-portfolio` ([`PortfolioUpdateConsumerService`](../../../am-portfolio/portfolio-kafka/src/main/java/com/portfolio/kafka/consumer/PortfolioUpdateConsumerService.java)).
- Analytics: `trade_details` ([`TradeDetailsEntity`](../../am-trade-persistence/src/main/java/am/trade/persistence/entity/TradeDetailsEntity.java)) + [`PortfolioSummaryController`](../../am-trade-api/src/main/java/am/trade/api/controller/PortfolioSummaryController.java).
- UI: [`TradeWebScreen`](../../../am-modern-ui/am_trade_ui/lib/features/trade/presentation/web/trade_web_screen.dart), [`AddTradeWebPage`](../../../am-modern-ui/am_trade_ui/lib/features/trade/presentation/add_trade/pages/add_trade_web_page.dart).
- Helm today: Java [`helm/values.yaml`](../../helm/values.yaml) image `am-trade-management-service` port 8080. No Python chart yet.
- `POST /trade/orders` is only an identity-plan sketch — not implemented.

## Target journeys

1. First-run: Create PAPER wallet → Java paper portfolio + PAPER book → banner + ₹10L → empty blotter.
2. BUY MARKET equity → LTP fill → cash down → blotter FILLED → journal OPEN trade → holdings (Kafka).
3. SELL MARKET ≤ OMS qty → cash up → FIFO close/partial on journal → holdings down.
4. Reject: overspend, oversell, LTP miss, OPTION, LIVE, LIMIT — wallet unchanged; broker untouched; no Kafka.
5. Duplicate `Idempotency-Key` → same `orderId`, one debit.
6. Analytics: `GET /v1/portfolio-summary/{paperPortfolioUuid}` and trade filters on that id; owner-wide summary **omits PAPER**.

## Identity & ownership

- Python + Java public APIs: Bearer. `ownerId` from token. Never trust body `ownerId`.
- Python Kafka consumer (Java): no Bearer; verify `event.ownerId == journalPortfolio.ownerId`; skip otherwise (log `fill_owner_mismatch`).
- `walletId` / `orderId` owned by Python. Journal `tradeId` new UUID; `sourceOrderId` = Python `orderId` (unique index).
- Event `id` = journal portfolio UUID; `portfolioId` = name (same split as today).
- `Idempotency-Key` required on `POST /v1/orders`. Unique `(ownerId, idempotencyKey)`.

## Operability (Python service — clone am-notification)

Template: [`am-platform/am-notification`](../../../am-platform/am-notification) (FastAPI + Motor + Kafka + Helm + `.am.yaml`). Multi-service-in-one-git pattern: [`am-market/am-news/.am.yaml`](../../../am-market/am-news/.am.yaml) (nested service folder; root Java [`.am.yaml`](../../.am.yaml) stays for `am-trade-management-service`).

**Do not invent a new layout.** Copy these files/behaviors:

| Piece | Copy from | Ours |
|-------|-----------|------|
| Package | `am_notification/` | `am_oms/` |
| App | [`main.py`](../../../am-platform/am-notification/am_notification/main.py) lifespan, `APIException` handler, `/health` `/health/live` `/health/ready` | same |
| Settings | [`core/config.py`](../../../am-platform/am-notification/am_notification/core/config.py) pydantic-settings aliases | `AM_OMS_*` |
| Mongo | [`core/database.py`](../../../am-platform/am-notification/am_notification/core/database.py) Motor + indexes | db `am_oms` |
| Deps | [`deps.py`](../../../am-platform/am-notification/am_notification/deps.py) | wallet/order/market/kafka |
| Auth | [`require_auth_context()`](../../../am-platform/am-notification/am_notification/api/notification_router.py) `am_platform_security` | `context.subject` = ownerId |
| Envelope | `am_platform_common.APIResponse` / `APIException` | named DTOs inside `data` |
| Helm | [`helm/values.yaml`](../../../am-platform/am-notification/helm/values.yaml) `language: python`, port **8080**, probes `/health/live` `/health/ready` | image `am-oms` |
| amctl | [`.am.yaml`](../../../am-platform/am-notification/.am.yaml) `runtime: python` | nested `am-oms/.am.yaml` |
| Docker | [`Dockerfile`](../../../am-platform/am-notification/Dockerfile) `python:3.12-slim`, uvicorn `--port 8080` | **build context = `am-oms/`** (this repo has no `libraries/`; pip-install `am-platform-common` + `am-platform-security` from GitHub subdirectory of `AM-Portfolio/am-platform`) |

Ingress like notification (path = router prefix, **do not strip**): host `am-preprod.asrax.in`, paths `/v1/wallets` and `/v1/orders`. Java keeps `/v1/portfolios` — no `/trade-execution` prefix.

Tree to scaffold (Execute) — **few files, reuse libs** (see Coding bar):

```
am-trade-management/am-oms/
  .am.yaml
  pyproject.toml
  requirements.txt
  Dockerfile
  helm/values.yaml
  helm/values.preprod.yaml
  helm/vault-mappings.yaml
  tests/test_wallet_order.py      # one test module
  am_oms/
    main.py
    deps.py
    core/config.py
    core/database.py
    api/routers.py                # wallets + orders in one module
    schemas.py                    # all named DTOs
    services.py                   # wallet + order + fill (one module)
    clients.py                    # journal HTTP + market LTP + kafka produce
```

No `paper_venue.py`, no extra providers package, no P1 route bodies. `uvicorn am_oms.main:app --host 0.0.0.0 --port 8080`

Vault env (same mapping style as notification): mongo URI/db/user/password, kafka bootstrap/user/pass, OIDC JWKS, `AM_TRADE_JOURNAL_BASE_URL`, `AM_MARKET_BASE_URL`.

Root Java Helm/image **unchanged**. `am run` / `am deploy` from `am-oms/` via its `.am.yaml`.

## Coding bar (hard — follow while writing)

**Line cap:** P0 **`am_oms/**/*.py` ≤ 600 lines** (production package only). Helm/Dockerfile/docs/requirements do not count. Tests live in `tests/` and stay **≤ 200 lines**. If a change would exceed 600, **stop and reuse** — do not add files.

Count before done: `git ls-files am-oms/am_oms | xargs wc -l`

**Reuse (do not rewrite):**

- Auth, logging, error envelope: `am_platform_security.require_auth_context`, `am_platform_common` `APIResponse` / `APIException` / `LoggingMiddleware` — copy handler from [`am_notification/main.py`](../../../am-platform/am-notification/am_notification/main.py), do not invent a parallel stack.
- Settings/Mongo: slim copy of notification `config.py` / `database.py` (aliases only).
- HTTP: one `httpx.AsyncClient` in `clients.py` for journal + market.
- Money: one `apply_fill(wallet, side, qty, ltp)` used by BUY and SELL — no duplicated ledger math.
- Java journal consumer (when Execute reaches Java): call existing `saveTradeDetails` + `applyTradesDelta` + `publishPortfolioSyncEvent` — no second journal pipeline.

**Standards (match-repo):**

- Python 3.11+, FastAPI type hints, Pydantic v2 named schemas (no bare `dict` response).
- `ownerId` only from `AuthContext.subject`. No secrets in repo.
- Routers thin: validate + call service. Services own Mongo txn + Kafka.
- P1 routes (`/ledger`, cancel, replace) = **one-liner 501 / reject reason**, not implementations.
- No comments that narrate; no unused abstractions; no new framework.
- Prefer one function over a class unless Motor/httpx needs a small wrapper.

**Per-file target:** each `.py` **< 150 lines**; none **> 200**. If `services.py` grows, extract **one** helper into `clients.py`, not a new package.

## State & money

**Wallet (PAPER):** `available`, `reserved`, `INR`. Invariant both ≥ 0. Seed: `available=seedAmount` (default 1_000_000), `reserved=0`.

**OMS positions (Python):** `qty` per `walletId+symbol`. SELL gate does not wait on Kafka.

**P0 BUY MARKET:** LTP miss → REJECTED no reserve. `notional=qty*ltp`; if `notional > available` → `INSUFFICIENT_CASH`. Else `available-=notional`, `reserved+=notional`, ACCEPTED, fill: `reserved-=notional`, FILLED, position+=qty, emit fill.

**P0 SELL MARKET:** `qty > position` → `INSUFFICIENT_QTY`. LTP miss → REJECTED. Fill: `available += qty*ltp`, position-=qty, FILLED, emit fill.

**Wallet create (locked order — no orphan cash):**

1. If owner already has PAPER wallet → 200 existing (do not create second Java portfolio).
2. Else **Java first:** forward Bearer `POST /v1/portfolios` `{ name: "Paper", currency: INR, initialCapital: seed, kind: PAPER }`. Fail → **502/503**, no Python wallet. Log `journal_portfolio_create_failed`.
3. Persist Python wallet with `portfolioUuid` + `portfolioId` (name). If Mongo persist fails after Java 201: retry create finds existing PAPER journal portfolio for owner (`kind=PAPER`) and attaches; do not create a second Java book. Unique owner+kind on journal.

Java `currentCapital` on create = seed (display only). Cash truth = Python wallet.

**OPTION/LIVE/LIMIT P0:** persist REJECTED with reason; no cash; no Kafka.

**Race:** serializable wallet update per `walletId`. Two different idempotency keys can theoretically overspend — best-effort P0; document.

## Journal mapping (analytics)

Java consumer on `am-oms-fills` reuses addTrade internals: save `TradeDetails`, `applyTradesDelta`, evict `analyticsCache`/`portfolioSummary`/`tradeSummaryCache`, then `publishPortfolioSyncEvent`.

| Fill | Journal |
|------|---------|
| BUY | New OPEN LONG; `entryInfo` qty/price/time; tags `PAPER`; `sourceOrderId` |
| SELL | **FIFO** oldest OPEN `portfolioId+symbol`. Partial: `exitInfo.quantity` += sold; if remaining entry qty > exited, stay OPEN; if fully exited, WIN/LOSS/BREAK_EVEN from prices. Next OPEN if SELL qty remains. Never insert SELL-only row. |

Idempotent: unique `sourceOrderId`. Retry skips.

**`kind` on journal:** add `kind` to [`PortfolioCreateRequest`](../../am-trade-api/src/main/java/am/trade/api/dto/PortfolioCreateRequest.java) and `PortfolioModel` (`PAPER`\|`BROKER` default BROKER). [`PortfolioSyncEvent`](../../am-trade-models/src/main/java/am/trade/models/kafka/PortfolioSyncEvent.java) + portfolio `TradePortfolioSyncEvent`: `portfolioKind`.

**Owner-wide analytics:** [`getPortfolioSummariesForAuthenticatedUser`](../../am-trade-api/src/main/java/am/trade/api/controller/PortfolioSummaryController.java) **excludes** `kind=PAPER` by default. Query `includePaper=true` to include. Filters with no portfolioId exclude PAPER trades.

## Isolation

- PAPER wallet/orders/positions never apply to BROKER/BASKET.
- `TRADE_SYNC.portfolioKind=PAPER`. am-portfolio **refuses** PAPER action on non-PAPER book (skip + metric). Does **not** consume `am-oms-fills`.
- No broker-statement import onto PAPER.
- UI live overview keeps `isBroker`; paper UI uses `portfolioUuid` from wallet.

## Failure modes

| Trigger | System | User sees |
|---------|--------|-----------|
| LTP timeout/empty | REJECTED `LTP_UNAVAILABLE`; no ledger | Error toast |
| Overspend / oversell | REJECTED; no Kafka | Error; available unchanged |
| OPTION / LIVE / LIMIT | REJECTED reason; blotter row | Message |
| Java portfolio create fail | 502; no wallet | Retry CTA |
| Kafka lag / publish fail | Order FILLED | Holdings/analytics lag; blotter OK |
| Kind mismatch | Skip TRADE_SYNC apply | Broker unchanged |
| Duplicate idempotency | 200 same order | Same blotter row |
| Fill owner mismatch | Skip journal | Log only |

## Auth / envelope (Python)

- `ErrorResponse`: `{ code: RejectReason, message, orderId?, walletId? }`
- Money/qty: decimal **strings**. Time: ISO-8601 UTC.

### Enums

`WalletKind` PAPER\|LIVE; `Venue` PAPER\|LIVE; `InstrumentType` EQUITY\|OPTION; `OrderSide` BUY\|SELL; `OrderType` MARKET\|LIMIT\|STOP\|STOP_LIMIT (P0 MARKET only); `OrderStatus` NEW\|ACCEPTED\|PARTIALLY_FILLED\|FILLED\|REJECTED\|CANCELLED\|EXPIRED; `OptionType` CE\|PE; `RejectReason` INSUFFICIENT_CASH\|INSUFFICIENT_QTY\|LTP_UNAVAILABLE\|OPTIONS_NOT_ENABLED\|LIVE_NOT_ENABLED\|LIMIT_NOT_ENABLED\|CANCEL_NOT_ENABLED\|REPLACE_NOT_ENABLED\|VALIDATION\|IDEMPOTENCY_CONFLICT\|NOT_FOUND\|FORBIDDEN.

## Wallet contracts — `/v1/wallets`

`WalletCreateRequest`: `kind`, `currency` (INR), `seedAmount?`

`WalletResponse`: `walletId`, `kind`, `currency`, `available`, `reserved`, `portfolioId` (name), `portfolioUuid`, `ownerId`, `createdAt`, `updatedAt`

| P | operationId | Method | Notes |
|---|-------------|--------|--------|
| P0 | `createWallet` | POST /v1/wallets | 201 or 200 existing. LIVE → 400 `LIVE_NOT_ENABLED` |
| P0 | `listWallets` | GET /v1/wallets?kind= | owner-scoped |
| P0 | `getWallet` | GET /v1/wallets/{walletId} | 404 |
| P1 | `listWalletLedger` | GET .../ledger | OpenAPI now; P0 **501** |
| P2 | `depositWallet` / `withdrawWallet` | POST deposits/withdrawals | not P0 |

## Order contracts — `/v1/orders`

Header `Idempotency-Key` required on create.

`OptionLeg`: `underlying`, `expiry`, `strike`, `optionType`, `lotSize?`

`OrderCreateRequest`: `walletId`, `venue`, `instrumentType`, `symbol`, `side`, `orderType`, `quantity`, `limitPrice?`, `option?`

`OrderResponse`: `orderId`, wallet/owner, venue, instrument, symbol, side, type, qty, `status`, `fillPrice`, `filledQuantity`, `rejectReason`, `option`, `walletSnapshot {available,reserved}`, timestamps, `idempotencyKey`

| P | operationId | Method | P0 |
|---|-------------|--------|-----|
| P0 | `createOrder` | POST /v1/orders | MARKET PAPER EQUITY fills sync; else reject reasons |
| P0 | `listOrders` | GET /v1/orders | blotter |
| P0 | `getOrder` | GET /v1/orders/{orderId} | |
| P1 | `cancelOrder` / `replaceOrder` | POST .../cancel, /replace | 400 `*_NOT_ENABLED` |
| P2 | `listOrderFills` | GET .../fills | later |

## Kafka

**`OrderFillEvent`** topic `am-oms-fills`: `eventId`, `source=am-oms`, `dataVersion=1.0`, `orderId`, `walletId`, `ownerId`, `action`, `symbol`, `quantity`, `price`, `instrumentType`, `option?`, `portfolioKind=PAPER`, `id` (journal UUID), `portfolioId` (name), `timestamp`.

Consumer: **Java journal only**. Then Java emits `TRADE_SYNC` on `am-portfolio`.

## Corner-case matrix

| Case | Trigger | Expected | Covered by |
|------|---------|----------|------------|
| Empty LTP | bad symbol / timeout | REJECTED `LTP_UNAVAILABLE`; wallet same | unit + Test Plan 9 |
| Overspend | qty*LTP > available | REJECTED `INSUFFICIENT_CASH` | unit + row 4 |
| Oversell | qty > OMS position | REJECTED `INSUFFICIENT_QTY` | unit + row 6 |
| OPTION / LIVE / LIMIT | those fields | REJECTED reason; no Kafka | unit + rows 10–11, 16 |
| Kind mismatch | PAPER TRADE_SYNC → BROKER | skip | unit + row 12 |
| Double submit | same Idempotency-Key | same orderId; one debit | unit + row 13 |
| Empty first-run | create wallet only | seed; 0 orders; Java PAPER portfolio; empty holdings | rows 2, 15 |
| Java create fail | journal 5xx | no wallet; 502 | unit + row 17 |
| Kafka lag | delayed consume | blotter FILLED; journal/holdings catch up | row 7 note |
| Owner summary mix | GET summary by-owner | PAPER omitted | row 18 |
| FIFO SELL | two BUYs then SELL qty of first | oldest OPEN closed; newer remains | unit + row 19 |
| Second wallet | POST wallets again | 200 same walletId | row 20 |
| Fill replay | same sourceOrderId | one journal row | unit + row 14 |

## Out of scope

- Code before user confirm
- New GitHub repo / Java-in-process OMS
- Live broker, deposits, paper option fill, LIMIT working (P1+)
- Mixing paper P&L into broker owner-summary (default)
- Prod deploy (re-ask)
- Preview image regen (user said no)
- New DB engine

## Open questions

- (none) — Python layout cloned from **am-notification**; nested `.am.yaml` like **am-news**; ingress `/v1/wallets` + `/v1/orders` on 8080 (not `/trade-execution`, not port 8000).

## Review roles

| Role | When | Looks for |
|------|------|-----------|
| Agent architect | Until Agent satisfied | Scorecard, adversarial, code cites |
| You | User review gate | First-run, Place order vs Add Trade, P0 vs P1, **no images this pass** |
| Optional `/review` | If asked | am-code-review |

## UI previews (modern-ui)

- **Gate: n/a this pass** — user forbade image generation. P0 still includes Place order / wallet UI at Execute.
- Widget cites: new place-order surface beside [`AddTradeWebPage`](../../../am-modern-ui/am_trade_ui/lib/features/trade/presentation/add_trade/pages/add_trade_web_page.dart); banner on [`TradeWebScreen`](../../../am-modern-ui/am_trade_ui/lib/features/trade/presentation/web/trade_web_screen.dart).

## Services

### am-oms (new, Python, this repo)

- Clone [`am-notification`](../../../am-platform/am-notification) module layout (see Operability tree). FastAPI + Motor + `require_auth_context` + Helm python 8080.
- Tests: seed; BUY/SELL; overspend; oversell; LTP miss; OPTION/LIVE/LIMIT reject; idempotency; second wallet 200; Java-fail → no wallet (mock).

### am-trade-management Java (existing)

- `kind` on create + model; `portfolioKind` on `PortfolioSyncEvent`; consumer `am-oms-fills` → TradeDetails + applyTradesDelta + TRADE_SYNC; `sourceOrderId` unique; owner-summary exclude PAPER; FIFO SELL mapper.
- Tests: BUY→OPEN; SELL FIFO; replay; owner mismatch skip; by-owner omits PAPER.

### am-portfolio (existing)

- `PortfolioKind.PAPER` + `isPaper`; audit `isBroker`; TRADE_SYNC CREATE/BUY/SELL with kind; refuse PAPER on BROKER. **Do not** subscribe `am-oms-fills`.
- Tests: PAPER apply; mismatch; broker unchanged.

### am-modern-ui (existing)

- Create wallet CTA; banner; Place order; blotter; map reject codes; paper holdings by `portfolioUuid`; analytics uses paper portfolio id.
- Tests: cubit seed; reject toast.

### am-market

- No P0 impl.

## Test Plan (Postman or MCP)

Collection **AM OMS** folder `Paper trading`. Isolation rows on Trade Management API + AM Portfolio. Env `AM-preprod` or related.

| Row | Request | Method + path | Expected | Owning | Result |
|-----|---------|---------------|----------|--------|--------|
| 1 | Auth | Auth Get Token | token on env | — | pass (preprod login) |
| 2 | Create PAPER wallet | POST `/v1/wallets` | 201; available=1000000; portfolioUuid set | am-oms | pass (200 existing) |
| 3 | BUY MARKET | POST `/v1/orders` | FILLED; available down | am-oms | pass |
| 4 | BUY overspend | huge qty | REJECTED INSUFFICIENT_CASH | am-oms | pass |
| 5 | SELL happy | qty ≤ position | FILLED; available up | am-oms | pass |
| 6 | SELL over qty | | REJECTED INSUFFICIENT_QTY | am-oms | pass |
| 7 | Paper holdings | GET /v1/portfolios/holdings | PAPER position (may retry) | portfolio | pass |
| 8 | Broker isolation | GET broker holdings | unchanged | portfolio | pass |
| 9 | LTP miss | bad symbol | REJECTED LTP_UNAVAILABLE | am-oms | pass |
| 10 | OPTION P0 | instrumentType=OPTION | OPTIONS_NOT_ENABLED | am-oms | pass |
| 11 | LIVE P0 | venue=LIVE | LIVE_NOT_ENABLED | am-oms | pass |
| 12 | Kind mismatch | unit | PAPER not on BROKER | portfolio | pass (unit) |
| 13 | Idempotency | same key twice | same orderId | am-oms | pass |
| 14 | Journal from fill | GET trades by paper portfolioId | OPEN/closed row; sourceOrderId | journal | pass |
| 15 | Journal portfolio | GET /v1/portfolios/{id} | kind=PAPER | journal | pass via `GET /trade/v1/portfolio-summary/{uuid}` |
| 16 | LIMIT P0 | orderType=LIMIT | LIMIT_NOT_ENABLED | am-oms | pass |
| 17 | Journal down | mock/unit | 502; no wallet | am-oms | pass (unit) |
| 18 | Owner summary | GET /v1/portfolio-summary/by-owner | no PAPER book | journal | pass |
| 19 | FIFO SELL | unit two opens | oldest closed first | journal | pass (unit) |
| 20 | Second wallet | POST wallets again | 200 same walletId | am-oms | pass |

## Deploy

preprod fast Helm **am-oms**, then Java trade-management, portfolio, modern-ui. No gitops image-tag on fast path. **Prod:** ask again.

## Observability (P0)

Log+counter: `order_rejected{reason}`, `ltp_timeout`, `fills_publish_failed`, `journal_portfolio_create_failed`, `fill_owner_mismatch`, `paper_kind_mismatch`.

## Agent scorecard

| Dimension | Score /10 | Evidence |
|-----------|-----------|----------|
| Product / features | 9.5 | Map P0–P2; first-run; previews n/a by user |
| Architecture / design | 9.5 | 7-sheet `architecture.drawio` is Python `am-oms` → Java journal → TRADE_SYNC |
| Data / identity / contracts | 9.5 | wallet/order OpenAPI; events; sourceOrderId; kind |
| State / money / consistency | 9.5 | Reserve/settle; Java-first create; FIFO SELL |
| Reliability / failure | 9.5 | LTP; Kafka lag; 502 journal; matrix |
| Security / authz | 9.5 | Bearer; fill owner check; PAPER≠BROKER |
| Observability | 9.0 | Reject/LTP/Kafka/mismatch counters |
| Testability | 9.5 | Matrix → rows 2–20 |
| Operability / rollout | 9.5 | Clone am-notification; nested .am.yaml; port 8080; ingress /v1/wallets /v1/orders |
| TODO / implementability | 9.5 | Junior tasks; backend P0 done; modern-ui next |
| **Overall (mean)** | **9.5** | |

## Adversarial review

| Severity | Finding |
|----------|---------|
| Nit | GET ledger 501 until P1 |
| Nit | Stale preview PNGs remain in images/ |

- Blocker/Major count: **0** → Agent satisfied **yes**

## Rating log (agent architect loop)

| Pass | Delivery | Design | Overall | Gaps then patched |
|------|----------|--------|---------|-------------------|
| 1 (Cursor plan) | 6 | 7 | 8.1 | Pack stale; no TODO/draw.io for Python |
| 2 (PLAN+TODO) | 9 | 8 | 8.8 | Contracts/corners/TODO locked; `.drawio` write blocked in Plan mode |
| 3 (Execute) | 10 | 10 | 9.5 | `.drawio` replaced with am-oms; user confirmed Execute |

## User review gate

Review **PLAN.md**, **architecture.drawio** (7 sheets), **TODO.md**. Previews not in this pass. Confirm **go / approved / implement** before Python/Java/Dart. You may promote P1 or ask preview regen.

## REPORT gate

Do not write `REPORT.md` until unit tests and Test Plan rows are verified (or you accept remaining not-verified).
