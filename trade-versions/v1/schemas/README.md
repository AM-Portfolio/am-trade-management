# API Schemas - v1

## Overview

This folder contains comprehensive JSON Schema definitions for all API endpoints in the AM Trade Management system. Schemas define the contract, validation rules, and examples for every API.

## Schema Files by Controller

### Core Trade Schemas

#### trade-controller-api-schema.json
- **API**: TradeController
- **Endpoints**: Trade CRUD, filtering, batch operations
- **Main Models**:
  - `TradeDetails` - Complete trade with all metadata
  - `TradeModel` - Trade execution model
  - `TradeMetrics` - Performance metrics
  - `InstrumentInfo` - Trading instrument details
  - `EntryExitInfo` - Entry/exit details
  - `TradePsychologyData` - Psychology factors
  - `TradeEntryExistReasoning` - Entry/exit reasoning
- **Use**: Reference for trade creation and validation

#### favorite-filter-api-schema.json
- **API**: FavoriteFilterController
- **Purpose**: Filter configuration and management
- **Main Models**:
  - `FavoriteFilter` - Filter configuration
  - `MetricsFilterConfig` - Filter metrics
  - `FilterCriteria` - Filter parameters
- **Use**: Build and validate filter configurations

#### journal-api-schema.json
- **API**: TradeJournalController
- **Purpose**: Trading journal entry management
- **Main Models**:
  - `TradeJournalEntryRequest` - Create/update journal entry
  - `TradeJournalEntryResponse` - Journal entry response
  - `BehaviorPatternSummary` - Daily behavior summary
  - `Attachment` - File attachments
- **Use**: Manage trading journal and notes

### Other Controller Schemas
- `portfolio-summary-api-schema.json` - Portfolio analytics
- `trade-metrics-api-schema.json` - Performance metrics
- `trade-summary-api-schema.json` - Period summaries
- Additional controller schemas as needed

## Schema Structure

Each schema follows JSON Schema Draft 7 specification with:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "API Schema Title",
  "description": "Complete description",
  "version": "1.0.0",
  "definitions": {
    "ModelName": {
      "type": "object",
      "description": "Model description",
      "required": ["field1", "field2"],
      "properties": {
        "field1": {
          "type": "string",
          "description": "Field description",
          "example": "sample value"
        }
      }
    }
  },
  "paths": {
    "/api/endpoint": {
      "method": {
        "summary": "Operation summary",
        "requestBody": { ... },
        "responses": { ... }
      }
    }
  }
}
```

## Key Models

### TradeDetails
Comprehensive trade information including:
- Basic info (tradeId, portfolio, symbol)
- Entry/exit details
- Metrics (P&L, holding time, risk/reward)
- Psychology factors
- Reasoning for entry/exit
- Attachments

### BehaviorPatternSummary
Daily trading behavior tracking:
- Summary of activities
- Mood (CONFIDENT, ANXIOUS, etc.)
- Market sentiment (1-10 scale)
- Categorization tags

### TradeMetrics
Performance metrics including:
- Profit/Loss amount and percentage
- Return on equity
- Risk-reward ratio
- Max adverse/favorable excursion
- Holding time

### MetricsFilterConfig
Filter configuration for querying trades:
- Portfolio selection
- Date range filters
- Metric types
- Instrument filters
- Trade characteristics
- Profit/loss ranges

## Using Schemas

### 1. Code Generation
Generate type-safe code from schemas:

```bash
# Java (with jsonschema2pojo)
jsonschema2pojo --source trade-controller-api-schema.json

# Python
python -m datamodel_code_generator --input trade-controller-api-schema.json

# JavaScript/TypeScript
quicktype trade-controller-api-schema.json
```

### 2. Validation
Validate API requests/responses:

```javascript
const Ajv = require('ajv');
const ajv = new Ajv();
const validate = ajv.compile(schema);
const valid = validate(data);
```

### 3. Documentation
Use schemas to generate API documentation:
- OpenAPI/Swagger tools
- API documentation generators
- Interactive API explorers

### 4. IDE Integration
Use schemas in your IDE:
- JSON validation in VS Code
- Auto-completion for JSON files
- Type hints for API calls

## API Endpoint Reference

### Trade Operations

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/api/v1/trades/details` | TradeDetails | TradeDetails |
| PUT | `/api/v1/trades/details/{id}` | TradeDetails | TradeDetails |
| GET | `/api/v1/trades/details/portfolio/{id}` | Query params | Array<TradeDetails> |
| POST | `/api/v1/trades/filter` | FilterConfig | FilterResponse |
| POST | `/api/v1/trades/details/batch` | Array<TradeDetails> | Array<TradeDetails> |

