# AM-Trade-Management Coding Standards

## Method Design
- **Maximum Length**: Methods should not exceed 20 lines of code
- **Single Responsibility**: Each method should perform exactly one logical operation
- **Naming**: Use descriptive camelCase names that indicate the method's purpose
- **Parameters**: Limit to 3-4 parameters; use objects for more complex parameter sets
- **Return Values**: Never return null or empty objects to mask errors; use Optional or exceptions

## Error Handling
- **No Silent Failures**: Never catch exceptions only to return empty objects
- **Granular Exception Handling**: Use specific catch blocks for specific exceptions
- **Logging**: Always log exceptions with appropriate severity levels
- **Context**: Include meaningful context in error messages and logs

## Exception Handling Best Practices
- **Custom Exceptions**: Create domain-specific exception classes that extend from appropriate base exceptions
- **Meaningful Messages**: Include detailed information in exception messages (what failed, why it failed, possible remedies)
- **Exception Hierarchy**: Design a clear exception hierarchy for your application domains
- **Fail Fast**: Validate inputs early and throw exceptions as soon as invalid conditions are detected
- **Wrapping Exceptions**: When catching and re-throwing, preserve the original exception as the cause
- **Exception Translation**: Convert low-level exceptions to domain-appropriate exceptions that make sense to callers
- **Resource Cleanup**: Always use try-with-resources for closeable resources
- **Avoid Exception for Flow Control**: Don't use exceptions for normal application flow control

### Exception Design Examples

```java
// Domain-specific exception hierarchy
public class TradeException extends RuntimeException {
    private final String tradeId;
    
    public TradeException(String message, String tradeId) {
        super(message);
        this.tradeId = tradeId;
    }
    
    public TradeException(String message, String tradeId, Throwable cause) {
        super(message, cause);
        this.tradeId = tradeId;
    }
    
    public String getTradeId() {
        return tradeId;
    }
}

// Specific exception types
public class TradeValidationException extends TradeException {
    private final List<String> validationErrors;
    
    public TradeValidationException(String message, String tradeId, List<String> validationErrors) {
        super(message, tradeId);
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
```

### Exception Handling Examples

```java
// Exception translation example
public void processTrade(TradeModel trade) {
    try {
        // Business logic
        validateTrade(trade);
        persistTrade(trade);
    } catch (SQLException e) {
        // Translate to domain exception with context
        throw new TradePersistenceException(
            "Failed to persist trade: " + e.getMessage(),
            trade.getId(),
            e  // Include original cause
        );
    } catch (Exception e) {
        // Generic handler with context
        throw new TradeProcessingException(
            "Unexpected error processing trade: " + e.getMessage(),
            trade.getId(),
            e
        );
    }
}
```

## Class Structure
- **Maximum Size**: Classes should not exceed 300 lines
- **Encapsulation**: Keep fields private, expose through getters/setters when necessary
- **Cohesion**: A class should represent a single concept or entity

## Modularity
- **Module Boundaries**: Clearly define module responsibilities and interfaces
- **Dependency Injection**: Use Spring DI for loose coupling between components
- **API Design**: Create clear, consistent APIs between modules

## Code Quality
- **Comments**: Add meaningful comments for complex logic, not obvious operations
- **Unit Tests**: Write tests for all public methods and edge cases
- **Coverage**: Maintain minimum 80% test coverage
- **DRY Principle**: Don't Repeat Yourself - extract common code into reusable methods
- **SOLID Principles**: Follow Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion principles

## Logging Best Practices
- **Log Levels**: Use appropriate log levels (ERROR, WARN, INFO, DEBUG, TRACE) based on message importance
- **Contextual Information**: Include relevant context in log messages (IDs, operation names, etc.)
- **Sensitive Data**: Never log sensitive information (passwords, personal data, etc.)
- **Performance**: Use isDebugEnabled() checks before constructing expensive log messages
- **Exception Logging**: Include full stack traces when logging exceptions
- **Structured Logging**: Use structured logging format for machine parsing when possible

