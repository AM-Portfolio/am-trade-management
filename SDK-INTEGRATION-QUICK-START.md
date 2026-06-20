# AM Trade SDK - Integration Quick Start

**TL;DR** - Get started with AM Trade SDK in 5 minutes

---

## 🚀 Java: 3 Steps to Integration

### 1️⃣ Add to `pom.xml`
```xml
<dependency>
    <groupId>am.trade</groupId>
    <artifactId>am-trade-sdk-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2️⃣ Create `application.yml`
```yaml
am-trade:
  api-url: http://am-trade-system:8073
  api-key: your-api-key
```

### 3️⃣ Use in Your Code
```java
@Autowired
private AmTradeSdk amTradeSdk;

// Get FREE tier trades
Map<String, Object> trades = amTradeSdk.getTradeClient()
    .getTradesByFreeTab(0, 20);

// Get NIFTY trades (FREE tier)
Map<String, Object> nifty = amTradeSdk.getTradeClient()
    .getTradesByFreeTabAndSymbol("NIFTY", 0, 20);

// Get winning trades (FREE tier)
Map<String, Object> wins = amTradeSdk.getTradeClient()
    .getTradesByFreeTabAndStatus("WIN", 0, 20);
```

---

## 🐍 Python: 3 Steps to Integration

### 1️⃣ Install Package
```bash
pip install am-trade-sdk
```

### 2️⃣ Create Config
```python
from am_trade_sdk import AmTradeSdk

sdk = AmTradeSdk(
    api_url="http://am-trade-system:8073",
    api_key="your-api-key"
)
```

### 3️⃣ Use in Your Code
```python
# Get FREE tier trades
trades = sdk.trade_client.get_trades_by_free_tab(page=0, page_size=20)

# Get NIFTY trades (FREE tier)
nifty = sdk.trade_client.get_trades_by_free_tab_and_symbol(
    symbol="NIFTY", page=0, page_size=20
)

# Get winning trades (FREE tier)
wins = sdk.trade_client.get_trades_by_free_tab_and_status(
    status="WIN", page=0, page_size=20
)
```

---

## 📋 Available Methods

### Trade Operations

| Operation | Java | Python | Purpose |
|-----------|------|--------|---------|
| Get all | `getAllTrades()` | `get_all_trades()` | Paginated list |
| Get by ID | `getTradeById(id)` | `get_trade_by_id(id)` | Single trade |
| **FREE Tab** | **`getTradesByFreeTab()`** | **`get_trades_by_free_tab()`** | **NEW: FREE tier** |
| **FREE + Symbol** | **`getTradesByFreeTabAndSymbol()`** | **`get_trades_by_free_tab_and_symbol()`** | **NEW: FREE + filter** |
| **FREE + Status** | **`getTradesByFreeTabAndStatus()`** | **`get_trades_by_free_tab_and_status()`** | **NEW: FREE + status** |
| Create | `createTrade()` | `create_trade()` | New trade |
| Update | `updateTrade()` | `update_trade()` | Edit trade |
| Delete | `deleteTrade()` | `delete_trade()` | Remove trade |
| Filter | `filterTrades()` | `filter_trades()` | Advanced search |
| Batch Create | `batchCreateTrades()` | `batch_create_trades()` | Multiple trades |

### Portfolio Operations
```java
// Java
amTradeSdk.getPortfolioClient().getPortfolioById("id")

// Python
sdk.portfolio_client.get_portfolio_by_id("id")
```

### Journal Operations
```java
// Java
amTradeSdk.getJournalClient().createJournal("trade-id", "title", "notes")

