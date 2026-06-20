# Trade API Documentation

**Base URL**: `/api/v1/trades`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeController.java`

This module is the core orchestrator for inserting and updating trades into the system.

---

### 1. Add a New Trade
*   **Method Name**: `addTrade` [POST `/details`]
*   **Lifecycle & Orchestration**: 
    1. The frontend JSON maps directly to a `TradeDetails` envelope. 
    2. The controller immediately delegates to `TradeApiServiceImpl.addTrade()`.
    3. The API Service utilizes a `TradeValidator` to ensure all necessary fields are present.
    4. An auto-generated UUID is assigned if the request doesn't have one.
    5. The model translates via `TradeDetailsMapper` to a `TradeDetailsEntity`.
*   **Database Resources**: Ultimately persists the data into **MongoDB** by instructing the `TradeDetailsServiceImpl` wrapper around `TradeDetailsRepository.save()`.
*   **External/Downstream Resources**: After saving, it triggers a "ripple effect" by calling `tradeProcessingService.processTradeDetails()`. In a full setup, this aggregation step frequently publishes messages to **Kafka Topics** representing a state change in the portfolio performance.

---

### 2. Update an Existing Trade
*   **Method Name**: `updateTrade` [PUT `/details/{tradeId}`]
*   **Lifecycle & Orchestration**: 
    1. Takes the updated `TradeDetails` JSON in the body and a target `tradeId` in the URL path.
    2. The `TradeApiServiceImpl.updateTrade()` first requests a lookup of the original trade via `TradeDetailsService.findModelByTradeId()`.
    3. Strict validation protects immutable fields: Symbol, Portfolio ID, Position Type, and Base Price cannot be modified in an update sequence. It will throw a `TradeFieldValidationException` if violated.
    4. Merges logic applies, specifically appending new image attachments to pre-existing `tradeAnalysisImages`.
*   **Database Resources**: Executes an update in **MongoDB** tracking the new fields via `TradeDetailsRepository.save()`.
*   **External/Downstream Resources**: The update automatically triggers the `tradeProcessingService` again to ensure the Portfolio's overall metrics are recalculated based on the updated exit/entry parameters.

---

### 3. Fetch Trade Details by Portfolio
*   **Method Name**: `getTradeDetailsByPortfolioAndSymbols` [GET `/details/portfolio/{portfolioId}`]
*   **Lifecycle & Orchestration**:
    1. Returns a list of Trade details tailored to the UI components.
    2. Optional filtering is piped directly from the controller via `@RequestParam(required = false) List<String> symbols`.
    3. Defers entirely to `TradeManagementService.getTradesBySymbols()`.
*   **Database Resources**: Initiates a read-only **MongoDB** `find()` operation via `TradeDetailsRepository`. 

---

### 4. Advanced Trade Filtering
*   **Method Name**: `getTradesByFilters` [GET `/filter`]
*   **Lifecycle & Orchestration**:
    1. Designed for complex Data Tables. Retrieves a paginated list of `TradeDetails`.
    2. Controller accepts complex query parameters including multiple combinations of `portfolioIds`, `statuses`, `date ranges` (ISO parsed), and `strategies`.
*   **Database Resources**: Invokes complex, paginated `MongoTemplate` or customized repository queries in **MongoDB** to efficiently query large clusters of trades. Does not impact outside services.

---

### 5. Batch Addition/Updates
*   **Method Name**: `addOrUpdateTrades` [POST `/details/batch`]
*   **Lifecycle & Orchestration**:
    1. Allows passing an array `List<TradeDetails>` for bulk ingestion.
    2. Validates every item sequentially before delegating to `tradeDetailsService.saveAllTradeDetails()`.
*   **Database Resources**: Maximizes throughput via **MongoDB** batch/bulk operations rather than hitting single `save()` cycles in a loop.
