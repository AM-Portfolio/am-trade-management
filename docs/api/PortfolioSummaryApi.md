# Portfolio Summary API Documentation

**Base URL**: `/api/v1/portfolio-summary`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/PortfolioSummaryController.java`

This module provides high-level aggregation metrics, allowing users to measure portfolio performance, asset allocation, and overall trading success across multiple instruments.

---

### 1. Get Portfolio Summary
*   **Method Name**: `getPortfolioSummary` [GET `/{portfolioId}`]
*   **Lifecycle & Orchestration**: 
    1. Extracts `{portfolioId}` from the URL path.
    2. Directly delegates to `portfolioSummaryService.getPortfolioSummary()`.
    3. Handles potential `IllegalArgumentException` thrown by the backend and resolves it to a clean HTTP 400 response.
*   **Database Resources**: Initiates a lookup within **MongoDB**, retrieving the core `Portfolio` document which holds nested statistics (like `RiskMetrics`, `PerformanceMetrics`, etc.).
*   **External/Downstream Resources**: Read-only mechanism; relies heavily on aggregated document counts rather than processing heavy calculations on the fly. 

---

### 2. Get Asset Allocation
*   **Method Name**: `getAssetAllocation` [GET `/{portfolioId}/asset-allocation`]
*   **Lifecycle & Orchestration**: 
    1. Requests the breakdown of capital allocation across different financial instruments.
    2. Calls `portfolioSummaryService.getAssetAllocation()`.
*   **Database Resources**: Expects **MongoDB** to execute a mapped query across trade entities related to the portfolio, grouping by ticker symbol/instrument type to calculate capital distribution percentages.

---

### 3. Get Portfolio Performance
*   **Method Name**: `getPortfolioPerformance` [GET `/{portfolioId}/performance`]
*   **Lifecycle & Orchestration**: 
    1. Requires `startDate` and `endDate` via query parameters formatted as strict ISO strings (`YYYY-MM-DD`). 
    2. Pipes to `portfolioSummaryService.getPortfolioPerformance()`.
    3. Returns a map of `LocalDate` mapped to a double representing specific timeline values (perfect for generating frontend progress charts).
*   **Database Resources**: Invokes complex date-range queries within **MongoDB's** timeseries or aggregate projections. 

---

### 4. Compare Portfolios
*   **Method Name**: `comparePortfolios` [GET `/compare`]
*   **Lifecycle & Orchestration**: 
    1. Consumes a list query parameter `?portfolioIds=id1,id2,id3`.
    2. Delegates to `portfolioSummaryService.comparePortfolios()`.
    3. Returns a `Map<String, PortfolioModel>` hashing the requested IDs against their respective portfolio models.
*   **Database Resources**: Bulk-fetches multiple `Portfolio` records from **MongoDB**.

---

### 5. Fetch Portfolios by Owner
*   **Method Name**: `getPortfolioSummariesByOwnerId` [GET `/by-owner/{ownerId}`]
*   **Lifecycle & Orchestration**: 
    1. Tailored to retrieve all top-level portfolio shapes for a single logged-in user.
    2. Delegates to `portfolioSummaryService.getPortfolioSummariesByOwnerId()`.
*   **Database Resources**: Performs a specialized lookup in **MongoDB** filtering by the index tied to the root user representation.