// Python
sdk.journal_client.create_journal("trade-id", "title", "notes")
```

---

## 🔑 Important: API Key

Get your API key from the AM Trade Management system:
```bash
# Backend generates JWT token with tier
# Pass as api_key to SDK
# SDK automatically extracts tier and filters responses
```

**Response Format (FREE tier automatically shows 10 fields):**
```json
{
  "trades": [
    {
      "id": "123",
      "portfolio_id": "456",
      "symbol": "NIFTY",
      "trade_type": "FUTURES",
      "quantity": 10,
      "entry_price": 18500.0,
      "entry_date": "2025-01-15",
      "status": "WIN",
      "pnl": 5000.0,
      "pnl_percentage": 2.5
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 20,
  "totalPages": 8
}
```

---

## ⚡ Common Patterns

### Pattern 1: Get and Display Trades
```java
// Java
Map<String, Object> result = amTradeSdk.getTradeClient()
    .getTradesByFreeTab(0, 20);

List<Trade> trades = (List<Trade>) result.get("trades");
for (Trade trade : trades) {
    System.out.println(trade.getSymbol() + ": " + trade.getPnl());
}
```

```python
# Python
result = sdk.trade_client.get_trades_by_free_tab(page=0, page_size=20)
trades = result.get("trades", [])

for trade in trades:
    print(f"{trade['symbol']}: {trade['pnl']}")
```

### Pattern 2: Filter and Count
```java
// Java
Map<String, Object> winning = amTradeSdk.getTradeClient()
    .getTradesByFreeTabAndStatus("WIN", 0, 20);

long winCount = (long) winning.get("totalCount");
System.out.println("Winning trades: " + winCount);
```

```python
# Python
winning = sdk.trade_client.get_trades_by_free_tab_and_status(
    status="WIN", page=0, page_size=20
)
print(f"Winning trades: {winning['totalCount']}")
```

### Pattern 3: Pagination
```java
// Java
int totalPages = (int) result.get("totalPages");
for (int page = 0; page < totalPages; page++) {
    Map<String, Object> pageData = amTradeSdk.getTradeClient()
        .getTradesByFreeTab(page, 20);
    // Process page data
}
```

```python
# Python
total_pages = result['totalPages']
for page in range(total_pages):
    page_data = sdk.trade_client.get_trades_by_free_tab(
        page=page, page_size=20
    )
    # Process page data
```

### Pattern 4: Error Handling
```java
// Java
try {
    Map<String, Object> trades = amTradeSdk.getTradeClient()
        .getTradesByFreeTab(0, 20);
} catch (ResourceNotFoundException e) {
    System.err.println("Not found: " + e.getMessage());
} catch (ApiException e) {
    System.err.println("API error: " + e.getMessage());
} catch (Exception e) {
    System.err.println("Error: " + e.getMessage());
}
```

```python
# Python
try:
    trades = sdk.trade_client.get_trades_by_free_tab()
except Exception as e:
    print(f"Error: {e}")
```

---

## 🎯 Use Case Examples

### Dashboard
```java
// Show last 20 FREE tier trades + stats
Map<String, Object> trades = amTradeSdk.getTradeClient().getTradesByFreeTab(0, 20);
Map<String, Object> wins = amTradeSdk.getTradeClient().getTradesByFreeTabAndStatus("WIN", 0, 1);

model.addAttribute("trades", trades.get("trades"));
model.addAttribute("totalTrades", trades.get("totalCount"));
model.addAttribute("winCount", wins.get("totalCount"));
```

### Export
```python
# Export FREE tier trades to CSV
trades = sdk.trade_client.get_trades_by_free_tab(page=0, page_size=1000)
with open("trades.csv", "w") as f:
    writer = csv.DictWriter(f, fieldnames=[...])
    writer.writerows(trades["trades"])
```

### Sync
```java
// Sync FREE tier trades hourly
@Scheduled(fixedRate = 3600000)
public void syncTrades() {
    Map<String, Object> trades = amTradeSdk.getTradeClient()
        .getTradesByFreeTab(0, 20);
    // Save to local database
}
```

---

## 📦 Installation Methods

### Java (Maven)
```bash
# Published to Maven Central
# Just add to pom.xml and run
mvn clean install

# Or for local development
cd am-trade-sdk-core
mvn clean install
```

### Python (pip)
```bash
# Published to PyPI
pip install am-trade-sdk

# Or for local development
cd am-trade-sdk-python
pip install -e .
```

---

## ⚙️ Configuration

### Java
```yaml
# application.yml
am-trade:
  api-url: ${AM_TRADE_API_URL:http://localhost:8073}
  api-key: ${AM_TRADE_API_KEY}
```

### Python
```python
import os
from dotenv import load_dotenv

load_dotenv()
sdk = AmTradeSdk(
    api_url=os.getenv('AM_TRADE_API_URL'),
    api_key=os.getenv('AM_TRADE_API_KEY')
)
```

---

## 🔗 Links

- **Full Guide:** See `SDK-INTEGRATION-GUIDE.md`
- **SDK Docs:** See `SDK-README.md`
- **Tier System:** See `TIER-BASED-ACCESS-CONTROL.md`
- **Quick Ref:** See `TIER-QUICK-REFERENCE.md`

---

## ✅ Checklist

- [ ] Install SDK (Maven or pip)
- [ ] Configure API URL and key
- [ ] Create service/wrapper class
- [ ] Add REST endpoint(s)
- [ ] Test with FREE tab methods
- [ ] Handle errors properly
- [ ] Add to tests
- [ ] Deploy

---

**Version:** 1.0.0  
**Last Updated:** December 5, 2025  
**Status:** Production Ready
