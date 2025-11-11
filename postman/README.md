# AM Trade Management - Postman Collections

This directory contains comprehensive Postman collections and environment configurations for testing the AM Trade Management API system.

## 🚀 Getting Started

### Prerequisites
- Postman Desktop App or Postman Web Client
- Docker and Docker Compose installed
- AM Trade Management application running

### Starting the Application
```bash
# Navigate to the project root
cd a:\InfraCode\AM-Portfolio\am-trade-managment

# Start the application using Docker Compose
docker-compose up -d

# Check if the application is running
curl http://localhost:8073/actuator/health
```

## 📁 Collection Files

### 1. AM-Trade-Management-API.postman_collection.json
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

### Common Issues

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