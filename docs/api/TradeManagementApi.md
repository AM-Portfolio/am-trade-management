# Trade Management API Documentation

**Base URL**: `/api/v1/trades`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeManagementController.java`

This module serves specifically as a calendar-driven retrieval system. While `TradeController` manages the heavy insertion/update states, this module focuses strictly on navigating history via logical financial timelines.

---

### 1. Fetch by Standard Calendars
*   **Method Names**: `getTradeDetailsByDay`, `getTradeDetailsByMonth`, `getTradeDetailsByQuarter`, `getTradeDetailsByFinancialYear`
*   **Lifecycle & Orchestration**: 
    1. Extracts specific timeline boundary integers (e.g., year, month, quarter) via query parameters. 
    2. Optional `{portfolioId}` parameter constrains the timeline locally.
    3. Defers lookup resolution immediately to `TradeManagementService`. 
*   **Database Resources**: Heavily relies on **MongoDB** date-query predicates, abstracting raw timestamps into mapped calendar buckets to retrieve arrays of `TradeDetails`.

---

### 2. Fetch by Custom Date Range
*   **Method Name**: `getTradeDetailsByDateRange` [GET `/calendar/custom`]
*   **Lifecycle & Orchestration**: 
    1. Validates `startDate` and `endDate` boundaries (ensuring start is before end to prevent failing backend executions).
    2. Defers to `tradeManagementService.getTradeDetailsByDateRange()`.
*   **Database Resources**: Scans execution boundaries within **MongoDB**.

---

### 3. Paginated Portfolio Drill-Down
*   **Method Name**: `getTradeDetailsByPortfolio` [GET `/portfolio-details/{portfolioId}`]
*   **Lifecycle & Orchestration**: 
    1. Pure REST pattern linking a distinct Portfolio to its child Trades.
    2. Takes a standard `Pageable` injection.
    3. Forwards directly to `TradeManagementService`.
*   **Database Resources**: Leverages highly optimized `Page<TradeDetails>` pagination structures in **MongoDB**. Allows the UI to lazy-load massive blocks of trade records efficiently.
