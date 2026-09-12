# architecture.drawio source (Python OMS)

**Status:** Disk [`architecture.drawio`](architecture.drawio) is the 7-sheet Python `am-oms` file (Context / BusinessFlow / Containers / Sequence / DataIdentity / SecurityTrust / Failures). This markdown is a readable companion, not a stale override.

## 1. Context

```mermaid
flowchart LR
  user[User]
  am[AM_product_modern-ui_Python_am-oms_Java_journal_portfolio]
  mkt[am-market_LTP]
  idp[Keycloak]
  mongo[Mongo_am-oms_and_journal]
  brk[Upstox_P2]
  user -->|"Create paper wallet / Place order"| am
  am -->|"getCurrentPrices"| mkt
  am -->|"Bearer"| idp
  am --> mongo
  am -.->|"P2"| brk
```

## 2. BusinessFlow

```mermaid
flowchart LR
  e0[Empty_no_broker]
  e1[Create_paper_wallet_10L]
  e2[Banner_available_empty_blotter]
  e3[Place_MARKET_BUY]
  e4[Blotter_FILLED_cash_now]
  e5[Holdings_and_analytics_catch_up]
  rej[Reject_overspend_LTP_OPTION]
  e0 -->|CTA| e1 -->|seeded| e2 -->|Place_order| e3 -->|success| e4 -->|async| e5
  e3 -.->|fail| rej
```

## 3. Containers (labeled edges)

```mermaid
flowchart LR
  ui[modern-ui]
  py[Python_am-oms]
  java[Java_journal]
  pf[am-portfolio]
  mkt[am-market]
  ui -->|"POST /v1/wallets"| py
  ui -->|"POST /v1/orders"| py
  ui -->|"GET /v1/portfolios/holdings"| pf
  ui -->|"GET /v1/portfolio-summary/id"| java
  py -->|"GET LTP getCurrentPrices"| mkt
  py -->|"POST /v1/portfolios kind=PAPER"| java
  py -->|"Kafka am-oms-fills"| java
  java -->|"Kafka TRADE_SYNC CREATE BUY SELL"| pf
```

## 4. Sequence

1. UI → Python `POST /v1/wallets`
2. Python → Java `POST /v1/portfolios kind=PAPER`
3. Java → portfolio `TRADE_SYNC CREATE`
4. UI → Python `POST /v1/orders` MARKET BUY
5. Python → market `getCurrentPrices`
6. Python → Java Kafka `am-oms-fills`
7. Java → portfolio `TRADE_SYNC BUY`
8. Reject: UI → Python overspend `REJECTED` (no Kafka)

## 5–7. Data / Security / Failures

- Python: walletId, available/reserved, orderId, idempotencyKey, portfolioUuid
- Java: portfolio UUID kind=PAPER, tradeId, sourceOrderId
- Events: OrderFillEvent then TRADE_SYNC (UUID vs name split)
- Bearer on HTTP; Kafka verifies ownerId; PAPER ≠ BROKER; owner-summary omits PAPER
- Failures: LTP, overspend, OPTION/LIVE/LIMIT, Java 502 no wallet, Kafka lag, kind/owner skip

The XML lives in [`architecture.drawio`](architecture.drawio). Do not paste an older am-trading file over it.
