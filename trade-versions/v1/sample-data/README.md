# Sample Data - v1

## Overview

This folder contains realistic sample payloads, mock data, and example requests/responses for all API endpoints. Use these as templates for your API calls and for understanding expected data structures.

## Folder Organization

```
sample-data/
├── trade-payloads/          # Trade creation & update examples
├── filter-examples/         # Filter configuration examples
├── journal-entries/         # Trading journal examples
├── portfolio-data/          # Portfolio-level examples
├── analytics-examples/      # Analytics & metrics examples
└── error-examples/          # Error response examples
```

## Sample Data Categories

### 1. Trade Payloads (trade-payloads/)

#### Basic Trade
- Simple equity trade entry
- Minimum required fields
- Use: Quickstart examples

#### Options Trade
- Option contract with derivatives
- Strike price, expiry, option type
- Use: Options trading

#### Futures Trade
- Futures contract example
- Leverage and contract specifications
- Use: Futures trading

#### Complex Trade
- Full trade with all optional fields
- Psychology factors
- Entry/exit reasoning
- Attachments
- Use: Complete example

#### Batch Operations
- Multiple trades for batch creation
- Mixed trade types
- Use: Bulk operations

### 2. Filter Examples (filter-examples/)

#### Intraday Trades Filter
- Filter for same-day trades
- Time-based criteria
- Use: Intraday analysis

#### High-Probability Trades Filter
- Win rate > 60%
- Risk-reward ratio > 2
- Use: Quality filter

#### Momentum Strategy Filter
- Strategy-based filtering
- Tags: momentum, breakout
- Use: Strategy analysis

#### Loss Recovery Trades Filter
- Trades taken after losses
- Psychology factors included
- Use: Pattern analysis

#### High-Risk High-Reward Filter
- Risk-reward ratio > 3
- Large position sizes
- Use: Aggressive trading review

#### Nifty Options Filter
- Nifty 50 index options
- Options-specific criteria
- Use: Options analysis

### 3. Journal Entries (journal-entries/)

#### Minimal Journal Entry
- Required fields only
- Quick note format
- Use: Simple entry example

#### Basic Trade Journal
- Linked to specific trade
- Mood and market sentiment
- Use: Trade documentation

#### Detailed Analysis Entry
- Comprehensive analysis
- Technical and fundamental reasoning
- Multiple attachments
- Use: Complete documentation

#### Psychology-Focused Entry
- Psychology factors
- Trading emotions
- Lesson learned
- Use: Trading psychology

#### Trade Replay Entry
- Entry/exit analysis
- Alternative scenarios
- Outcome comparison
- Use: Trade analysis

### 4. Portfolio Data (portfolio-data/)

#### Portfolio Summary
- Portfolio-level metrics
- Multi-trade aggregation
- Use: Portfolio overview

#### Portfolio Composition
- Asset allocation
- Symbol distribution
- Strategy breakdown
- Use: Portfolio structure

#### Performance Summary
- Win/loss statistics
- P&L metrics
- Risk metrics
- Use: Performance review

### 5. Analytics Examples (analytics-examples/)

#### Daily Metrics
- Daily performance summary
- Intraday trade statistics
- Use: Daily review

#### Weekly Summary
- Weekly aggregated metrics
- Strategy performance
- Use: Weekly analysis

#### Monthly Report
- Monthly comprehensive analysis
- Seasonal patterns
- Use: Monthly review

#### Win Rate Analysis
- Winning vs losing trades
- Win rate percentage
- Average win/loss
- Use: Win rate analysis

#### Risk Analysis
- Risk metrics summary
- Drawdown analysis
- Risk-reward distribution
- Use: Risk assessment

#### Heatmap Data
- P&L by day/strategy
- Performance visualization
- Use: Visual analysis

### 6. Error Examples (error-examples/)

#### Validation Error
- Missing required field
- Use: Error handling

#### Not Found Error
- Resource doesn't exist
- Use: 404 handling

#### Conflict Error
- Duplicate or conflicting data
- Use: Conflict handling

#### Server Error
- Internal server error
- Use: 500 handling

## Using Sample Data

### Method 1: Copy-Paste in Postman

```
1. Open collection request
2. Go to "Body" tab
3. Click "Raw" and select "JSON"
4. Copy content from sample file
5. Modify as needed
6. Click Send
```

### Method 2: Environment Variables

```json
{
  "base_url": "http://localhost:8080",
  "api_version": "v1",
  "user_id": "ssd2658",
  "portfolio_id": "163d0143-4fcb-480c-ac20-622f14e0e293"
}
```

### Method 3: Data-Driven Testing

```
1. Import sample data as CSV/JSON
2. Set up Postman data file
3. Run collection with data iteration
4. Postman replaces {{variables}} with data values
```

## Real-World Examples

### Scenario 1: Create and Analyze Trade

**Sample Files Used:**
1. `trade-payloads/basic-trade.json` - Create trade
2. `journal-entries/basic-trade-journal.json` - Add journal entry
3. Analytics response example

