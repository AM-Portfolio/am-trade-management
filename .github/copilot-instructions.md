# GitHub Copilot Instructions - AM Trade Management System

## Project Overview
This is a comprehensive Spring Boot-based trade management system built with a multi-module architecture. The system provides REST APIs for managing trades, portfolios, analytics, and related financial operations.

## Architecture & Technology Stack

### Core Technologies
- **Java 17** - Primary development language
- **Spring Boot 3.5.0** - Application framework with Spring Web MVC
- **Maven** - Build and dependency management
- **MongoDB** - Primary document database for trade data
- **Redis** - Caching layer for performance optimization
- **Apache Kafka** - Event streaming and messaging
- **Docker** - Containerization (port 8073:8080)
- **Lombok** - Reduces boilerplate code
- **SpringDoc OpenAPI 2.2.0** - API documentation (Swagger)

### Multi-Module Structure
```
am-trade-management (parent)
├── am-trade-common      # Shared utilities and common models
├── am-trade-models      # Data models and DTOs
├── am-trade-services    # Business logic layer
├── am-trade-api         # REST API controllers
├── am-trade-app         # Main application entry point
├── am-trade-dashboard   # Dashboard functionality
├── am-trade-kafka       # Kafka messaging integration
├── am-trade-persistence # Data access layer
├── am-trade-exceptions  # Custom exception handling
└── am-trade-analytics   # Trade analytics and reporting
```

## Coding Standards & Best Practices

### Method Design Principles
- **Maximum method length**: 20 lines of code
- **Single Responsibility**: Each method performs exactly one logical operation
- **Naming**: Use descriptive camelCase names indicating method purpose
- **Parameters**: Limit to 3-4 parameters; use objects for complex parameter sets
- **Return Values**: Never return null or empty objects to mask errors; use Optional or exceptions

### Error Handling Standards
- **No Silent Failures**: Never catch exceptions only to return empty objects
- **Granular Exception Handling**: Use specific catch blocks for specific exceptions
- **Comprehensive Logging**: Always log exceptions with appropriate severity levels
- **Meaningful Context**: Include detailed information in error messages and logs
- **Custom Exception Hierarchy**: Use domain-specific exceptions extending TradeException
- **Fail Fast Principle**: Validate inputs early and throw exceptions for invalid conditions

### Exception Architecture
The system uses a comprehensive exception hierarchy:
```java
TradeException (base)
├── ValidationException
├── TradeFieldValidationException
└── InvalidTradeDataException
```

All exceptions include:
- HTTP status codes
- Structured error details
- Trace IDs for debugging
- Request context information

## API Design Patterns

### Controller Structure
All REST controllers follow consistent patterns:
```java
@RestController
@RequestMapping("/api/v1/{resource}")
@Tag(name = "API Name", description = "Description")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ResourceController {
    private final ResourceService service;
    // Controller methods
}
```

### API Documentation
- Use `@Operation` for method-level documentation
- Include `@ApiResponses` with status codes (200, 400, 500)
- Use `@Parameter` for path and query parameter descriptions
- Follow OpenAPI 3.0 standards

### Response Patterns
- Successful responses: Return ResponseEntity with appropriate HTTP status
- Error responses: Use GlobalExceptionHandler for consistent error formatting
- Pagination: Use Spring Data's Pageable for list endpoints
- Validation: Use `@Validated` and `@Valid` annotations

## Data Model Conventions

### MongoDB Documents
```java
@Document(collection = "collection_name")
@CompoundIndex(name = "idx_name", def = "{'field1': 1, 'field2': 1}")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityName extends BaseDocument {
    @Id
    private String id;
    
    @Indexed
    @Field("field_name")
    private Type fieldName;
}
```

### Key Field Conventions
- Use `@Field` annotations for MongoDB field mapping with snake_case
- Apply `@Indexed` for frequently queried fields
- Extend `BaseDocument` for common audit fields
- Use appropriate compound indexes for query optimization

### Model Classes
- **Trade**: Main trade entity with comprehensive trade information
- **TradeDetails**: Extended trade information with calculated fields
- **TradeStatistics**: Aggregated performance metrics and analytics
- **Statistics Models**: Organized into logical groups (ValueMetrics, TimeMetrics, RiskMetrics, etc.)

## Service Layer Patterns

