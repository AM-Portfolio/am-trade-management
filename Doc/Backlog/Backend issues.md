# AM Trade Management Remediation Plan & Implementation Guide

This document is a comprehensive, production-ready implementation plan to resolve all identified architectural, financial, concurrency, and security issues across the `am-trade-management` repository, as well as a forward-looking roadmap to elevate the app from a **4.5 / 10** past **7.8 / 10** to a **9.5+ / 10 enterprise-grade platform**.

Each issue includes:
- **Exact File & Line Numbers**
- **Root Cause Analysis**
- **Before & After Code Implementation**
- **Testing & Verification Criteria**

---

## Architecture & Remediation Roadmap

```mermaid
flowchart TD
    subgraph Phase 1: Security & Tenant Isolation [Immediate Hotfixes]
        S1["1.1 PortfolioSummary IDOR (/by-owner)"]
        S2["1.2 Notebook Read/Delete IDOR"]
        S3["1.3 Journal Template IDOR"]
        S4["1.4 Trade Replay Analytics IDOR"]
        S5["1.5 Security Fallback Hardening"]
        S6["1.6 CORS & Kafka Deserialization"]
    end

    subgraph Phase 2: Financial Calculation Integrity [High Priority]
        F1["2.1 Partial Exit P&L Multiplication Bug"]
        F2["2.2 Client-Forged P&L Injection"]
        F3["2.3 Trade Resurrection on Delete"]
        F4["2.4 Mock Risk-Reward Constants"]
        F5["2.5 Double-Counted Trades on Exit"]
    end

    subgraph Phase 3: OMS Concurrency & Execution [High Priority]
        O1["3.1 Atomic Wallet Balances ($inc)"]
        O2["3.2 Multi-Pod CAS Order Matching"]
        O3["3.3 AIOKafkaProducer Singleton"]
        O4["3.4 Partial Fills Support"]
    end

    subgraph Phase 4: Event Reliability & Code Hygiene [Medium Priority]
        H1["4.1 Portfolio Sync Error Propagation"]
        H2["4.2 Fix Jackson YAML Nesting"]
        H3["4.3 Convert UTF-16LE Files to UTF-8"]
        H4["4.4 Git Tracking Cleanup & Lombok Update"]
    end

    subgraph Phase 5: Future Architecture & Excellence [Roadmap to 9.5+/10]
        E1["5.1 Consolidate Service Layer & Split God Classes"]
        E2["5.2 MongoDB Normalization & Aggregation Pipelines"]
        E3["5.3 Testcontainers Suite & 80%+ Coverage"]
        E4["5.4 Transactional Outbox Pattern"]
        E5["5.5 Advanced Tax Lots, Wash Sales & Corporate Actions"]
        E6["5.6 Unified Multi-Language CI/CD Matrix"]
    end

    Phase 1 --> Phase 2
    Phase 2 --> Phase 3
    Phase 3 --> Phase 4
    Phase 4 -.-> Phase 5
```

---

## Phase 1: Security & Tenant Isolation (Day 1 - Critical Hotfixes)