**Steps:**
```
1. POST /api/v1/trades/details (use basic-trade.json)
2. GET /api/v1/journal/trade/{tradeId} (returns journal entry format)
3. POST /api/v1/filters (use high-probability-filter.json)
4. Review trade metrics
```

### Scenario 2: Filter and Compare Trades

**Sample Files Used:**
1. `filter-examples/momentum-strategy-filter.json`
2. `filter-examples/high-risk-reward-filter.json`
3. Trade comparison response

**Steps:**
```
1. POST /api/v1/trades/filter (apply momentum filter)
2. POST /api/v1/trades/filter (apply high-risk filter)
3. Compare results using TradeComparisonController
```

### Scenario 3: Portfolio Analysis

**Sample Files Used:**
1. `portfolio-data/portfolio-summary.json`
2. `analytics-examples/monthly-report.json`
3. `analytics-examples/heatmap-data.json`

**Steps:**
```
1. GET /api/v1/portfolio-summary/{portfolioId}
2. POST /api/v1/analytics/monthly-report
3. GET /api/v1/heatmap
```

## Data Generation & Modification

### Generating Variations

**From Basic Template:**
```json
{
  "tradeId": "TRD-2025-{{$randomInt(1000, 9999)}}",
  "symbol": "{{$randomChoice(['NIFTY', 'BANKNIFTY', 'FINNIFTY'])}}",
  "status": "{{$randomChoice(['WIN', 'LOSS', 'BREAKEVEN'])}}",
  "metrics": {
    "profitLoss": {{$randomInt(500, 50000)}}
  }
}
```

### Date Range Modifications

```json
{
  "dateRange": {
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }
}
```

### Symbol Variations

```json
{
  "symbols": ["NIFTY", "BANKNIFTY", "FINNIFTY", "SENSEX"]
}
```

## Best Practices

### When Using Sample Data

1. **Always Copy**: Don't modify original files
   ```bash
   cp filter-examples/momentum-strategy-filter.json my-filter.json
   ```

2. **Match Your Environment**: Update IDs and values
   ```json
   {
     "portfolioId": "YOUR_PORTFOLIO_ID",
     "userId": "YOUR_USER_ID"
   }
   ```

3. **Validate Structure**: Check against schema
   - Reference `../schemas/`
   - Validate using tools
   - Check required fields

4. **Test Edge Cases**: Use error examples
   - Missing fields
   - Invalid values
   - Boundary conditions

### Creating New Examples

**Template:**
```json
{
  "name": "example-name",
  "description": "What this example demonstrates",
  "category": "trade|filter|journal|portfolio|analytics",
  "use_case": "When to use this example",
  "payload": { ... }
}
```

## Sample Data Statistics

| Category | Files | Scenarios |
|----------|-------|-----------|
| Trade Payloads | 8+ | Basic, Options, Futures, Complex, Batch |
| Filters | 6+ | Momentum, Risk, Psychology, Time-based |
| Journal Entries | 5+ | Minimal, Detailed, Psychology, Replay |
| Portfolio Data | 3+ | Summary, Composition, Performance |
| Analytics | 6+ | Daily, Weekly, Monthly, Heatmap |
| Error Examples | 4+ | Validation, NotFound, Conflict, Server |

## Testing with Sample Data

### Postman Testing Workflow

```
1. Load sample data
2. Update environment variables
3. Run Pre-request Script (if needed)
4. Send request
5. Validate response against schema
6. Check tests in "Tests" tab
```

### Automated Testing

```bash
# Using Newman (Postman CLI)
newman run collection.json \
  -d sample-data.json \
  -e environment.json \
  -r cli,json
```

### Integration Testing

```javascript
// Load sample data in test
const sampleData = require('./sample-data/trade-payloads/basic-trade.json');
const response = await api.createTrade(sampleData);
expect(response.status).toBe(201);
```

## Common Use Cases

### "I want to create a trade"
→ Use `trade-payloads/basic-trade.json`

### "I want to filter for momentum trades"
→ Use `filter-examples/momentum-strategy-filter.json`

### "I want to add a journal entry"
→ Use `journal-entries/basic-trade-journal.json`

### "I want to see portfolio metrics"
→ Use `portfolio-data/portfolio-summary.json`

### "I want to understand error handling"
→ Use `error-examples/validation-error.json`

## Updating Sample Data

When API changes:
1. Update corresponding sample files
2. Verify against schema
3. Test with Postman
4. Document changes in changelog
5. Update examples/documentation

## Related Resources

- **Collections**: See `../collections/` for API operations
- **Schemas**: See `../schemas/` for validation rules
- **Documentation**: See `../documentation/` for detailed guides

## Version Information

- **Sample Data Format**: JSON
- **API Version**: v1.0.0
- **Last Updated**: December 2025
- **Scenarios Covered**: 50+

---

**Pro Tips**:
1. Copy sample files before modifying
2. Use Postman environments for variable substitution
3. Keep realistic data for better testing
4. Document custom variations you create
5. Share useful examples with team
