# AM Trade Management - API v1 Documentation

## Overview

This directory contains complete API documentation, collections, schemas, and sample data for the AM Trade Management system version 1.0.

## Directory Structure

```
v1/
├── collections/          # Postman Collection exports
│   ├── Trade APIs
│   ├── Portfolio APIs
│   ├── Journal APIs
│   ├── Analytics APIs
│   └── Filter APIs
├── schemas/             # JSON Schema definitions for all APIs
│   ├── Trade Controller
│   ├── Favorite Filters
│   ├── Trade Journal
│   ├── Portfolio Summary
│   ├── Trade Metrics
│   └── Other Controllers
├── sample-data/         # Sample payloads and mock data
│   ├── trade-payloads/
│   ├── filter-examples/
│   ├── journal-entries/
│   └── portfolio-data/
└── documentation/       # Guides, quick references, and implementation docs
    ├── API Guides
    ├── Quick References
    ├── Code Generation Guides
    └── Implementation Guides
```

## Quick Start

### 1. **Explore Collections**
   - Start with `collections/` folder
   - Import any `.postman_collection.json` file into Postman
   - Collections are organized by API endpoint

### 2. **Review Schemas**
   - Check `schemas/` for complete API contract definitions
   - Use schemas for code generation and validation
   - Each schema includes detailed property definitions and examples

### 3. **Use Sample Data**
   - Reference `sample-data/` for request/response examples
   - Use examples as templates for your API calls
   - Includes edge cases and comprehensive test scenarios

### 4. **Read Documentation**
   - Start with `documentation/QUICK-REFERENCE.md`
   - Consult specific guides for detailed implementation
   - Follow code generation guides for your preferred language

## API Controllers & Collections

| Collection | Purpose | Key Endpoints |
|-----------|---------|---|
| **Trade Controller** | Core trade management | Create, Read, Update, Filter trades |
| **Trade Journal** | Trading journal entries | Journal CRUD operations |
| **Portfolio Summary** | Portfolio analytics | Portfolio-level metrics & summaries |
| **Trade Metrics** | Performance metrics | Performance KPIs & analysis |
| **Favorite Filters** | Saved filter management | Filter CRUD & application |
| **Trade Comparison** | Trade analysis | Side-by-side trade comparison |
| **Trade Heatmap** | P&L visualization | Heat map analytics |
| **Trade Summary** | Trade aggregation | Time-period summaries |
| **User Preferences** | User settings | Preference management |

## Common Tasks

### Creating a Trade
1. Open `collections/TradeController.postman_collection.json`
2. Navigate to "Create Trade" endpoint
3. Refer to `sample-data/trade-payloads/basic-trade.json`
4. Modify as needed and send request

### Filtering Trades
1. Open `collections/FavoriteFilterController.postman_collection.json`
2. Use filter examples from `sample-data/filter-examples/`
3. Check `documentation/FILTER-EXAMPLES-README.md` for complex filters

### Adding Journal Entry
1. Import `collections/TradeJournalController.postman_collection.json`
2. Check `sample-data/journal-entries/` for examples
3. Reference `schemas/journal-api-schema.json` for field definitions

### Understanding API Schemas
1. Review JSON schemas in `schemas/` folder
2. Each schema includes:
   - Property definitions and types
   - Required fields
   - Validation rules
   - Example values
   - Constraints and limits

## Documentation Files

### Quick References
- `QUICK-REFERENCE.md` - API endpoint quick lookup
- `PAGINATION-GUIDE.md` - Pagination patterns
- Controller-specific quick refs in documentation folder

### Comprehensive Guides
- `TRADE-CONTROLLER-EXAMPLES.md` - Trade API examples
- `FILTER-EXAMPLES-README.md` - Filter usage patterns
- `FILTER-TRADE-DETAILS-README.md` - Trade detail filtering
- `SCHEMA-CODE-GENERATION-GUIDE.md` - Generate code from schemas

### Implementation Guides
- `TRADE-SCHEMA-CODE-GENERATION-GUIDE.md` - Trade schema code gen
- Code generation guides for Java, Python, JavaScript, Flutter
- Best practices and patterns

## Integration

### With Postman
1. Clone this repository
2. Import collections from `v1/collections/`
3. Set up environment variables
4. Use pre-configured requests

### With Your Application
1. Reference schemas in `v1/schemas/`
2. Use sample data for testing
3. Follow code generation guides
4. Implement according to your language/framework

## Versioning & Future Updates

- **v1/** - Current stable API version (API v1.0)
- **v2/** - Future API version (planned for next release)
- Each version is self-contained and backward compatible

### Migration Path
- New features added to latest version
- Previous versions maintained for backward compatibility
- Migration guides provided with new versions

## Support & Resources

### For Each API
- **Schema**: Provides contract & validation rules
- **Collection**: Executable examples in Postman
- **Sample Data**: Real-world example payloads
- **Documentation**: Detailed guides and patterns

### Getting Help
1. Check Quick Reference for endpoint syntax
2. Review examples in sample-data folder
3. Consult schema for field requirements
4. Follow implementation guides in documentation

## Best Practices

### API Usage
- Always validate requests against schema
- Use pagination for large datasets
- Include proper error handling
- Log request/response for debugging

### Development
- Generate code from schemas for type safety
- Use sample data for testing
- Follow existing patterns in collections
- Refer to guides for complex operations

### Testing
- Use sample payloads from sample-data folder
- Test edge cases mentioned in documentation
- Validate responses against schema
- Check error scenarios

## Notes

- All schemas are in JSON Schema Draft 7 format
- Collections are compatible with Postman 10+
- Sample data includes both success and error scenarios
- Documentation is kept in sync with code updates

---

**Version**: 1.0.0  
**Last Updated**: December 2025  
**API Base URL**: `http://localhost:8080/api/v1`  
**Contact**: AM Trade Management Team
