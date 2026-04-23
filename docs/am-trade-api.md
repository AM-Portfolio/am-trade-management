# Module Analysis: `am-trade-api`

**Role**: The Gateway & REST Orchestration Layer

## 1. What is this folder doing?
The `am-trade-api` module is the "front door" for your Backend. In any Clean Architecture setup, this folder's strict responsibility is handling **Incoming HTTP Traffic**, ensuring that the data mapped to JSON is validated, and then handing it over to the core `am-trade-services` module. 

It specifically avoids executing heavy mathematical loops or database queries itself. Its primary job is:
1. Converting JSON to Java Objects (DTOs / Models).
2. Running structural validations (`@Valid`).
3. Acting as a "Traffic Cop" by passing validated data down to Business Services.
4. Supplying the Swagger / OpenAPI Documentation interfaces.

---

## 2. Dependencies (`pom.xml`)
This module heavily relies on standard framework tools for REST, while strictly importing only the required internal sibling modules:
*   `am-trade-services`: Required to execute the actual business logic of a trade or summary.
*   `am-trade-dashboard`: Required to pipe performance metric inputs.
*   `am-trade-exceptions`: Inherits global custom error tracking.
*   `spring-boot-starter-web`: The core engine empowering `@RestController` definitions.
*   `spring-boot-starter-validation`: Powers the `@Valid` tags to ensure incoming JSON payloads aren't empty.
*   `springdoc-openapi-starter-webmvc-ui` (v2.2.0): Auto-generates the Swagger UI visual test interface on `http://localhost:8080/swagger-ui.html`.

---

## 3. Folder Structure Breakdown
Inside `src/main/java/am/trade/api`, you will find exactly 5 packages:

### `config/`
*   Contains setup definitions like `OpenApiConfig.java`. This tells Swagger what the Title and Version of your web APIs are. It also contains `ApiAutoConfiguration.java` to ensure these beans load into the master `am-trade-app` execution cycle.

### `controller/`
*   The actual HTTP REST interfaces. Every file here is annotated with `@RestController` and `@RequestMapping`. They exclusively return `ResponseEntity<?>` objects.

### `dto/`
*   **Data Transfer Objects**. These are "bridge" classes tailored specifically for the frontend. Sometimes the UI only needs a subset of a database table (e.g., `FavoriteFilterRequest`, `MetricsResponse`). They exist here so the core database entities don't leak out to the web layer.

### `service/`
*   **The Orchestrators**. While `am-trade-services` does the heavy lifting, the `ApiServiceImpl` classes live here to coordinate multi-step logic. For example, `TradeApiServiceImpl.addTrade()` will first tell the `Validator` to check the trade, then tell `TradeDetailsService` to save it, and then tell `TradeProcessingService` to calculate metrics. 

### `validation/`
*   Custom validation logic that sits between the Controller and the API Service. For example, ensuring that a "Closed Range" trade strictly contains an exit price, throwing a 400 Bad Request error cleanly before it ever reaches the database layer.

---

## 4. Endpoints & Functionality
Because this module is so extensive, **we have created a dedicated documentation file for every single feature inside it!** 

To explore the precise endpoints, database mappings, and lifecycle of any Controller in this module, refer to the root-level API Documentation package:

*   [Trade Endpoints](api/TradeApi.md)
*   [Portfolio Summary Endpoints](api/PortfolioSummaryApi.md)
*   [Trade Journal Endpoints](api/TradeJournalApi.md)
*   [Performance Heatmap Endpoints](api/ProfitLossHeatmapApi.md)
*   [Save Filters Endpoints](api/FavoriteFilterApi.md)
*   [Trade Dashboard Metrics](api/TradeMetricsApi.md)
*   [Advanced Trade Comparisons](api/TradeComparisonApi.md)
*   [Trade Calendar Management](api/TradeManagementApi.md)
*   [Trade Summaries](api/TradeSummaryApi.md)
*   [User Configuration Preferences](api/UserPreferencesApi.md)

---

## 5. Summary for Developers
When tasked with writing code in the `am-trade-api` folder, strictly adhere to these rules:
1. **No Database Calls**: Do not wire any `Repositories` here. Always delegate to a Service.
2. **Handle Errors Fast**: Use the Custom Validation package to reject bad HTTP request formats immediately; do not let bad properties sink down to the core engine.
3. **Map Correctly**: Return `DTOs`, never raw database `Entities`.
