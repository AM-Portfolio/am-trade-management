# Trade Journal API Documentation

**Base URL**: `/api/v1/journal`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeJournalController.java`

This module is responsible for managing "Mental Models" and journaling capabilities. It connects a trader's emotional state, psychology data, and specific textual reasonings directly to executed trades.

---

### 1. Create a Journal Entry
*   **Method Name**: `createJournalEntry` [POST `/`]
*   **Lifecycle & Orchestration**: 
    1. Takes a `@Valid TradeJournalEntryRequest` as JSON.
    2. Immediately logs the incoming user identifier to trace intent.
    3. Delegates entirely to `tradeJournalService.createJournalEntry()`.
    4. Utilizes explicit `try/catch` catching `IllegalArgumentException` manually wrapping custom `ErrorResponse` constructs.
*   **Database Resources**: Invokes **MongoDB** insertions explicitly tied to `TradingNote` components nested inside the associated database tables.

---

### 2. Fetch Single Entry
*   **Method Name**: `getJournalEntry` [GET `/{entryId}`]
*   **Lifecycle & Orchestration**: 
    1. Typical lookup calling `tradeJournalService.getJournalEntry(entryId)`.
    2. Robust error handling converting "not found" service logic to specific HTTP 404 definitions with details specifying exactly the ID that was missing.
*   **Database Resources**: Read request from **MongoDB** by the entry's UUID.

---

### 3. Fetch Entries by User
*   **Method Name**: `getJournalEntriesByUser` [GET `/user/{userId}`]
*   **Lifecycle & Orchestration**: 
    1. Handles bulk fetching mapped across a user dimension.
    2. Accepts a `Pageable` input mapping to `page`, `size`, and `sort` query params allowing the UI to paginate thousands of journal notes smoothly. 
    3. Forwards directly to `tradeJournalService.getJournalEntriesByUser()`.
*   **Database Resources**: Generates paginated queries in **MongoDB**.

---

### 4. Fetch Entries by Target Trade
*   **Method Name**: `getJournalEntriesByTrade` [GET `/trade/{tradeId}`]
*   **Lifecycle & Orchestration**: 
    1. Retrieves all notes definitively linked to a singular, specific trade execution.
    2. Does not rely on pagination, usually assuming nodes per trade remain small and localized.
*   **Database Resources**: Executes relational-style lookups linking Trade execution IDs with Note IDs inside **MongoDB**.

---

### 5. Fetch Entries by Date Range
*   **Method Name**: `getJournalEntriesByDateRange` [GET `/date-range`]
*   **Lifecycle & Orchestration**: 
    1. Combines `userId` plus strict ISO boundaries `startDate` and `endDate` parameters alongside paginated inputs.
    2. Embeds Controller-level business logic: explicitly blocks requests where `endDate < startDate`. This fail-fast mechanism prevents burdening the lower services.
*   **Database Resources**: Triggers temporal/timeline queries traversing the `timestamp` metadata inside **MongoDB** Journal entities.

---

### 6. Update & Delete a Journal Entry
*   **Method Names**: `updateJournalEntry` [PUT `/{entryId}`] , `deleteJournalEntry` [DELETE `/{entryId}`]
*   **Lifecycle & Orchestration**: 
    1. Provides robust patching/destruction mechanics via `tradeJournalService`.
    2. Specific error parsing on the PUT method catches exception string values containing "not found" to dynamically pivot logic mapping 400 Bad Request to 404 Not Found seamlessly using a custom `ErrorResponse`.
*   **Database Resources**: Executes update routines or fully evicts notes out of **MongoDB** storage. 