### Journal Operations

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/api/v1/journal` | TradeJournalEntryRequest | TradeJournalEntryResponse |
| GET | `/api/v1/journal/{id}` | - | TradeJournalEntryResponse |
| PUT | `/api/v1/journal/{id}` | TradeJournalEntryRequest | TradeJournalEntryResponse |
| DELETE | `/api/v1/journal/{id}` | - | 204 No Content |

### Filter Operations

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/api/v1/filters` | FavoriteFilter | FavoriteFilter |
| GET | `/api/v1/filters/{id}` | - | FavoriteFilter |
| POST | `/api/v1/filters/apply` | FilterRequest | FilterResponse |

## Validation Rules

### Common Validations

**TradeDetails**
- `tradeId`: Required, non-empty string
- `portfolioId`: Required, valid UUID
- `entryInfo.quantity`: Integer > 0
- `metrics.profitLoss`: Number (can be negative)
- `metrics.riskRewardRatio`: Number > 0

**BehaviorPatternSummary**
- `mood`: String, one of predefined values
- `marketSentiment`: Integer, 1-10 range
- `tags`: Array of strings

**TradeJournalEntry**
- `userId`: Required, non-empty string
- `title`: Required, 1-500 characters
- `content`: Required, 1-5000 characters
- `entryDate`: Required, valid ISO 8601 datetime
- `marketSentiment`: If provided, must be 1-10

## Field Constraints

### String Fields
- `tradeId`, `portfolioId`: UUID format
- `symbol`: Pattern: `[A-Z0-9]+`
- Descriptions: Max 500 characters

### Numeric Fields
- `profitLoss`: Decimal, 2 decimal places
- `marketSentiment`: Integer 1-10
- `quantity`: Integer >= 1

### Date Fields
- Format: ISO 8601 (YYYY-MM-DD'T'HH:MM:SS'Z')
- Must be valid timestamp

### Array Fields
- Tags: Non-empty strings
- Portfolio IDs: Valid UUIDs
- Symbols: Valid trading symbols

## Error Response Schema

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/trades/details",
  "details": [
    "Field 'portfolioId' must be a valid UUID",
    "Field 'quantity' must be greater than 0"
  ]
}
```

## Examples by Model

### Example: TradeDetails Request
```json
{
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "BANKNIFTY",
    "exchange": "NSE"
  },
  "status": "CLOSED",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T10:30:00Z",
    "price": 22500.00,
    "quantity": 25
  },
  "exitInfo": {
    "timestamp": "2025-01-15T14:30:00Z",
    "price": 22750.00,
    "quantity": 25
  }
}
```

### Example: BehaviorPatternSummary
```json
{
  "summary": "Active trading day with 5 positions. Strong momentum in IT sector.",
  "mood": "CONFIDENT",
  "marketSentiment": 8,
  "tags": ["high-volatility", "momentum-trading", "profitable-day"]
}
```

## Integration Points

### With Collections
- Collections reference schemas for request/response validation
- Sample data follows schema definitions
- Postman tests validate against schemas

### With Sample Data
- Payloads conform to schema specifications
- Examples include all required fields
- Edge cases shown in separate examples

### With Code Generators
- Use schemas to generate Java, Python, JavaScript classes
- Type-safe implementations
- Built-in validation

## Schema Updates

### Versioning
- Schema version in `"version"` field
- Backward compatible changes (additions) keep same version
- Breaking changes increment version
- Migration guide provided with major updates

### When Using New Fields
1. Check schema version
2. Verify backward compatibility
3. Update code generation if needed
4. Test with both old and new fields

## Tools & Resources

### Schema Validation Tools
- JSON Schema Validator: https://www.jsonschemavalidator.net/
- AJV (JavaScript): https://ajv.js.org/
- jsonschema2pojo (Java): http://www.jsonschema2pojo.org/

### Code Generation
- QuickType: https://quicktype.io/
- datamodel-code-generator: https://github.com/koxudaxi/datamodel-code-generator
- OpenAPI Generator: https://openapi-generator.tech/

### IDE Support
- VS Code JSON Schema integration
- JetBrains IDE JSON schema support
- Schema store: https://www.schemastore.org/

## Related Resources

- **Collections**: See `../collections/` for API examples
- **Sample Data**: See `../sample-data/` for realistic data
- **Documentation**: See `../documentation/` for detailed guides

## Version Information

- **Schema Format**: JSON Schema Draft 7
- **API Version**: v1.0.0
- **Last Updated**: December 2025
- **Compatibility**: JSON Schema compatible tools

---

**Quick Tips**:
1. Use schemas to generate type-safe code
2. Validate API requests using schema validation tools
3. Reference schemas when building API clients
4. Keep schemas updated with API changes