### Issue 1.1: Broken Object Level Authorization (BOLA/IDOR) in Portfolio Summary
- **Target File:** [`am-trade-api/src/main/java/am/trade/api/controller/PortfolioSummaryController.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-api/src/main/java/am/trade/api/controller/PortfolioSummaryController.java#L83-L125)
- **Root Cause:** The endpoint `@GetMapping("/by-owner/{ownerId}")` takes `ownerId` directly from the URL path and queries portfolios without validating that `ownerId` matches the authenticated user in `UserContext`. Similarly, `getPortfolioSummary(@PathVariable String portfolioId)` returns the portfolio without verifying owner ownership.

#### Code Changes:
Replace lines 83–109 in `PortfolioSummaryController.java`:

```java
// BEFORE
@GetMapping("/by-owner/{ownerId}")
public ResponseEntity<?> getPortfolioSummaryByOwnerIdFallback(
        @Parameter(description = "Owner ID") @PathVariable String ownerId,
        @RequestParam(name = "includePaper", defaultValue = "false") boolean includePaper) {
    try {
        log.info("Fetching portfolio summaries for ownerId: {}", ownerId);
        List<PortfolioModel> portfolioSummaries = portfolioSummaryService.getPortfolioSummariesByOwnerId(ownerId);
...

// AFTER
@GetMapping("/by-owner/{ownerId}")
public ResponseEntity<?> getPortfolioSummaryByOwnerIdFallback(
        @Parameter(description = "Owner ID") @PathVariable String ownerId,
        @RequestParam(name = "includePaper", defaultValue = "false") boolean includePaper) {
    String authenticatedUserId = UserContext.getUserIdOrThrow();
    if (!authenticatedUserId.equals(ownerId)) {
        log.warn("Security Alert: User {} attempted to access portfolios of owner {}", authenticatedUserId, ownerId);
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .status("FORBIDDEN")
                        .code(403)
                        .message("Access denied: You cannot view portfolios belonging to another user")
                        .path("/v1/portfolio-summary/by-owner/" + ownerId)
                        .build());
    }
    try {
        log.info("Fetching portfolio summaries for authenticated ownerId: {}", authenticatedUserId);
        List<PortfolioModel> portfolioSummaries = portfolioSummaryService.getPortfolioSummariesByOwnerId(authenticatedUserId);
...
```

In `getPortfolioSummary` (around line 124):
```java
// AFTER
PortfolioModel portfolioSummary = portfolioSummaryService.getPortfolioSummary(portfolioId);
String authenticatedUserId = UserContext.getUserIdOrThrow();
if (portfolioSummary.getOwnerId() != null && !portfolioSummary.getOwnerId().equals(authenticatedUserId)) {
    log.warn("Security Alert: User {} attempted to access portfolio {} owned by {}", 
             authenticatedUserId, portfolioId, portfolioSummary.getOwnerId());
    return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
}
```

---

### Issue 1.2: Tenant Isolation in Notebook Items & Tags
- **Target File:** [`am-trade-api/src/main/java/am/trade/api/service/impl/NotebookServiceImpl.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-api/src/main/java/am/trade/api/service/impl/NotebookServiceImpl.java#L55-L100)
- **Root Cause:** `getNotebookItem`, `deleteNotebookItem`, `getNotebookTag`, and `deleteNotebookTag` fetch or delete items by ID without checking `item.getUserId().equals(UserContext.getUserIdOrThrow())`.

#### Code Changes:
Replace `getNotebookItem` (lines 55–60) and `deleteNotebookItem` (lines 83–100):

```java
// BEFORE
@Override
public NotebookItemResponse getNotebookItem(String itemId) {
    NotebookItem item = notebookItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Notebook item not found with ID: " + itemId));
    return convertToItemResponse(item);
}

@Override
public void deleteNotebookItem(String itemId) {
    if (!notebookItemRepository.existsById(itemId)) {
        throw new IllegalArgumentException("Notebook item not found with ID: " + itemId);
    }
    List<NotebookItem> children = notebookItemRepository.findByUserIdAndParentId(
            notebookItemRepository.findById(itemId).get().getUserId(),
            itemId);
    for (NotebookItem child : children) {
        deleteNotebookItem(child.getId());
    }
    notebookItemRepository.deleteById(itemId);
}

// AFTER
@Override
public NotebookItemResponse getNotebookItem(String itemId) {
    NotebookItem item = notebookItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Notebook item not found with ID: " + itemId));
    
    String currentUserId = UserContext.getUserIdOrThrow();
    if (!currentUserId.equals(item.getUserId())) {
        throw new SecurityException("Access denied: You do not have permission to view this notebook item");
    }
    return convertToItemResponse(item);
}

@Override
public void deleteNotebookItem(String itemId) {
    NotebookItem item = notebookItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Notebook item not found with ID: " + itemId));

    String currentUserId = UserContext.getUserIdOrThrow();
    if (!currentUserId.equals(item.getUserId())) {
        throw new SecurityException("Access denied: You do not have permission to delete this notebook item");
    }

    // Find and delete all children recursively within the user's scope
    List<NotebookItem> children = notebookItemRepository.findByUserIdAndParentId(currentUserId, itemId);
    for (NotebookItem child : children) {
        deleteNotebookItem(child.getId());
    }

    notebookItemRepository.deleteById(itemId);
}
```

Apply identical ownership verification in `getNotebookTag` and `deleteNotebookTag` (lines 144–174):
```java
@Override
public NotebookTagResponse getNotebookTag(String tagId) {
    NotebookTag tag = notebookTagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Notebook tag not found with ID: " + tagId));
    if (!UserContext.getUserIdOrThrow().equals(tag.getUserId())) {
        throw new SecurityException("Access denied: You do not have permission to view this tag");
    }
    return convertToTagResponse(tag);
}

@Override
public void deleteNotebookTag(String tagId) {
    NotebookTag tag = notebookTagRepository.findById(tagId)
            .orElseThrow(() -> new IllegalArgumentException("Notebook tag not found with ID: " + tagId));
    if (!UserContext.getUserIdOrThrow().equals(tag.getUserId())) {
        throw new SecurityException("Access denied: You do not have permission to delete this tag");
    }
    notebookTagRepository.deleteById(tagId);
}
```

---

### Issue 1.3: Tenant Isolation in Journal Templates
- **Target File:** [`am-trade-api/src/main/java/am/trade/api/service/impl/JournalTemplateServiceImpl.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-api/src/main/java/am/trade/api/service/impl/JournalTemplateServiceImpl.java#L60-L146)
- **Root Cause:**
  1. `getTemplate` returns any template regardless of creator.
  2. `updateTemplate` only checks ownership if `isSystemTemplate == true`, allowing any user to edit another user's custom template.
  3. `getAllTemplates` returns `templateRepository.findAll()`, leaking all private templates.

#### Code Changes:
Update `getTemplate` (lines 60–65):
```java
@Override
public JournalTemplateResponse getTemplate(String templateId, String userId) {
    JournalTemplate template = findTemplateById(templateId);
    boolean isSystem = Boolean.TRUE.equals(template.getIsSystemTemplate());
    if (!isSystem && !Objects.equals(template.getCreatedBy(), userId)) {
        throw new SecurityException("Access denied: You cannot view private templates created by another user");
    }
    return convertToResponse(template, userId);
}
```

Update `updateTemplate` (lines 74–79):
```java
// BEFORE
if (Boolean.TRUE.equals(template.getIsSystemTemplate()) &&
        !Objects.equals(template.getCreatedBy(), userId)) {
    throw new IllegalArgumentException("Cannot update system template");
}

// AFTER
if (Boolean.TRUE.equals(template.getIsSystemTemplate())) {
    if (!Objects.equals(template.getCreatedBy(), userId)) {
        throw new SecurityException("Cannot update system template");
    }
} else if (!Objects.equals(template.getCreatedBy(), userId)) {
    throw new SecurityException("Cannot update template created by another user");
}
```

Update `getAllTemplates` (lines 129–146):
Replace global queries with queries filtered by `(isSystemTemplate == true OR createdBy == userId)`:
```java
@Override
public List<JournalTemplateResponse> getAllTemplates(String userId, JournalTemplateCategory category, String searchQuery) {
    List<JournalTemplate> templates;
    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
        templates = templateRepository.findVisibleTemplatesByName(searchQuery, userId);
    } else if (category != null) {
        templates = templateRepository.findVisibleTemplatesByCategory(category, userId);
    } else {
        templates = templateRepository.findVisibleTemplates(userId);
    }
    return templates.stream().map(t -> convertToResponse(t, userId)).collect(Collectors.toList());
}
```

Add query methods to `JournalTemplateRepository.java`:
```java
@Query("{ $or: [ { 'isSystemTemplate': true }, { 'createdBy': ?0 } ] }")
List<JournalTemplate> findVisibleTemplates(String userId);

@Query("{ 'category': ?0, $or: [ { 'isSystemTemplate': true }, { 'createdBy': ?1 } ] }")
List<JournalTemplate> findVisibleTemplatesByCategory(JournalTemplateCategory category, String userId);

@Query("{ 'name': { $regex: ?0, $options: 'i' }, $or: [ { 'isSystemTemplate': true }, { 'createdBy': ?1 } ] }")
List<JournalTemplate> findVisibleTemplatesByName(String nameRegex, String userId);
```

---

### Issue 1.4: Map `SecurityException` to HTTP 403 in Global Exception Handler
- **Target File:** [`am-trade-exceptions/src/main/java/am/trade/exceptions/handler/GlobalExceptionHandler.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-exceptions/src/main/java/am/trade/exceptions/handler/GlobalExceptionHandler.java)
- **Root Cause:** When `SecurityException` is thrown, it falls into `handleGenericException` and returns HTTP 500.

#### Code Changes:
Add a dedicated handler method in `GlobalExceptionHandler.java`:
```java
@ExceptionHandler(SecurityException.class)
public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, HttpServletRequest request) {
    log.warn("Access denied on {}: {}", request.getRequestURI(), ex.getMessage());
    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error(HttpStatus.FORBIDDEN.getReasonPhrase())
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .traceId(UUID.randomUUID().toString())
            .build();
    return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
}
```

---

### Issue 1.5: Secure Fallbacks in `SecurityConfig` and CORS
- **Target Files:**
  - [`am-trade-api/src/main/java/am/trade/api/config/SecurityConfig.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-api/src/main/java/am/trade/api/config/SecurityConfig.java#L25)
  - [`am-trade-app/src/main/resources/application.yml`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-app/src/main/resources/application.yml#L13,L87)

#### Code Changes:
1. In `SecurityConfig.java`: Change `matchIfMissing` to `false` to fail closed:
```java
// BEFORE
@ConditionalOnProperty(prefix = "am.security", name = "enabled", havingValue = "false", matchIfMissing = true)

// AFTER
@ConditionalOnProperty(prefix = "am.security", name = "enabled", havingValue = "false", matchIfMissing = false)
```
2. In `application.yml` (line 13): Remove wildcard `https://*`:
```yaml
# BEFORE
  cors:
    allowed-origin-patterns: ${CORS_ALLOWED_ORIGINS:http://localhost:*,https://localhost:*,https://*.replit.dev,https://*}

# AFTER
  cors:
    allowed-origin-patterns: ${CORS_ALLOWED_ORIGINS:http://localhost:*,https://localhost:*,https://*.replit.dev}
```
3. In `application.yml` (line 87): Restrict trusted deserialization packages:
```yaml
# BEFORE
      properties:
        spring.json.trusted.packages: "*"

# AFTER
      properties:
        spring.json.trusted.packages: "am.trade.kafka.model,am.trade.models.kafka,java.util,java.lang"
```

---

## Phase 2: Financial Calculation & State Integrity (Days 2–3)

### Issue 2.1: Fix Partial Exit P&L Multiplication Bug
- **Target File:** [`am-trade-services/src/main/java/am/trade/services/service/impl/TradeProcessingServiceImpl.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-services/src/main/java/am/trade/services/service/impl/TradeProcessingServiceImpl.java#L814-L845)
- **Root Cause:** Line 820 and Line 825 multiply by `entryInfo.getQuantity()`. In a partial exit, `exitInfo.getQuantity() < entryInfo.getQuantity()`. Realized P&L can **only** be calculated on the shares that were actually closed.

#### Code Changes:
```java
// BEFORE (lines 814-845)
BigDecimal profitLoss;
if (tradePositionType == TradePositionType.LONG) {
    profitLoss = exitInfo.getPrice()
            .subtract(entryInfo.getPrice())
            .multiply(BigDecimal.valueOf(entryInfo.getQuantity() != null ? entryInfo.getQuantity() : 0));
} else {
    profitLoss = entryInfo.getPrice()
            .subtract(exitInfo.getPrice())
            .multiply(BigDecimal.valueOf(entryInfo.getQuantity() != null ? entryInfo.getQuantity() : 0));
}

BigDecimal entryFees = entryInfo.getFees() != null ? entryInfo.getFees() : BigDecimal.ZERO;
BigDecimal exitFees = exitInfo.getFees() != null ? exitInfo.getFees() : BigDecimal.ZERO;
BigDecimal totalFees = entryFees.add(exitFees);
profitLoss = profitLoss.subtract(totalFees);

// AFTER
BigDecimal profitLoss;
int closedQty = exitInfo.getQuantity() != null ? exitInfo.getQuantity() : 0;
int totalEntryQty = entryInfo.getQuantity() != null && entryInfo.getQuantity() > 0 ? entryInfo.getQuantity() : closedQty;

// Guard against multiplying by 0
if (closedQty <= 0) {
    return TradeMetrics.builder()
            .profitLoss(BigDecimal.ZERO)
            .profitLossPercentage(BigDecimal.ZERO)
            .returnOnEquity(BigDecimal.ZERO)
            .build();
}

if (tradePositionType == TradePositionType.LONG) {
    // For LONG: (Exit Price - Entry Price) * Closed Quantity
    profitLoss = exitInfo.getPrice()
            .subtract(entryInfo.getPrice())
            .multiply(BigDecimal.valueOf(closedQty));
} else {
    // For SHORT: (Entry Price - Exit Price) * Closed Quantity
    profitLoss = entryInfo.getPrice()
            .subtract(exitInfo.getPrice())
            .multiply(BigDecimal.valueOf(closedQty));
}

// Proportional entry fees + full exit fees for the sold portion
BigDecimal entryFees = entryInfo.getFees() != null ? entryInfo.getFees() : BigDecimal.ZERO;
BigDecimal proportionalEntryFees = totalEntryQty > 0 
        ? entryFees.multiply(BigDecimal.valueOf(closedQty)).divide(BigDecimal.valueOf(totalEntryQty), 4, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

BigDecimal exitFees = exitInfo.getFees() != null ? exitInfo.getFees() : BigDecimal.ZERO;
BigDecimal totalFees = proportionalEntryFees.add(exitFees);
profitLoss = profitLoss.subtract(totalFees);

// Cost basis of the closed portion (for P&L percentage)
BigDecimal costBasisOfClosedPortion = entryInfo.getPrice().multiply(BigDecimal.valueOf(closedQty));
BigDecimal profitLossPercentage = costBasisOfClosedPortion.compareTo(BigDecimal.ZERO) > 0
        ? profitLoss.divide(costBasisOfClosedPortion, DECIMAL_SCALE, ROUNDING_MODE).multiply(BigDecimal.valueOf(100))
        : BigDecimal.ZERO;
```

---

### Issue 2.2: Sanitize / Recompute Trade Metrics on `addTrade`
- **Target File:** [`am-trade-api/src/main/java/am/trade/api/service/impl/TradeApiServiceImpl.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-api/src/main/java/am/trade/api/service/impl/TradeApiServiceImpl.java#L123-L145)
- **Root Cause:** Incoming `tradeDetails.getMetrics()` is accepted verbatim and applied to portfolio totals without validation.

#### Code Changes:
In `addTrade` (lines 123–140), recompute metrics if `entryInfo` and `exitInfo` exist:
```java
// Validate required fields
tradeValidator.validateTrade(tradeDetails);

// Ensure metrics are computed server-side, not trusted from client
boolean isClosed = tradeDetails.getExitInfo() != null 
        && tradeDetails.getExitInfo().getQuantity() != null
        && tradeDetails.getExitInfo().getPrice() != null;

if (isClosed && tradeDetails.getEntryInfo() != null && tradeDetails.getEntryInfo().getPrice() != null) {
    TradeMetrics verifiedMetrics = tradeProcessingService.calculateTradeMetrics(
            tradeDetails.getEntryInfo(),
            tradeDetails.getExitInfo(),
            tradeDetails.getTradePositionType()
    );
    tradeDetails.setMetrics(verifiedMetrics);
    
    // Set status based on genuine PnL
    if (verifiedMetrics.getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
        tradeDetails.setStatus(TradeStatus.WIN);
    } else if (verifiedMetrics.getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
        tradeDetails.setStatus(TradeStatus.LOSS);
    } else {
        tradeDetails.setStatus(TradeStatus.BREAK_EVEN);
    }
} else {
    // Open trade — zero out realized PnL
    tradeDetails.setStatus(TradeStatus.OPEN);
    if (tradeDetails.getMetrics() != null) {
        tradeDetails.getMetrics().setProfitLoss(BigDecimal.ZERO);
        tradeDetails.getMetrics().setProfitLossPercentage(BigDecimal.ZERO);
    }
}

// Now save the sanitized trade
TradeDetails savedTrade = tradeDetailsService.saveTradeDetails(tradeDetails);
```

---

### Issue 2.3: Prevent Deleted Trade Resurrections
- **Target File:** [`am-trade-services/src/main/java/am/trade/services/service/impl/TradeProcessingServiceImpl.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-services/src/main/java/am/trade/services/service/impl/TradeProcessingServiceImpl.java#L129-L150)
- **Root Cause:** `processTradeDetailsWithObjects` loads `existingPortfolio.get().getTradeIds()` and does `addAll`, which re-inserts the deleted trade ID that was just removed from MongoDB.

#### Code Changes:
```java
// BEFORE (lines 136-147)
Optional<PortfolioModel> existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId);
Set<String> uniqueTradeIds = new HashSet<>();

if (existingPortfolio.isPresent()) {
    List<String> existingTrades = existingPortfolio.get().getTradeIds();
    if (existingTrades != null && !existingTrades.isEmpty()) {
        uniqueTradeIds.addAll(existingTrades);
    }
}
uniqueTradeIds.addAll(newTradeIds);
List<String> allTradeIds = new ArrayList<>(uniqueTradeIds);

// AFTER
// When processing an explicit list of remaining trade objects (e.g. after a deletion or full sync),
// the provided list IS the definitive source of truth for the portfolio. Do NOT re-merge deleted IDs!
List<String> allTradeIds = trades.stream()
        .map(TradeDetails::getTradeId)
        .filter(Objects::nonNull)
        .distinct()
        .collect(Collectors.toList());

processTradeDetailsAndGetPortfolio(allTradeIds, portfolioId, userId);
```

---

### Issue 2.4: Fix Double Counting of Trades on Exit in `OmsFillJournalService`
- **Target File:** [`am-trade-kafka/src/main/java/am/trade/kafka/service/OmsFillJournalService.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-kafka/src/main/java/am/trade/kafka/service/OmsFillJournalService.java#L122-L133)
- **Root Cause:** When `applySell` updates an open position to closed (`WIN`/`LOSS`), it calls `tradeProcessingService.applyTradesDelta(List.of(saved), ...)` which adds `+1` to `totalTrades`. `totalTrades` was already incremented when the position was opened via `applyBuy`.

#### Code Changes:
In `OmsFillJournalService.java`, call `applyTradeUpdateDelta` instead of `applyTradesDelta` when modifying an existing trade:
```java
// BEFORE (line 132)
tradeProcessingService.applyTradesDelta(List.of(saved), saved.getPortfolioId(), saved.getUserId());

// AFTER
// Passing open (the state before exit) and saved (the state after exit) computes the exact delta:
// totalTradesDelta = 0, openPositionsDelta = -1, winningDelta = +1, profitDelta = +X.
tradeProcessingService.applyTradeUpdateDelta(open, saved, saved.getPortfolioId(), saved.getUserId());
```

---

## Phase 3: Order Management Service (OMS) Concurrency (Days 3–4)

### Issue 3.1: Atomic Wallet Balance Updates via MongoDB `$inc`
- **Target File:** [`am-oms/am_oms/services.py`](file:///c:/Asrax/Asrax/am-trade-management/am-oms/am_oms/services.py#L202-L211,L467-L484,L485-L510)
- **Root Cause:** Wallet documents are read into memory and written back via `update_one({"walletId": wid}, {"$set": w})`, causing race conditions and lost balance updates.

#### Code Changes:
Replace non-atomic helper functions with atomic MongoDB `$inc` updates:

```python
# In am_oms/services.py

async def _atomic_reserve_buy(self, wallet_id: str, cost: Decimal) -> bool:
    """Atomically deduct from available and add to reserved if sufficient available cash exists."""
    cost_str = money(cost)
    result = await self.db.wallets.update_one(
        {
            "walletId": wallet_id,
            "available": {"$gte": cost_str}
        },
        {
            "$inc": {
                "available": -float(cost),
                "reserved": float(cost)
            },
            "$set": {"updatedAt": now()}
        }
    )
    return result.modified_count == 1

async def _atomic_release_buy(self, wallet_id: str, cost: Decimal) -> bool:
    """Atomically return cash from reserved back to available."""
    result = await self.db.wallets.update_one(
        {
            "walletId": wallet_id,
            "reserved": {"$gte": money(cost)}
        },
        {
            "$inc": {
                "available": float(cost),
                "reserved": -float(cost)
            },
            "$set": {"updatedAt": now()}
        }
    )
    return result.modified_count == 1
```

---

### Issue 3.2: Multi-Replica Compare-And-Swap Order Execution
- **Target File:** [`am-oms/am_oms/services.py`](file:///c:/Asrax/Asrax/am-trade-management/am-oms/am_oms/services.py#L509-L535)
- **Root Cause:** When `try_match_order` matches an order, it sets `status = FILLED` without checking that `status` is still `ACCEPTED` in MongoDB.

#### Code Changes:
Use an atomic CAS check when transitioning from `ACCEPTED` to `FILLED`:

```python
# In _complete_fill in am_oms/services.py:
# Atomically claim the fill transition. If another replica already filled/cancelled it, modified_count == 0.
claim = await self.db.orders.update_one(
    {"orderId": d["orderId"], "status": OrderStatus.ACCEPTED.value},
    {"$set": {
        "status": OrderStatus.FILLED.value,
        "fillPrice": money(fill_px),
        "filledQuantity": money(qty),
        "updatedAt": t
    }}
)

if claim.modified_count == 0:
    log.info("Order %s was already processed by another worker; skipping duplicate fill", d["orderId"])
    return False

# Now safe to update position, balances, and publish fill event
```

---

### Issue 3.3: Convert `AIOKafkaProducer` into a Persistent Singleton
- **Target File:** [`am-oms/am_oms/clients.py`](file:///c:/Asrax/Asrax/am-trade-management/am-oms/am_oms/clients.py#L52-L65,L143-L161)
- **Root Cause:** `publish_fill` instantiates, connects, and stops an `AIOKafkaProducer` on every single message.

#### Code Changes:
```python
# In am_oms/clients.py

class Clients:
    def __init__(self, settings: Settings | None = None, ...):
        self.s = settings or get_settings()
        self.http = ...
        self.matcher_http = ...
        self._producer = None

    async def get_producer(self):
        if not self.s.kafka_enabled:
            return None
        if self._producer is None:
            from aiokafka import AIOKafkaProducer
            self._producer = AIOKafkaProducer(
                bootstrap_servers=self.s.kafka_bootstrap,
                security_protocol=self.s.kafka_security,
                sasl_mechanism=self.s.kafka_sasl if self.s.kafka_username else None,
                sasl_plain_username=self.s.kafka_username or None,
                sasl_plain_password=self.s.kafka_password or None,
            )
            await self._producer.start()
        return self._producer

    async def close(self):
        if self._producer:
            await self._producer.stop()
            self._producer = None

    async def publish_fill(self, event: dict) -> None:
        if not self.s.kafka_enabled:
            log.info("kafka_skipped", extra={"orderId": event.get("orderId")})
            return
        
        producer = await self.get_producer()
        try:
            await producer.send_and_wait(self.s.fills_topic, json.dumps(event).encode())
        except Exception:
            log.error("fills_publish_failed", extra={"orderId": event.get("orderId")}, exc_info=True)
```

In `am-oms/am_oms/main.py`:
```python
@asynccontextmanager
async def lifespan(_: FastAPI):
    await init_db()
    clients = get_clients()
    task = asyncio.create_task(matcher_loop(Oms(get_database(), clients)))
    try:
        yield
    finally:
        task.cancel()
        await clients.close()
        await close_db()
```

---

## Phase 4: Event Reliability & Code Hygiene (Day 4)

### Issue 4.1: Synchronous Delivery Confirmation for Portfolio Sync Events
- **Target File:** [`am-trade-kafka/src/main/java/am/trade/kafka/producer/KafkaProducerService.java`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-kafka/src/main/java/am/trade/kafka/producer/KafkaProducerService.java#L65-L85)

#### Code Changes:
Wait for Kafka ack with a timeout instead of fire-and-forget:
```java
// BEFORE
CompletableFuture<SendResult<String, Object>> future = 
    kafkaTemplate.send(holdingUpdateTopic, event.getId(), event);
future.whenComplete((result, ex) -> { ... });

// AFTER
try {
    SendResult<String, Object> result = kafkaTemplate.send(holdingUpdateTopic, event.getId(), event)
            .get(5, java.util.concurrent.TimeUnit.SECONDS);
    log.info("Holding update event confirmed by Kafka broker for userId: {}, offset: {}", 
             event.getUserId(), result.getRecordMetadata().offset());
} catch (Exception e) {
    log.error("Critical: Failed to publish holding update to Kafka for userId: {}", event.getUserId(), e);
    throw new org.springframework.kafka.KafkaException("Failed to publish holding update to topic " + holdingUpdateTopic, e);
}
```

In `TradeConsumerService.java` (lines 221–224):
```java
// BEFORE
} catch (Exception e) {
    log.error("Failed to publish batch holding update. Trades are still saved.", e);
}

// AFTER
} catch (Exception e) {
    log.error("Failed to publish holding update for trade batch. Failing transaction to trigger retry.", e);
    throw e; // Allows Kafka DefaultErrorHandler to retry and eventually DLQ
}
```

---

### Issue 4.2: Fix Jackson Configuration Nesting in `application.yml`
- **Target File:** [`am-trade-app/src/main/resources/application.yml`](file:///c:/Asrax/Asrax/am-trade-management/am-trade-app/src/main/resources/application.yml#L67-L73)
- **Root Cause:** `jackson:` is indented under `spring.data:`. It must be under `spring:`.

#### Changes:
```yaml
# BEFORE (lines 61-73)
  data:
    mongodb:
      host: ${MONGODB_HOST:localhost}
      port: ${MONGODB_PORT:27017}
      database: ${MONGODB_DATABASE:trade}
      auto-index-creation: false
    jackson:
      default-property-inclusion: non_null
      serialization:
        write-dates-as-timestamps: false

# AFTER
  data:
    mongodb:
      host: ${MONGODB_HOST:localhost}
      port: ${MONGODB_PORT:27017}
      database: ${MONGODB_DATABASE:trade}
      auto-index-creation: false
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
    date-format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
```

---

### Issue 4.3: Convert UTF-16LE Files to Clean UTF-8
- **Corrupted Files:**
  1. `am-trade-sdk-python/sdk/client/trade_client.py`
  2. `am-trade-sdk-python/sdk/models/__init__.py`
  3. `README.md`
  4. `preprod_job_logs.txt`

#### PowerShell Command to Fix:
```powershell
$files = @(
    "c:\Asrax\Asrax\am-trade-management\am-trade-sdk-python\sdk\client\trade_client.py",
    "c:\Asrax\Asrax\am-trade-management\am-trade-sdk-python\sdk\models\__init__.py",
    "c:\Asrax\Asrax\am-trade-management\README.md"
)

foreach ($f in $files) {
    if (Test-Path $f) {
        $content = Get-Content $f -Encoding Unicode -Raw
        [System.IO.File]::WriteAllText($f, $content, [System.Text.UTF8Encoding]::new($false))
        Write-Host "Converted $f to UTF-8 without BOM"
    }
}
```

---

### Issue 4.4: Git Cleanup & Maven Lombok Update
1. **Remove tracked build artifacts and logs from Git:**
```bash
git rm --cached am-trade-sdk-python/dist/*.whl
git rm --cached am-trade-sdk-python/dist/*.tar.gz
git rm -r --cached am-trade-sdk-python/am_trade_sdk.egg-info/
git rm --cached am-trade-app/logs/am-trade-management.log.*.gz
git rm --cached preprod_job_logs.txt
```
2. **Update Lombok in root `pom.xml` (line 32):**
```xml
<!-- BEFORE -->
<lombok.version>1.18.30</lombok.version>

<!-- AFTER (supports JDK 17 through JDK 25) -->
<lombok.version>1.18.36</lombok.version>
```

---

## Phase 5: Future Architecture & Long-Term Engineering Roadmap (Roadmap to 9.5+/10)

This phase represents the architectural and operational evolution required to transition the application from a repaired service to a resilient, enterprise-grade multi-asset trading platform.

### 5.1 Consolidate the Service Layer & Deconstruct God-Classes
- **Target Modules:** `am-trade-api` and `am-trade-services`
- **Current Problem:** 
  1. `am-trade-api` houses massive implementations: `TradeApiServiceImpl` (1,319 lines), `TradeJournalServiceImpl` (1,324 lines), and `PortfolioApiServiceImpl` (1,100+ lines).
  2. `am-trade-services` contains parallel duplicate service interfaces and implementations (`TradeSummaryServiceImpl` requiring `@Service("serviceLayerTradeSummaryService")` to prevent bean collisions).
- **Architecture Goal:**
  - Strip `am-trade-api` down to pure REST controllers, request/response DTOs, and OpenAPI annotations.
  - Migrate all domain logic into `am-trade-services`.
  - Decompose the 1,300-line god-classes into single-responsibility domain services:
    - `TradeLifecycleService`: Handles trade creation, execution lifecycle, and transitions.
    - `TradeTaxAndFeeCalculator`: Encapsulates broker charges, STT, exchange turnover fees, and GST.
    - `TradeOwnershipValidator`: Handles authorization and multi-tenant security assertions.
    - `TradePortfolioSyncDispatcher`: Dispatches events to Kafka with outbox guarantees.

```
CURRENT (Anti-pattern):
am-trade-api ──────► [TradeController] ──► [TradeApiServiceImpl (1319 LOC)] ──► [MongoDB Repositories]
am-trade-services ──► [TradeServiceImpl (Parallel Abandoned Class)]

FUTURE (Clean Architecture):
am-trade-api ──────► [TradeController] (Pure HTTP/DTO Layer)
                            │
                            ▼
am-trade-services ──► [TradeLifecycleService] ──► [TradeTaxCalculator]
                            │                 ──► [TradeOwnershipValidator]
                            ▼
                      [TradePersistenceService] ──► [MongoDB Repositories]
```

---

### 5.2 MongoDB Data Normalization & High-Performance Aggregations
- **Target Entities:** `PortfolioEntity.java`, `TradeDetailsEntity.java`
- **Current Problem:**
  `PortfolioEntity` embeds `List<String> tradeIds`. As portfolios grow to tens of thousands of trades, the document exceeds normal BSON sizes, triggers repeated in-place disk allocations, and eventually crashes against MongoDB’s 16MB document cap. Furthermore, portfolio metrics are calculated by pulling thousands of trade documents into JVM memory.
- **Implementation Strategy:**
  1. **Schema Normalization:** Drop `tradeIds` from `PortfolioEntity`. The relationship is already naturally modeled via `TradeDetailsEntity.portfolioId` with an index:
     ```java
     // In TradeDetailsEntity.java
     @CompoundIndex(name = "idx_portfolio_status_entryTs", def = "{'portfolioId': 1, 'status': 1, 'entryInfo.timestamp': -1}")
     ```
  2. **Native MongoDB Aggregation Pipelines:** Replace in-JVM loops with native aggregation pipelines using Spring Data Mongo’s `Aggregation` API. Computations for `totalProfit`, `totalLoss`, `winRate`, `lossRate`, and `expectancy` are pushed to the database engine in a single query:
     ```java
     MatchOperation matchStage = match(Criteria.where("portfolioId").is(portfolioId).and("userId").is(userId));
     GroupOperation groupStage = group("portfolioId")
             .count().as("totalTrades")
             .sum(ConditionalOperators.when(Criteria.where("status").is("WIN")).then(1).otherwise(0)).as("winningTrades")
             .sum(ConditionalOperators.when(Criteria.where("status").is("LOSS")).then(1).otherwise(0)).as("losingTrades")
             .sum("metrics.profitLoss").as("netProfitLoss")
             .sum(ConditionalOperators.when(Criteria.where("status").is("WIN")).thenValueOf("metrics.profitLoss").otherwise(0)).as("totalProfit")
             .sum(ConditionalOperators.when(Criteria.where("status").is("LOSS")).thenValueOf("metrics.profitLoss").otherwise(0)).as("totalLoss");

     Aggregation aggregation = newAggregation(matchStage, groupStage);
     AggregationResults<PortfolioMetricsDTO> result = mongoTemplate.aggregate(aggregation, "trade_details", PortfolioMetricsDTO.class);
     ```

---

### 5.3 Automated Testing Infrastructure & Testcontainers Setup (80%+ Target)
- **Current Problem:** 469 Java source files with only 17 test files (<5% coverage). `am-trade-services` has zero unit tests. `am-oms` cannot run tests due to missing dependencies.
- **Implementation Strategy:**
  1. **Testcontainers Integration:** Add `testcontainers-bom` to root `pom.xml`:
     ```xml
     <dependency>
         <groupId>org.testcontainers</groupId>
         <artifactId>mongodb</artifactId>
         <scope>test</scope>
     </dependency>
     <dependency>
         <groupId>org.testcontainers</groupId>
         <artifactId>kafka</artifactId>
         <scope>test</scope>
     </dependency>
     ```
  2. **Parametric Financial Test Suite:** Implement JUnit 5 parameterized tests for edge cases:
     - Multi-leg option trades (straddles, strangles, spreads).
     - Partial exits with unequal lots (e.g. 3 entries of 25 shares, 2 exits of 35 and 40 shares).
     - Short selling with square-offs and flips.
     - Zero-capital and negative-capital portfolios.
  3. **OMS Python Test Suite:** Fix `pyproject.toml` and `pytest.ini` in `am-oms` with `pytest-asyncio` and `testcontainers-python` to test MongoDB and Kafka under simulated high-concurrency order placement.

---

### 5.4 Enterprise Event-Driven Architecture: Transactional Outbox Pattern
- **Current Problem:** Even with synchronous sending, if the database commits and the Kafka broker crashes before ack, the service must either throw and cause duplicate work or swallow and lose events.
- **Implementation Strategy:** Implement the **Transactional Outbox Pattern**:
  1. Create an `outbox_events` collection in MongoDB:
     ```json
     {
       "_id": "uuid",
       "aggregateType": "TRADE",
       "aggregateId": "trade-123",
       "eventType": "PORTFOLIO_SYNC",
       "payload": "{ ... }",
       "status": "PENDING",
       "createdAt": "2026-11-01T12:00:00Z",
       "retryCount": 0
     }
     ```
  2. In the trade service, write both the trade record and the outbox event in the same MongoDB session/transaction.
  3. A lightweight poller (or Debezium Mongo Change Streams) reads `PENDING` outbox records, publishes to Kafka, and marks them `PROCESSED`. If Kafka is down, events queue safely in MongoDB without stalling API requests.

---

### 5.5 Advanced Market Mechanics & Tax Engine
- **Tax-Lot Matching Strategies:**
  Implement configurable tax-lot matching engines in `am-trade-services`:
  - **FIFO (First In, First Out)** (Current default)
  - **LIFO (Last In, First Out)**
  - **Highest In, First Out (HIFO)** (Tax minimization)
  - **Specific Lot Identification (SpecID)**
- **Wash Sale Rule Detection:**
  Flag trades where a security is sold at a loss and repurchased within 30 days, adjusting the cost basis of the new position automatically.
- **Corporate Actions Processing:**
  Add Kafka consumers for corporate actions events (stock splits, reverse splits, bonus shares, mergers), updating historical trade quantities and prices proportionally.

---

### 5.6 Unified Multi-Language CI/CD Matrix
- **Target File:** `.github/workflows/ci.yml`
- **Current Problem:** CI only tests Java files. Changes to `am-oms`, Python SDK, or Helm charts deploy without automated pull request checks.
- **Implementation Strategy:** Update `ci.yml` to run a parallel matrix build:
  ```yaml
  name: AM Trade Management CI
  on:
    push:
      branches: [main, develop]
    pull_request:
      branches: [main, develop]

  jobs:
    test-java:
      name: Test Java Backend
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            java-version: '17'
            distribution: 'temurin'
            cache: maven
        - run: mvn clean verify -DskipITs=false

    test-python-oms:
      name: Test Python OMS
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: actions/setup-python@v5
          with:
            python-version: '3.12'
            cache: pip
        - working-directory: am-oms
          run: |
            pip install -r requirements.txt pytest pytest-asyncio
            pytest

    lint-helm:
      name: Lint Helm Charts
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - uses: azure/setup-helm@v4
        - run: helm lint helm/
  ```

---

## Testing & Verification Checklist

| Test Case | Method | Expected Result |
|---|---|---|
| **Partial Exit P&L** | Submit trade: Buy 100 @ ₹100, Sell 20 @ ₹110 | Realized P&L is `(110-100)*20 = ₹200` (not ₹2,000) |
| **Forged P&L** | POST `/v1/trades/details` with `metrics.profitLoss: 9999999` | Server ignores client metrics and computes real P&L |
| **Cross-User Portfolio** | Call `GET /v1/portfolio-summary/by-owner/{otherUserId}` | HTTP 403 Forbidden with security alert |
| **Notebook Item IDOR** | User A creates item; User B calls `GET` or `DELETE /v1/notebook/items/{id}` | HTTP 403 Forbidden |
| **Journal Template IDOR** | User A creates custom template; User B calls `PUT /v1/journal/templates/{id}` | HTTP 403 Forbidden |
| **Multi-Pod Matching** | Fire 20 limit orders matching simultaneously | Zero duplicate fills in DB; exactly 20 fills on Kafka |
| **Kafka Outage Recovery** | Stop Kafka; post trade batch to consumer | Consumer rolls back and retries; message sent to DLT |
| **File Encodings** | Run `file` or Python inspection on `trade_client.py` | Valid UTF-8 text file, no BOM |
| **Native Mongo Aggregation**| Query portfolio metrics for 10,000 trades | Sub-50ms execution via `$group` aggregation pipeline |
