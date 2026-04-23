# User Preferences API Documentation

**Base URL**: `/api/v1/preferences`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/UserPreferencesController.java`

This module maintains the specific state, layouts, and configurations unique to the individual trader. It abstracts UI configuration states away from trading mechanics.

---

### 1. Fetch Preferences
*   **Method Name**: `getDashboardPreferences` [GET `/dashboard`]
*   **Lifecycle & Orchestration**: 
    1. Extracts `userId` from the URL parameters.
    2. Forwards request to `userPreferencesService.getDashboardPreferences()`.
    3. Explicitly wraps "preferences not found" backend logic into custom `ErrorResponse` HTTP 404 bodies allowing frontends to gracefully fallback to defaults.
*   **Database Resources**: Target queries on user-profile configurations natively housed in **MongoDB**.

---

### 2. Save Preferences
*   **Method Name**: `saveDashboardPreferences` [POST `/dashboard`]
*   **Lifecycle & Orchestration**: 
    1. Ingests a completely encapsulated layout map via `@Valid DashboardPreferencesRequest`.
    2. Defers to `userPreferencesService` for logic and overrides existing datasets.
*   **Database Resources**: Usually operates as a strict *upsert* function in **MongoDB**, matching user identifiers and wholly replacing configuration BSONs.

---

### 3. Reset to Vanilla State
*   **Method Name**: `resetDashboardPreferences` [POST `/dashboard/reset`]
*   **Lifecycle & Orchestration**: 
    1. Strips existing modifications away, delegating to `userPreferencesService.resetDashboardPreferences()`.
    2. Returns the newly formulated system-default configurations.
*   **Database Resources**: Destroys custom state logic from **MongoDB** records returning them to foundational schemas.