### Logging Examples

```java
// Good logging practice
private static final Logger logger = LoggerFactory.getLogger(TradeService.class);

public void processTrade(TradeModel trade) {
    logger.info("Starting trade processing for trade ID: {}", trade.getId());
    
    try {
        // Processing logic
        logger.debug("Trade validation passed for trade ID: {}", trade.getId());
        
        // More processing
        logger.info("Successfully processed trade ID: {}, result: {}", 
                   trade.getId(), result.getStatus());
    } catch (ValidationException e) {
        logger.warn("Validation failed for trade ID: {}, errors: {}", 
                   trade.getId(), e.getValidationErrors());
        throw e;
    } catch (Exception e) {
        logger.error("Failed to process trade ID: {}", trade.getId(), e);
        throw new TradeProcessingException("Processing failed", trade.getId(), e);
    }
}
```

## Mappers
- **Null Handling**: Always check for null inputs at the beginning of mapper methods
- **Exception Propagation**: Allow exceptions to propagate rather than returning empty objects
- **Granular Error Handling**: Use try-catch only around specific conversion operations that might fail
- **Logging**: Log warnings for individual item failures without failing the entire mapping

## Naming Conventions
- **Classes**: PascalCase (e.g., `TradeDetailsMapper`)
- **Methods/Variables**: camelCase (e.g., `convertToEntity`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
- **Packages**: lowercase with dots (e.g., `am.trade.persistence.mapper`)

## Project Structure
- **Package Organization**: Group related classes in the same package
- **Layer Separation**: Keep clear boundaries between persistence, service, and API layers
- **Test Structure**: Mirror main code structure in test directories

## JUnit Testing Guidelines
- **Test Naming**: Use descriptive names that indicate the scenario being tested (e.g., `shouldThrowExceptionWhenInputIsInvalid`)
- **Test Structure**: Follow the Arrange-Act-Assert (AAA) or Given-When-Then pattern
- **Test Independence**: Each test should be independent and not rely on other tests
- **Mock Dependencies**: Use Mockito or similar frameworks to mock external dependencies
- **Test Edge Cases**: Include tests for boundary conditions, null inputs, empty collections, etc.
- **Parameterized Tests**: Use JUnit's parameterized tests for testing multiple inputs
- **Test Exception Handling**: Explicitly test exception scenarios using assertThrows
- **Test Coverage**: Aim for both line coverage and branch coverage
- **Test Data**: Use meaningful test data that represents real-world scenarios
- **Test Performance**: Keep unit tests fast; separate slow integration tests

### Test Coverage Requirements
- **Mapper Classes**: 95% line coverage, 90% branch coverage
- **Service Classes**: 90% line coverage, 85% branch coverage
- **Controllers/APIs**: 85% line coverage
- **Entity/Model Classes**: 80% line coverage (focusing on non-trivial methods)

### Mocking Best Practices

> **IMPORTANT**: Never mock mapper objects in tests. Always use real mapper implementations to ensure correct data transformation logic is tested. Mappers should be simple enough that using real instances doesn't impact test performance.

```java
@ExtendWith(MockitoExtension.class)
class TradeServiceTest {
    @Mock
    private TradeRepository tradeRepository;
    
    // Use real mapper implementation, not a mock
    private TradeMapper tradeMapper = new TradeMapperImpl();
    
    @InjectMocks
    private TradeService tradeService;
    
    @Test
    void shouldReturnTradeWhenValidIdProvided() {
        // Arrange
        String tradeId = "T-123";
        TradeEntity entity = new TradeEntity();
        entity.setId(tradeId);
        
        TradeModel expected = new TradeModel();
        expected.setId(tradeId);
        
        // Configure mocks
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(entity));
        when(tradeMapper.toModel(entity)).thenReturn(expected);
        
        // Act
        TradeModel result = tradeService.getTradeById(tradeId);
        
        // Assert
        assertEquals(expected.getId(), result.getId());
        verify(tradeRepository).findById(tradeId);
        verify(tradeMapper).toModel(entity);
    }
    
    @Test
    void shouldThrowExceptionWhenTradeNotFound() {
        // Arrange
        String tradeId = "T-999";
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(TradeNotFoundException.class, () -> {
            tradeService.getTradeById(tradeId);
        });
        
        verify(tradeRepository).findById(tradeId);
        verifyNoInteractions(tradeMapper);
    }
}
```

## Example Test Structure

```java
@Test
public void shouldConvertModelToEntityCorrectly() {
    // Arrange (Given)
    TradeEntryReasoning model = new TradeEntryReasoning();
    model.setPrimaryReason("TECHNICAL");
    model.setConfidenceLevel(80);
    
    // Act (When)
    TradeEntryReasoningEntity entity = mapper.toEntity(model);
    
    // Assert (Then)
    assertEquals("TECHNICAL", entity.getPrimaryReason());
    assertEquals(80, entity.getConfidenceLevel());
}

@Test
public void shouldThrowExceptionWhenInvalidDataProvided() {
    // Arrange
    TradeEntryReasoning model = new TradeEntryReasoning();
    model.setPrimaryReason("INVALID_REASON");
    
    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> {
        mapper.toEntity(model);
    });
}
```

## Performance Considerations
- **Avoid N+1 Queries**: Use eager fetching or batch processing when retrieving related entities
- **Pagination**: Implement pagination for large result sets
- **Caching**: Use appropriate caching strategies for frequently accessed, rarely changed data
- **Lazy Loading**: Configure lazy loading appropriately to avoid unnecessary database calls
- **Bulk Operations**: Use batch inserts/updates for multiple records
- **Connection Management**: Close database connections and other resources properly
- **Memory Management**: Avoid loading large datasets into memory at once
- **Profiling**: Regularly profile your application to identify performance bottlenecks

### Performance Example

```java
// Bad practice - N+1 query problem
public List<TradeModel> getAllTradesWithDetails() {
    List<TradeEntity> trades = tradeRepository.findAll();
    List<TradeModel> result = new ArrayList<>();
    
    for (TradeEntity trade : trades) {
        // This causes a separate query for each trade
        List<TradeDetailEntity> details = detailRepository.findByTradeId(trade.getId());
        TradeModel model = tradeMapper.toModel(trade);
        model.setDetails(detailMapper.toModelList(details));
        result.add(model);
    }
    
    return result;
}

// Better practice - Single query with join fetch
public List<TradeModel> getAllTradesWithDetails() {
    // One query that fetches trades and their details
    List<TradeEntity> tradesWithDetails = tradeRepository.findAllWithDetails();
    return tradeMapper.toModelList(tradesWithDetails);
}
```

## Service Design Patterns
- **Layered Architecture**: Maintain clear separation between controllers, services, and repositories
- **Facade Pattern**: Use service facades to simplify complex subsystem interactions
- **Strategy Pattern**: Implement different algorithms behind the same interface
- **Factory Pattern**: Use factories to create complex objects
- **Builder Pattern**: Use builders for objects with many optional parameters
- **Dependency Injection**: Inject dependencies rather than creating them internally
- **Repository Pattern**: Abstract data access behind repository interfaces
- **Command Pattern**: Encapsulate requests as objects
- **Observer Pattern**: Use for event-driven architectures
- **Decorator Pattern**: Add behavior to objects without affecting other objects

### Service Layer Guidelines
- **Transaction Management**: Use @Transactional at the service layer
- **Service Composition**: Compose complex services from simpler ones
- **Immutable Inputs**: Treat input parameters as immutable
- **Return New Objects**: Return new objects rather than modifying inputs
- **Validation**: Validate inputs at the service boundary
- **Idempotency**: Design service operations to be idempotent when possible
- **Pagination**: Support pagination for operations returning large datasets
- **Bulk Operations**: Support bulk operations for efficiency

### Service Layer Example

```java
// Service interface
public interface TradeService {
    TradeModel getTradeById(String id);
    List<TradeModel> getTradesByPortfolioId(String portfolioId);
    TradeModel createTrade(TradeModel trade);
    TradeModel updateTrade(String id, TradeModel trade);
    void deleteTrade(String id);
}

// Service implementation
@Service
public class TradeServiceImpl implements TradeService {
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;
    private final ValidationService validationService;
    
    @Autowired
    public TradeServiceImpl(TradeRepository tradeRepository, 
                           TradeMapper tradeMapper,
                           ValidationService validationService) {
        this.tradeRepository = tradeRepository;
        this.tradeMapper = tradeMapper;
        this.validationService = validationService;
    }
    
    @Override
    public TradeModel getTradeById(String id) {
        return tradeRepository.findById(id)
            .map(tradeMapper::toModel)
            .orElseThrow(() -> new TradeNotFoundException("Trade not found: " + id));
    }
    
    // Other method implementations...
}
```

## JSON Data Handling
- **Object Serialization**: Use Jackson or Gson for JSON serialization/deserialization
- **Data Transfer Objects**: Create dedicated DTOs for API requests and responses
- **Validation**: Validate JSON data using annotations or programmatic validation
- **Error Handling**: Provide clear error messages for invalid JSON
- **Schema Validation**: Consider using JSON Schema for complex validation requirements
- **Test Data**: Store test data in JSON files for reuse across tests
- **Configuration**: Use JSON for configuration when appropriate

### Advanced JSON Handling
- **Type References**: Use TypeReference for handling generic types and collections
- **Custom Serializers/Deserializers**: Implement custom serializers for complex objects
- **JSON Path**: Use JSON Path for querying complex JSON structures
- **Partial Updates**: Support PATCH operations with JsonPatch or JsonMergePatch
- **Streaming API**: Use streaming for large JSON documents
- **JSON Views**: Use Jackson's @JsonView for context-specific serialization

### JSON Data Handling Examples

```java
// Loading JSON from file
public <T> T loadFromJson(String filePath, Class<T> targetClass) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), targetClass);
    } catch (IOException e) {
        throw new JsonProcessingException("Failed to load JSON from " + filePath, e);
    }
}

// Creating object from JSON string
public <T> T fromJson(String json, Class<T> targetClass) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, targetClass);
    } catch (JsonProcessingException e) {
        throw new JsonParsingException("Failed to parse JSON: " + e.getMessage(), e);
    }
}

// Converting object to JSON
public String toJson(Object object) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
        throw new JsonSerializationException("Failed to serialize to JSON", e);
    }
}


// Loading generic collections from JSON
public <T> List<T> loadListFromJson(String filePath, Class<T> elementClass) {
    try {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        return mapper.readValue(new File(filePath), type);
    } catch (IOException e) {
        throw new JsonProcessingException("Failed to load JSON list from " + filePath, e);
    }
}

// Loading test data in test classes
@TestConfiguration
public class TestDataLoader {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @Bean
    public List<TradeModel> testTrades() {
        try {
            return mapper.readValue(
                new ClassPathResource("trades.json").getInputStream(),
                new TypeReference<List<TradeModel>>() {}
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test trades", e);
        }
    }
}
```

## Code Review Practices
- **Readability**: Code should be easily understood by other developers
- **Maintainability**: Changes should be easy to implement in the future
- **Security**: Review for potential security vulnerabilities
- **Performance**: Consider the performance implications of the code
- **Test Coverage**: Ensure adequate test coverage for new and modified code
- **Documentation**: Check that code is properly documented
- **Standards Compliance**: Verify adherence to these coding standards

### Code Review Checklist
1. Does the code fulfill the requirements?
2. Is the code well-structured and easy to understand?
3. Are there any potential bugs or edge cases not handled?
4. Is there appropriate error handling?
5. Are there adequate tests covering the changes?
6. Is the code consistent with the existing codebase?
7. Are there any performance concerns?
8. Is the code secure against common vulnerabilities?
9. Is there appropriate logging and monitoring?
