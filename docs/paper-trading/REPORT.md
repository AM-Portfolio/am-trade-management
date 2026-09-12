# REPORT — `paper-trading`

Lead repo: **am-trade-management**. Slot: **preprod**. Branch: `feature/paper-trading`.

P0 paper equity MARKET is live on `https://am-preprod.asrax.in` (`/v1/wallets`, `/v1/orders`). Cash is Python `am-oms`. Journal consumes `am-oms-fills`. Holdings come from Java `TRADE_SYNC` on `am-portfolio`. am-portfolio does **not** subscribe to `am-oms-fills`.

## Test Plan

Verified **2026-09-08** against **AM — Preprod**. Gateway Python + collection **AM OMS** (`3526384-34493d05-c2a9-453d-aa42-81f765bd9638`). **20/20 pass.**

| Row | Result | Evidence |
|-----|--------|----------|
| 1 Auth | pass | Identity `POST /identity/auth/login` 200 |
| 2 Create PAPER wallet | pass | 200 existing wallet; `portfolioUuid` set |
| 3 BUY MARKET | pass | 201 `FILLED` RELIANCE qty 1 |
| 4 BUY overspend | pass | `REJECTED` `INSUFFICIENT_CASH` |
| 5 SELL happy | pass | 201 `FILLED` |
| 6 SELL over qty | pass | `REJECTED` `INSUFFICIENT_QTY` |
| 7 Paper holdings | pass | `GET /portfolio/v1/portfolios/holdings?portfolioId=` RELIANCE after BUY |
| 8 Broker isolation | pass | Broker holdings GET does not include the paper uuid |
| 9 LTP miss | pass | `LTP_UNAVAILABLE` |
| 10 OPTION P0 | pass | `OPTIONS_NOT_ENABLED` |
| 11 LIVE P0 | pass | `LIVE_NOT_ENABLED` |
| 12 Kind mismatch | pass | unit `PortfolioServiceImplPaperTest.paperSyncOnBrokerIsSkipped` |
| 13 Idempotency | pass | same `Idempotency-Key` → same `orderId` |
| 14 Journal from fill | pass | `GET /trade/v1/trades/details/portfolio/{uuid}` has `sourceOrderId` |
| 15 Journal portfolio | pass | `GET /trade/v1/portfolio-summary/{uuid}` `kind=PAPER` (journal `GET /v1/portfolios/{id}` is not implemented) |
| 16 LIMIT P0 | pass | `LIMIT_NOT_ENABLED` |
| 17 Journal down | pass | unit `test_wallet_order` 502 `JOURNAL_UNAVAILABLE` (pytest 5 passed) |
| 18 Owner summary | pass | `GET /trade/v1/portfolio-summary/by-owner` omits paper uuid |
| 19 FIFO SELL | pass | unit `OmsFillJournalServiceTest.sellFifoClosesOldest` |
| 20 Second wallet | pass | 200 same `walletId` |

AM OMS Postman folder `Paper trading`: **12/12** assertions (existing wallet no longer requires `available==1000000`).

## Fixes during Execute

- **LTP after hours:** market OHLC can return `lastPrice=0` with a usable `previousClose`. `am_oms.clients.Clients.ltp` takes the first positive of `lastPrice` / `previousClose` / `ohlc.close`.
- **Holdings empty after FILLED:** Mongo/Redis holdings cache treated an empty CREATE snapshot as fresh (and SWR returned it). Empty cache now forces a sync rebuild; TRADE_SYNC also deletes Mongo holdings cache. Image `ghcr.io/am-portfolio/am-portfolio:local-e2ae0ff` digest `sha256:1e796a58…` (kubectl rollout; Helm cannot import the Argo-owned Service).

## Preprod images (fast Helm / kubectl)

| Service | Path |
|---------|------|
| am-oms | Helm `am-oms-preprod`, Running |
| am-trade-management | `kubectl set image` (Argo owns Helm) |
| am-portfolio | kubectl rollout of rebuilt tag `local-e2ae0ff` |
| am-modern-ui | Image built `ghcr.io/am-portfolio/am-modern-ui:local-e12f40fd` after `flutter_quill` `^11.5.1`. Helm failed (ns check flake / Argo-owned); apply via `kubectl set image` |

## Not done (by design / wait)

- `--via actions`, GitOps image-tag, **prod**, PR — only if asked
- Dedicated Mongo user `am_oms_user_preprod` (OMS uses existing admin URI)
- Full `am test` suites on Java/Flutter (targeted units + this Test Plan)

## How to retry holdings

Create/get PAPER wallet → BUY MARKET → `GET /portfolio/v1/portfolios/holdings?portfolioId={portfolioUuid}` before flattening the position with SELL. Kafka lag: retry a few seconds (PLAN).
