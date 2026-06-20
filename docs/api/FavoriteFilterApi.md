# Favorite Filter API Documentation

**Base URL**: `/api/v1/filters`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/FavoriteFilterController.java`

This module manages the user's saved queries, acting as a bookmarking system for complex filter parameters so users don't have to manually select their preferred indicators/strategies over and over.

---

### 1. CRUD Operations on Filters
*   **Method Names**: `createFilter` [POST `/`], `updateFilter` [PUT `/{filterId}`], `deleteFilter` [DELETE `/{filterId}`]
*   **Lifecycle & Orchestration**: 
    1. Endpoints route the user's `FavoriteFilterRequest` straight to the `FavoriteFilterService`.
    2. Allows renaming, updating descriptions, or mutating the nested `MetricsFilterConfig`.
*   **Database Resources**: Manages documents in **MongoDB** tracking `FavoriteFilter` entities containing custom payload boundaries.

---

### 2. Fetch User Filters
*   **Method Names**: `getUserFilters` [GET `/`], `getFilterById` [GET `/{filterId}`]
*   **Lifecycle & Orchestration**: 
    1. Lookups bound strictly to the `userId` query parameter for basic tenant-safety.
    2. Read operations delegated to `FavoriteFilterService`.
*   **Database Resources**: Performs read lookups against the Filter table in **MongoDB**.

---

### 3. Default Filter Toggles
*   **Method Names**: `getDefaultFilter` [GET `/default`], `setDefaultFilter` [PUT `/{filterId}/default`]
*   **Lifecycle & Orchestration**: 
    1. Specifically manages the "startup" state for a user's dashboard view.
    2. `setDefaultFilter` ensures all other filters for the user have their `isDefault` flag unset before setting the new targeted filter.
*   **Database Resources**: Will cause bulk updates if multiple filters exist, modifying boolean flags across user documents in **MongoDB**.

---

### 4. Save Current UI Context
*   **Method Name**: `saveCurrentFilter` [POST `/save-current`]
*   **Lifecycle & Orchestration**: 
    1. A convenience endpoint receiving the active `MetricsFilterRequest` and casting it into a savable `FavoriteFilterRequest` format using `convertToFilterConfig`. 
    2. Defers to `FavoriteFilterService.createFilter()`.
*   **Database Resources**: Creates new configurations in **MongoDB**.

---

### 5. Apply a Filter (Data Fetch)
*   **Method Name**: `getMetricsWithFavoriteFilter` [GET `/apply/{filterId}`]
*   **Lifecycle & Orchestration**: 
    1. The core functional endpoint: looks up a saved filter by ID, loads its criteria, and then immediately runs a massive Trade query. 
    2. Defers cross-module to the `UserTradeService` to orchestrate this interaction between saved configurations and the core Trade databases.
    3. Triggers detailed HTTP 400 Bad Request error detail arrays when configurations are missing.
*   **Database Resources**: Initiates heavy complex aggregation queries across trades in **MongoDB** driven by the deserialized filter criteria.