### Service Implementation
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceImpl implements ServiceInterface {
    private final Repository repository;
    // Service methods following business logic patterns
}
```

### Transaction Management
- Use `@Transactional` for operations requiring data consistency
- Handle rollback scenarios appropriately
- Log transaction boundaries for debugging

## REST API Endpoints Structure

### Core Controllers & Endpoints
1. **TradeController** (`/api/v1/trades`)
   - CRUD operations for individual trades
   - Batch operations for multiple trades
   - Filtering by portfolio, symbol, status, date ranges

2. **TradeManagementController** (`/api/v1/trades`)
   - Calendar-based trade queries
   - Advanced filtering and search capabilities

3. **TradeSummaryController** (`/api/v1/trade-summary`)
   - Time-period based trade analysis
   - Performance summaries and aggregations

4. **PortfolioSummaryController** (`/api/v1/portfolio-summary`)
   - Portfolio-level analytics and summaries
   - Multi-portfolio comparisons

5. **TradeAnalyticsController** (`/api/v1/analytics/trade-replays`)
   - Trade replay scenarios and analysis
   - Historical performance evaluation

6. **TradeMetricsController** (`/api/v1/metrics`)
   - Performance metrics and KPIs
   - Risk analysis and reporting

7. **TradeJournalController** (`/api/v1/journal`)
   - Trading journal and notes management
   - Trade documentation and insights

8. **TradeComparisonController** (`/api/v1/comparison`)
   - Side-by-side trade analysis
   - Comparative performance metrics

9. **ProfitLossHeatmapController** (`/api/v1/heatmap`)
   - P&L visualization data
   - Heat map analytics for performance

10. **FavoriteFilterController** (`/api/v1/filters`)
    - Saved filter management
    - User preference handling

11. **UserPreferencesController** (`/api/v1/preferences`)
    - User settings and configurations
    - Personalization features

## Development Guidelines

### When Creating New Features
1. **API-First Design**: Define OpenAPI specifications before implementation
2. **Test Coverage**: Create comprehensive unit and integration tests
3. **Exception Handling**: Use appropriate custom exceptions from the hierarchy
4. **Logging**: Include structured logging with correlation IDs
5. **Documentation**: Update API documentation and inline comments
6. **Validation**: Implement both client and server-side validation

### Code Generation Patterns
- Follow the established multi-module architecture
- Use consistent naming conventions across all layers
- Implement proper separation of concerns (Controller → Service → Repository)
- Apply dependency injection via constructor injection
- Use builder patterns for complex object creation

### Database Operations
- Use MongoDB-specific annotations and features
- Implement proper indexing strategies
- Handle large datasets with pagination
- Use aggregation pipelines for complex queries
- Cache frequently accessed data in Redis

### Performance Considerations
- Implement caching strategies using Redis
- Use async processing for long-running operations
- Apply database indexing for query optimization
- Monitor and log performance metrics
- Use connection pooling for external services

## Testing Guidelines

### Test Structure
- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **API Tests**: Test REST endpoints with real data
- **Performance Tests**: Validate response times and throughput

### Test Data Management
- Use realistic test data that matches production patterns
- Implement proper test data cleanup
- Use test containers for database operations
- Mock external dependencies appropriately

## Environment Configuration

### Docker & Deployment
- Application runs on port 8080 internally, exposed as 8073
- Uses environment-specific profiles (docker, dev, prod)
- Health checks via Spring Actuator `/actuator/health`
- Proper timezone configuration (Asia/Kolkata)

### External Dependencies
- MongoDB for primary data storage
- Redis for caching layer
- Kafka for event streaming
- Market data API integration

## Code Examples & Patterns

### Creating a New Controller
```java
@RestController
@RequestMapping("/api/v1/new-resource")
@Tag(name = "New Resource API", description = "API for new resource operations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class NewResourceController {
    
    private final NewResourceService service;
    
    @Operation(summary = "Get resource by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resource found"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getById(
            @Parameter(description = "Resource ID") @PathVariable String id) {
        log.info("Getting resource with ID: {}", id);
        ResourceDto resource = service.findById(id);
        return ResponseEntity.ok(resource);
    }
}
```

### Exception Handling Pattern
```java
public class NewResourceException extends TradeException {
    public NewResourceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    public NewResourceException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## Maintenance & Updates

### Code Quality
- Follow the existing coding standards document
- Use static code analysis tools
- Maintain consistent code formatting
- Regular dependency updates following security advisories

### Documentation Updates
- Keep API documentation synchronized with code changes
- Update README files for significant architectural changes
- Maintain changelog for version tracking
- Document configuration changes and environment requirements

This instruction set should guide GitHub Copilot to generate code that aligns with the established patterns and architectural decisions in the AM Trade Management system.