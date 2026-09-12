# TODO — `paper-trading`

Pack: `docs/paper-trading/`. Resume from the first unchecked item.

## Plan loop (no impl until Agent satisfied + user confirm)

- [x] Find existing pack / Postman (Trade Management API + AM Portfolio - Complete API; new **AM OMS** at Execute)
- [x] Branch `feature/paper-trading` on **am-trade-management** + am-portfolio (modern-ui next)
- [x] World-class feature map P0/P1/P2 (Python `am-oms` in this monorepo)
- [x] First-run / virtual value UX (one PAPER wallet, seed ₹10L, not payment)
- [x] Latency & consistency (LTP 3s/30s → REJECTED; Kafka lag journal/holdings)
- [x] Corner-case matrix mapped to Test Plan rows 2–20
- [x] Review roles
- [x] PLAN.md (previews **n/a this pass** — user forbade images)
- [x] architecture.drawio (7 sheets; `am-oms`)
- [x] UI preview gate **n/a this pass**
- [x] Design sections; Open questions empty
- [x] Delivery 10 + Design 10 (pack was Execute-confirmed by user)
- [x] Scorecard overall 9.5
- [x] Adversarial 0 Blocker/Major
- [x] **Agent satisfied** — user said start development
- [x] Test Plan rows 1–20 executable (verified preprod 2026-09-08)
- [x] **User reviewed** PLAN and confirmed Execute

## Per service (unit-test loop) — Execute only after user confirm

### am-oms (new Python in am-trade-management)

Clone **am-notification** (see PLAN Operability + **Coding bar**). Do not invent folders. **≤ 600 lines** in `am_oms/`.

- [x] Add `am-oms/` with `.am.yaml` (`runtime: python`, image `ghcr.io/am-portfolio/am-oms`, port 8080) like [`am-news/.am.yaml`](../../../am-market/am-news/.am.yaml)
- [x] `pyproject.toml` + `requirements.txt` (fastapi, uvicorn, motor, aiokafka, pydantic-settings, httpx, am-platform-common, am-platform-security)
- [x] `Dockerfile` python:3.12-slim; `CMD uvicorn am_oms.main:app --host 0.0.0.0 --port 8080`; HEALTHCHECK `/health/live`
- [x] Package files only: `main.py` `deps.py` `core/config.py` `core/database.py` `api/routers.py` `schemas.py` `services.py` `clients.py` — no extra packages
- [x] Reuse `require_auth_context`, `APIResponse`/`APIException`, notification health/lifespan — do not rewrite
- [x] `helm/values.yaml` `language: python` port 8080 probes `/health/live` `/health/ready`; ingress `/v1/wallets` + `/v1/orders`
- [x] `helm/vault-mappings.yaml` + `values.preprod.yaml` mongo/kafka/OIDC/journal URL/market URL
- [x] Unique: `(ownerId, kind)` one PAPER wallet; `(ownerId, idempotencyKey)`
- [x] Wallet create: Java `POST /v1/portfolios` **first**; 502 if Java fails; 200 if wallet exists
- [x] LIVE / OPTION / LIMIT / cancel / replace → reject or 501 in a few lines
- [x] One `apply_fill` for BUY/SELL cash; LTP in `clients.py` (3s/30s)
- [x] Kafka produce `am-oms-fills` from `clients.py`
- [x] `tests/test_wallet_order.py` ≤ 200 lines
- [x] **`wc -l am_oms/**/*.py` ≤ 600** before calling the slice done (454)
- [x] pytest **verified** (4 passed). `am test` / Helm deploy not yet

### am-trade-management Java (existing journal)

- [x] `kind` on `PortfolioCreateRequest` + `PortfolioModel` (PAPER | BROKER default)
- [x] `portfolioKind` on `PortfolioSyncEvent` (CREATE/BUY/SELL)
- [x] Kafka consumer `am-oms-fills` → map to `TradeDetails` (`sourceOrderId`, tags PAPER)
- [x] Reuse `saveTradeDetails` + `applyTradesDelta` + cache evict + `publishPortfolioSyncEvent`
- [x] FIFO SELL onto oldest OPEN `portfolioId+symbol`; no SELL-only row; unique `sourceOrderId`
- [x] Verify `event.ownerId == portfolio.ownerId`; else skip + `fill_owner_mismatch`
- [x] `GET /v1/portfolio-summary/by-owner` **excludes** PAPER unless `includePaper=true`
- [x] Trade filters with no portfolioId exclude PAPER
- [x] Do not turn Add Trade / `OrderStatus` journal enums into the OMS
- [x] Unit: BUY OPEN; FIFO SELL; replay; owner mismatch; owner-wide omits PAPER (TradeManagementServiceImplTest). `am test` full suite not yet

### am-portfolio (existing)

- [x] Add `PAPER` + `isPaper` to `PortfolioKind.java` (vendored am-common-data)
- [x] Audit `isBroker` in `PortfolioHoldingsService`, `PortfolioOverviewService`, `PortfolioHoldingsMapper`, `PortfolioServiceImpl`
- [x] Consume existing topic `am-portfolio` `TRADE_SYNC` with `portfolioKind`; refuse PAPER on non-PAPER
- [x] **Do not** subscribe to `am-oms-fills`
- [x] Unit: PAPER TRADE_SYNC on BROKER skipped (PortfolioServiceImplPaperTest). Full `am test` not yet

### am-modern-ui (existing)

- [x] Create-paper-wallet UI (seed 10L; copy: virtual cash, not live); one wallet — hide duplicate CTA if exists
- [x] Paper banner + available/reserved on `TradeWebScreen` / holdings chrome
- [x] New Place order surface (not `AddTradeWebPage`) + blotter
- [x] Hide option/live/limit tickets in P0; map reject reasons to toasts
- [x] Paper holdings + journal analytics via `portfolioUuid` from wallet
- [x] Config base URL same host; paths `/v1/wallets` `/v1/orders` (not `/trade-execution`)
- [x] Widget/cubit tests; flutter test **verified** (oms_cubit_test + compilation). Full `am test` not yet

### am-market (existing)

- [x] No P0 impl

## Deploy (fast — after impl)

- [x] Slot = **preprod**. No `dev`.
- [x] `am deploy doctor --env preprod` then `--via helm` for **am-oms** (Running). trade-management / portfolio images via kubectl (Argo owns Helm). modern-ui Docker: quill 11.5.1 bump; Helm still Argo-owned
- [x] **Prod:** asked and waited this turn before Helm — not deploying prod

## Feature-test loop (Postman first)

- [x] **Analyze** then create collection **AM OMS** via **am-postman**; folder `Paper trading` (Asrax: `3526384-34493d05-c2a9-453d-aa42-81f765bd9638`). Test Plan 20/20 on preprod.
- [x] Patch isolation rows on Trade Management API + AM Portfolio - Complete API (also AM Trade Metrics in Asrax)
- [x] Run Test Plan rows 1–20 against `AM-preprod` (or related)
- [x] All rows pass (or loop impl+unit; cap ~3 then pause)

## Report

- [x] **Only after tests verified:** write `REPORT.md`

## Deploy (slow — after user satisfied)

- [ ] `--via actions`; GitOps if managed
- [ ] **Prod:** asked again before Actions/GitOps

## PR

- [ ] PR only if the user asked
- [ ] `/review` then `/pr-ready`
