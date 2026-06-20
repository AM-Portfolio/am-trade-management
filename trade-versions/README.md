# Postman v1.0 - Complete API Documentation & Collections

Welcome to the **AM Trade Management - Postman v1.0** directory. This is your complete reference for API collections, schemas, sample data, and implementation guides.

## 📚 Table of Contents

1. [Quick Start](#-quick-start)
2. [Directory Structure](#-directory-structure)
3. [What's Inside Each Section](#-whats-inside-each-section)
4. [Collections Overview](#-collections-overview)
5. [Schemas & Models](#-schemas--models)
6. [Sample Data & Examples](#-sample-data--examples)
7. [Documentation & Guides](#-documentation--guides)
8. [Common Workflows](#-common-workflows)
9. [Versioning Strategy](#-versioning-strategy)
10. [FAQ & Troubleshooting](#-faq--troubleshooting)

---

## 🚀 Getting Started

### Prerequisites
- Postman Desktop App or Postman Web Client
- Docker and Docker Compose installed
- AM Trade Management application running

### Quick Start (1 minute)
```bash
# 1. Start the application
cd a:\InfraCode\AM-Portfolio\am-trade-managment
docker-compose up -d

# 2. Verify it's running
curl http://localhost:8073/actuator/health

# 3. Open Postman and import a collection:
#    File → Import → Select v1/collections/{ControllerName}.postman_collection.json
```

### Starting the Application
```bash
# Navigate to the project root
cd a:\InfraCode\AM-Portfolio\am-trade-managment

# Start the application using Docker Compose
docker-compose up -d

# Check if the application is running
curl http://localhost:8073/actuator/health
```

## 📚 Documentation Structure

> **All documentation is now organized in [v1/](./v1/) directory for clarity and scalability**

### 🗂️ Organized by Purpose

| Directory | Contains | Use Case |
|-----------|----------|----------|
| **[v1/collections/](./v1/collections/)** | Postman collection exports | Import into Postman, test APIs |
| **[v1/schemas/](./v1/schemas/)** | JSON Schema definitions | Code generation, validation, documentation |
| **[v1/sample-data/](./v1/sample-data/)** | Example payloads | Test data, integration examples |
| **[v1/documentation/](./v1/documentation/)** | Implementation guides | Learn, implement, troubleshoot |

### 📋 What's Available

**Collections** (10 API controllers)
- TradeController, TradeJournalController, FavoriteFilterController
- PortfolioSummaryController, TradeMetricsController, TradeSummaryController
- TradeComparisonController, TradeReplayController, ProfitLossHeatmapController
- UserPreferencesController
→ [Browse collections](./v1/collections/README.md)

**Schemas** (API contracts)
- Trade API schemas
- Filter API schemas  
- Journal API schemas
- Portfolio and Analytics schemas
→ [Browse schemas](./v1/schemas/README.md)

**Sample Data** (50+ examples)
- Trade payloads (create, update, batch)
- Filter configurations (by symbol, strategy, P&L, risk)
- Journal entries (with behavior patterns)
- Portfolio data, Analytics, Error responses
→ [Browse sample data](./v1/sample-data/README.md)

**Documentation** (Guides & Reference)
- Quick reference cards
- API endpoint guides (one per controller)
- Implementation guides (Java, Python, Node.js, etc.)
- Feature documentation, Troubleshooting, Pagination
→ [Browse documentation](./v1/documentation/README.md)

### 🔄 Version Management

```
v1/                    ← Current stable version (v1.0)
├── collections/       All API collections
├── schemas/          All API schemas
├── sample-data/      All example payloads
└── documentation/    All guides & reference

v2/                   ← Future major version (when needed)
├── collections/
├── schemas/
├── sample-data/
└── documentation/
```

→ [Learn about versioning & organization](./ORGANIZATION-GUIDE.md)

---

## 📖 Legacy Documentation Reference

### API Schemas & Code Generation
| File | Location | Description |
|------|----------|-------------|
| **FavoriteFilterController API** | | |
| `favorite-filter-api-schema.json` | `v1/schemas/` | **Complete JSON Schema** - OpenAPI 3.0 compliant |
| `favorite-filter-examples.json` | `v1/sample-data/filter-examples/` | **Comprehensive examples** - 7 request types |
| `SCHEMA-CODE-GENERATION-GUIDE.md` | `v1/documentation/` | **Multi-language code generation** (Java, Python, Flutter, TypeScript) |
| `SCHEMA-UPDATE-SUMMARY.md` | `v1/documentation/` | **Latest updates** - metric types, groupBy dimensions |
| **TradeController API** | | |
| `trade-controller-api-schema.json` | `v1/schemas/` | **Complete JSON Schema** - 19 models |
| `TRADE-CONTROLLER-EXAMPLES.md` | `v1/documentation/` | **Comprehensive examples** |
| `TRADE-SCHEMA-CODE-GENERATION-GUIDE.md` | `v1/documentation/` | **Multi-language code generation** |
| `TRADE-CONTROLLER-QUICK-REF.md` | `v1/documentation/` | **Quick reference** |
| `TRADE-SCHEMA-SUMMARY.md` | `v1/documentation/` | **Complete summary** |

### Quick Reference Guides
| File | Location | Description |
|------|----------|-------------|
| `FAVORITE-FILTER-QUICK-REF.md` | `v1/documentation/` | **Quick reference card** - All endpoints, models |
| `PAGINATION-QUICK-REF.md` | `v1/documentation/` | **Pagination guide** - Spring Pageable usage |
| `QUICK-REFERENCE.md` | `v1/documentation/` | **General API reference** |

### Implementation Documentation
| File | Location | Description |
|------|----------|-------------|
| `FILTER-IMPLEMENTATION-SUMMARY.md` | `v1/documentation/` | Filter implementation architecture |
| `FILTER-PAGINATION-GUIDE.md` | `v1/documentation/` | Detailed pagination implementation |
| `FILTER-EXAMPLES-README.md` | `v1/documentation/` | Filter usage examples |
| `FILTER-TRADE-DETAILS-README.md` | `v1/documentation/` | Trade details filtering |
| `BULK-DELETE-FILTERS.md` | `v1/documentation/` | Bulk deletion feature |

### Testing Scripts
| File | Location | Description |
|------|----------|-------------|
| `test-comprehensive-filter.sh` | `v1/sample-data/scripts/` | **Bash script** - Test all filter endpoints |
| `test-comprehensive-filter.ps1` | `v1/sample-data/scripts/` | **PowerShell script** - Test all filter endpoints |

---

## 📁 Collections (Now Organized in v1/collections/)

All collection files are now organized in the [`v1/collections/`](./v1/collections/) directory. To import:

**File → Import → Select from `v1/collections/{ControllerName}.postman_collection.json`**
- Save current filter configuration

### 2. AM-Trade-Management-API.postman_collection.json
**Main API Collection** - Contains core trade management endpoints:

#### 🔄 Trade Management
- **Get Trade Details by Portfolio and Symbols** - Retrieve trades for a specific portfolio
- **Add New Trade** - Create a new trade entry
- **Update Trade** - Modify an existing trade
- **Filter Trades** - Advanced trade filtering with multiple criteria
- **Batch Add/Update Trades** - Process multiple trades in a single request
- **Get Trades by IDs** - Retrieve specific trades by their IDs

#### 📅 Trade Calendar Management
- **Get Trades by Day** - Retrieve all trades for a specific date
- **Get Trades by Month** - Get monthly trade data

#### 📊 Trade Summary
- **Get Trade Details by Time Period** - Flexible time-period based trade retrieval
  - Supports: DAY, MONTH, QUARTER, FINANCIAL_YEAR, CUSTOM periods

#### 🔬 Trade Analytics
- **Create Trade Replay** - Create analytical trade replay scenarios
- **Get Trade Replay by ID** - Retrieve specific replay analysis
- **Get Trade Replays by Symbol** - Get all replays for a stock symbol
- **Get Trade Replays by Date Range** - Retrieve replays within date range

#### 🏥 Health Check & Actuator
- **Health Check** - Application health status
- **Application Info** - Application metadata and information
- **Metrics** - Application metrics and performance data

### 2. AM-Trade-Metrics-Portfolio-API.postman_collection.json
**Extended API Collection** - Contains metrics, portfolio, and user management endpoints:

#### 📈 Trade Metrics
- **Get All Trade Metrics** - Comprehensive trading metrics
- **Get Metrics by Portfolio** - Portfolio-specific metrics
- **Get Metrics by Date Range** - Time-filtered metrics

#### 💼 Portfolio Summary
- **Get Portfolio Summary** - Individual portfolio overview
- **Get All Portfolio Summaries** - All portfolio summaries
- **Get Portfolio Performance** - Performance analytics for portfolios

#### ⚙️ User Preferences
- **Get User Preferences** - Retrieve user settings
- **Update User Preferences** - Modify user preferences
- **Create User Preferences** - Set up new user preferences

#### 📝 Trade Journal
- **Get Journal Entries** - Retrieve trading journal entries
- **Create Journal Entry** - Add new journal entry
- **Update Journal Entry** - Modify existing journal entry

#### 🔄 Trade Comparison
- **Compare Trades** - Side-by-side trade analysis
- **Compare Strategies** - Strategy performance comparison

#### 🌡️ Profit/Loss Heatmap
- **Get P&L Heatmap** - Visual profit/loss data
- **Get P&L Heatmap by Symbol** - Symbol-specific P&L visualization

#### ⭐ Favorite Filters
- **Get User Favorite Filters** - Retrieve saved filters
- **Create Favorite Filter** - Save new filter configuration
- **Update Favorite Filter** - Modify existing filter
- **Delete Favorite Filter** - Remove saved filter

## 🌍 Environment Configuration

### AM-Trade-Management.postman_environment.json
Pre-configured environment variables for easy testing:

#### Core Variables
- `baseUrl`: Application base URL (default: http://localhost:8073)
- `authToken`: Authentication token (configure as needed)

#### Test Data Variables
- `portfolioId`: Sample portfolio ID (portfolio-123)
- `userId`: Sample user ID (user-123)
- `tradeId`: Sample trade ID (trade-456)
- `symbol`: Sample stock symbol (AAPL)
- `symbols`: Multiple symbols (AAPL,GOOGL,MSFT)

#### Date/Time Variables
- `startDate`: Sample start date (2024-01-01)
- `endDate`: Sample end date (2024-12-31)
- `date`: Single date (2024-01-15)
- `year`, `month`, `quarter`: Time period components

#### Pagination Variables
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

## 🛠️ Setup Instructions

### 1. Import Collections into Postman

1. **Open Postman**
2. **Import Collections**:
   - Click "Import" in Postman
   - Select both `.json` collection files
   - Import the environment file

3. **Set Environment**:
   - Select "AM Trade Management Environment" from the environment dropdown

### 2. Configure Authentication

If your API requires authentication:
1. Update the `authToken` variable in the environment
2. Or configure collection-level authentication in Postman

### 3. Customize Variables

Update environment variables as needed:
- Change `baseUrl` if running on different host/port
- Update test data IDs to match your actual data
- Modify date ranges for realistic testing

## 📋 Usage Examples

### Example 1: Create a New Trade
```http
POST {{baseUrl}}/api/v1/trades/details
Content-Type: application/json

{
  "portfolioId": "{{portfolioId}}",
  "symbol": "{{symbol}}",
  "userId": "{{userId}}",
  "quantity": 100,
  "price": 150.25,
  "tradeType": "BUY",
  "status": "EXECUTED",
  "strategy": "MOMENTUM",
  "entryDate": "2024-01-15",
  "notes": "Initial position in AAPL"
}
```

### Example 2: Filter Trades by Multiple Criteria
```http
GET {{baseUrl}}/api/v1/trades/filter?portfolioIds={{portfolioIds}}&symbols={{symbols}}&statuses={{statuses}}&startDate={{startDate}}&endDate={{endDate}}&strategies={{strategies}}&page={{page}}&size={{size}}
```

### Example 3: Get Portfolio Performance
```http
GET {{baseUrl}}/api/v1/portfolio-summary/{{portfolioId}}/performance?startDate={{startDate}}&endDate={{endDate}}
```

## 🔧 API Endpoints Summary

### Base URL
```
http://localhost:8073
```

### Main Endpoint Groups
- `/api/v1/trades/*` - Core trade operations
- `/api/v1/trades/calendar/*` - Calendar-based trade queries
- `/api/v1/trade-summary/*` - Trade summary and aggregations
- `/api/v1/analytics/*` - Trade analytics and replays
- `/api/v1/metrics/*` - Trading metrics
- `/api/v1/portfolio-summary/*` - Portfolio management
- `/api/v1/preferences/*` - User preferences
- `/api/v1/journal/*` - Trading journal
- `/api/v1/comparison/*` - Trade/strategy comparisons
- `/api/v1/heatmap/*` - P&L heatmaps
- `/api/v1/filters/*` - Favorite filters
- `/actuator/*` - Spring Boot actuator endpoints

## 🐛 Troubleshooting

### Common Issues & Solutions
- **Connection Refused**: Ensure Docker container is running (`docker-compose up -d`)
- **404 Not Found**: Verify correct API endpoint and port (8073)
- **Authentication Failed**: Update authentication token in environment
- **Invalid JSON**: Check payload format against schema files in `v1/schemas/`

---

## 📚 File Organization

### Quick Links

**Start Here** (if you're new)
1. [v1/README.md](./v1/README.md) - Overview of version 1.0
2. [v1/documentation/QUICK-REFERENCE.md](./v1/documentation/) - API quick lookup
3. [v1/collections/README.md](./v1/collections/) - Available collections

**Working with API**
- [v1/collections/](./v1/collections/) - Import collections into Postman
- [v1/sample-data/](./v1/sample-data/) - Example requests/responses
- [v1/documentation/](./v1/documentation/) - Implementation guides

**Understanding the System**
- [v1/schemas/](./v1/schemas/) - API contract definitions
- [ORGANIZATION-GUIDE.md](./ORGANIZATION-GUIDE.md) - Why files are organized this way
- [v1/documentation/QUICK-REFERENCE.md](./v1/documentation/) - Endpoint reference

### Directory Map
```
postman/
├── README.md                    ← You are here
├── ORGANIZATION-GUIDE.md        ← How it's organized
│
├── v1/                          ← All API v1.0 resources
│   ├── README.md               ← v1 overview & quick start
│   ├── collections/            ← Postman collections
│   ├── schemas/                ← API schemas
│   ├── sample-data/            ← Example payloads
│   └── documentation/          ← Guides & reference
│
└── LEGACY/                      ← Old structure (optional)
```

---

## 🎯 Common Tasks

| Task | How to Do It |
|------|------------|
| **Import API collection** | Go to [v1/collections/](./v1/collections/), import `.postman_collection.json` |
| **Understand an API endpoint** | Check [v1/documentation/QUICK-REFERENCE.md](./v1/documentation/) |
| **Generate code for API** | Read schema in [v1/schemas/](./v1/schemas/), follow code generation guides |
| **Test with sample data** | Find examples in [v1/sample-data/](./v1/sample-data/) |
| **Implement new feature** | Start with [v1/documentation/](./v1/documentation/) guides |
| **Add new version** | Create `v2/` with same structure as `v1/` |

---

## 🔑 Key Features

✅ **Fully Organized** - Everything grouped by purpose  
✅ **Version Ready** - Easy to add v2, v3, etc.  
✅ **Well Documented** - Guides for every API  
✅ **Examples Included** - 50+ sample payloads  
✅ **Scalable** - Grows with your API  
✅ **Maintainable** - Clear structure and naming  

---

## 📝 Version Info

| Version | Release | Status |
|---------|---------|--------|
| **v1.0** | Dec 2025 | ✅ Current Stable |
| **v2.0** | TBD | 📅 Planned |

---

## ❓ FAQ

**Q: Where are all the files?**  
A: Organized in [v1/](./v1/) directory. See [ORGANIZATION-GUIDE.md](./ORGANIZATION-GUIDE.md)

**Q: How do I import a collection?**  
A: File → Import → Choose from [v1/collections/](./v1/collections/)

**Q: Can I still use old files?**  
A: Yes, but new content is in v1/. Check [v1/collections/](./v1/collections/README.md) for status

**Q: How do I add v2?**  
A: Create `v2/` with same structure. See [ORGANIZATION-GUIDE.md](./ORGANIZATION-GUIDE.md)

**Q: What should I read first?**  
A: [v1/README.md](./v1/README.md) → [QUICK-REFERENCE.md](./v1/documentation/) → [Collections](./v1/collections/)

---

## 🚀 Next Steps

### For New Users
1. ✅ Read [v1/README.md](./v1/README.md)
2. ✅ Import a collection from [v1/collections/](./v1/collections/)
3. ✅ Try an example from [v1/sample-data/](./v1/sample-data/)
4. ✅ Read relevant guide from [v1/documentation/](./v1/documentation/)

### For Integrations
1. ✅ Choose your language
2. ✅ Get schema from [v1/schemas/](./v1/schemas/)
3. ✅ Read code generation guide from [v1/documentation/](./v1/documentation/)
4. ✅ Use examples from [v1/sample-data/](./v1/sample-data/)

### For Contributing
1. ✅ Understand structure (this file)
2. ✅ Add to appropriate `v1/` subdirectory
3. ✅ Update relevant README.md
4. ✅ Link from main documentation

---

## 📞 Support

For questions:
1. Check relevant README in [v1/](./v1/)
2. Search [v1/documentation/](./v1/documentation/) guides
3. Review [ORGANIZATION-GUIDE.md](./ORGANIZATION-GUIDE.md)
4. Consult [v1/documentation/QUICK-REFERENCE.md](./v1/documentation/)

---

**Last Updated**: December 2025  
**Status**: ✅ Fully Organized for v1.0 & Future Versions  
**Next**: Ready to add v2.0 with same structure

1. **Connection Refused**
   - Ensure the application is running: `docker-compose ps`
   - Check if port 8073 is accessible: `netstat -an | findstr :8073`

2. **404 Not Found**
   - Verify the endpoint URL in the collection
   - Check if the API path has changed in the application

3. **Authentication Errors**
   - Update the `authToken` environment variable
   - Check if authentication is required for the endpoint

4. **Invalid Request Data**
   - Verify request body format matches API expectations
   - Check required vs optional fields in the API documentation

### Health Check
Always start testing with the health check endpoint:
```http
GET {{baseUrl}}/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## 🔄 Testing Workflow

### Recommended Testing Sequence

1. **Health Check** - Verify application is running
2. **Create Sample Data** - Add test trades, portfolios, users
3. **Test Read Operations** - Retrieve and filter data
4. **Test Update Operations** - Modify existing data
5. **Test Analytics** - Run analytical operations
6. **Test Edge Cases** - Invalid data, missing parameters

### Data Cleanup

After testing, you may want to clean up test data:
- Use DELETE endpoints where available
- Or reset the database/containers

## 📞 Support

For API-related issues:
- Check application logs: `docker-compose logs am-trade-service`
- Review Swagger documentation (if available): `http://localhost:8073/swagger-ui.html`
- Validate request/response formats against the API specification

## 📝 Notes

- All date parameters use ISO 8601 format (yyyy-MM-dd)
- Datetime parameters use ISO 8601 format (yyyy-MM-ddTHH:mm:ss)
- Pagination uses 0-based indexing
- Most endpoints support optional filtering parameters
- Response formats are consistent across similar endpoint types