# AM Trade SDK - Python

Python SDK for AM Trade Management System - A comprehensive client library for trading portfolio management, analytics, and journal operations.

**Version:** 1.0.0  
**Python:** 3.8+  
**Status:** Production Ready

---

## 📦 Installation

### Option 1: Install from Local Build (Development)

```bash
# Build the SDK
cd am-trade-sdk-python
python -m build

# Install the wheel file
pip install dist/am_trade_sdk-1.0.0-py3-none-any.whl
```

### Option 2: Install in Editable Mode (Development)

```bash
# Install in editable mode (changes reflect immediately)
pip install -e /path/to/am-trade-sdk-python
```

### Option 3: Install from PyPI (Production)

```bash
pip install am-trade-sdk
```

### Dependencies

The SDK automatically installs:
- `requests>=2.28.0` - HTTP client
- `pydantic>=1.10.0` - Data validation
- `python-dotenv>=0.19.0` - Environment configuration
- `typing-extensions>=4.0.0` - Type hints

---

## 🚀 Quick Start

```python
from sdk import AmTradeSdk

# Initialize SDK
sdk = AmTradeSdk(
    api_url="https://api.munish.org",
    api_key="your-api-key-here"
)

# Get all trades
trades = sdk.trade_client.get_all_trades(page=0, page_size=20)
print(f"Total trades: {trades['totalCount']}")
```

---

## 🔧 Configuration

### Basic Configuration

```python
from sdk import AmTradeSdk

sdk = AmTradeSdk(
    api_url="https://api.munish.org",
    api_key="your-api-key",
    timeout=30,
    max_retries=3
)
```

### Using Environment Variables

```python
# .env file
API_URL=https://api.munish.org
API_KEY=your-api-key-here
TIMEOUT=30

# Python code
from sdk import AmTradeSdk
from dotenv import load_dotenv

load_dotenv()
sdk = AmTradeSdk()  # Reads from environment
```

### Advanced Configuration

```python
from sdk.config import SdkConfig, ConfigBuilder

config = ConfigBuilder() \
    .api_url("https://api.munish.org") \
    .api_key("your-api-key") \
    .timeout(30) \
    .max_retries(3) \
    .enable_logging(True) \
    .build()

sdk = AmTradeSdk(config=config)
```

---

## 📚 Features & Examples

### 1. Trade Management

#### Get All Trades

```python
# Get paginated trades
response = sdk.trade_client.get_all_trades(page=0, page_size=20)

print(f"Total: {response['totalCount']}")
print(f"Page: {response['page']} of {response['totalPages']}")

for trade in response['trades']:
    print(f"{trade['symbol']}: {trade['pnl']}")
```

**Response Format:**
```json
{
  "trades": [
    {
      "id": "trade-123",
      "portfolio_id": "portfolio-456",
      "symbol": "NIFTY",
      "trade_type": "FUTURES",
      "quantity": 10,
      "entry_price": 18500.00,
      "entry_date": "2025-01-15",
      "status": "WIN",
      "pnl": 5000.00,
      "pnl_percentage": 2.50
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 20,
  "totalPages": 8,
  "isFirst": true,
  "isLast": false
}
```

#### Get Trade by ID

```python
trade = sdk.trade_client.get_trade_by_id("trade-123")

print(f"Symbol: {trade['symbol']}")
print(f"P&L: ₹{trade['pnl']}")
print(f"Status: {trade['status']}")
```

**Response Format:**
```json
{
  "id": "trade-123",
  "portfolio_id": "portfolio-456",
  "symbol": "NIFTY",
  "trade_type": "FUTURES",
  "quantity": 10,
  "entry_price": 18500.00,
  "exit_price": 19000.00,
  "entry_date": "2025-01-15",
  "exit_date": "2025-01-20",
  "status": "WIN",
  "pnl": 5000.00,
  "pnl_percentage": 2.70
}
```

#### Create Trade

```python
new_trade = sdk.trade_client.create_trade(
    portfolio_id="portfolio-456",
    symbol="BANKNIFTY",
    trade_type="OPTIONS",
    quantity=50,
    entry_price=45000.00,
    entry_date="2025-01-15"
)

print(f"Created trade: {new_trade['id']}")
```

### 2. Filter Trades

#### Filter by Status

```python
# Get winning trades only
winning_trades = sdk.trade_client.filter_trades({
    "portfolio_id": "portfolio-456",
    "statuses": ["WIN"],
    "min_pnl": 1000.0
})

print(f"Winning trades: {len(winning_trades['trades'])}")
```

#### Filter by Symbol and Date Range

```python
# Get NIFTY trades from last month
filtered = sdk.trade_client.filter_trades({
    "portfolio_id": "portfolio-456",
    "symbols": ["NIFTY"],
    "start_date": "2025-01-01",
    "end_date": "2025-01-31"
})

for trade in filtered['trades']:
    print(f"{trade['entry_date']}: {trade['symbol']} - ₹{trade['pnl']}")
```

**Response Format:**
```json
{
  "trades": [
    {
      "id": "trade-789",
      "symbol": "NIFTY",
      "entry_date": "2025-01-15",
      "status": "WIN",
      "pnl": 3500.00
    }
  ],
  "totalCount": 25,
  "filters_applied": {
    "symbols": ["NIFTY"],
    "start_date": "2025-01-01",
    "end_date": "2025-01-31"
  }
}
```

### 3. Portfolio Operations

#### Get Portfolio

```python
portfolio = sdk.portfolio_client.get_portfolio_by_id("portfolio-456")

print(f"Name: {portfolio['name']}")
print(f"Total P&L: ₹{portfolio['total_pnl']}")
print(f"Win Rate: {portfolio['win_rate']}%")
```

**Response Format:**
```json
{
  "id": "portfolio-456",
  "name": "My Trading Portfolio",
  "total_trades": 150,
  "total_pnl": 125000.00,
  "win_rate": 65.5,
  "average_pnl": 833.33,
  "created_at": "2024-01-01T00:00:00Z"
}
```

#### Get Portfolio Trades

```python
trades = sdk.trade_client.get_trades_by_portfolio(
    portfolio_id="portfolio-456",
    page=0,
    page_size=50
)

print(f"Portfolio has {trades['totalCount']} trades")
```

### 4. Analytics

#### Get Trade Statistics

```python
stats = sdk.analytics_client.get_trade_metrics("portfolio-456")

print(f"Total Trades: {stats['total_trades']}")
print(f"Win Rate: {stats['win_rate']}%")
print(f"Average P&L: ₹{stats['average_pnl']}")
print(f"Best Trade: ₹{stats['best_trade']}")
print(f"Worst Trade: ₹{stats['worst_trade']}")
```

**Response Format:**
```json
{
  "total_trades": 150,
  "winning_trades": 98,
  "losing_trades": 52,
  "win_rate": 65.33,
  "total_pnl": 125000.00,
  "average_pnl": 833.33,
  "best_trade": 15000.00,
  "worst_trade": -8500.00,
  "sharpe_ratio": 1.85
}
```

#### Get Performance Chart Data

```python
# Get 1-month performance
performance = sdk.analytics_client.get_performance_chart(
    portfolio_id="portfolio-456",
    period="1M"
)

for point in performance['data']:
    print(f"{point['date']}: ₹{point['cumulative_pnl']}")
```

**Response Format:**
```json
{
  "period": "1M",
  "data": [
    {
      "date": "2025-01-01",
      "daily_pnl": 2500.00,
      "cumulative_pnl": 2500.00
    },
    {
      "date": "2025-01-02",
      "daily_pnl": 1800.00,
      "cumulative_pnl": 4300.00
    }
  ]
}
```

### 5. Journal Management

#### Create Journal Entry

```python
entry = sdk.journal_client.create_entry({
    "trade_id": "trade-123",
    "title": "Great NIFTY Trade",
    "notes": "Perfect technical setup. Entry at support level.",
    "tags": ["technical", "support-level"]
})

print(f"Journal entry created: {entry['id']}")
```

#### Get Journal Entries

```python
entries = sdk.journal_client.get_all_entries(
    portfolio_id="portfolio-456",
    page=0,
    page_size=10
)

for entry in entries['entries']:
    print(f"{entry['title']}: {entry['notes'][:50]}...")
```

---

## 🔐 Error Handling

```python
from sdk.exception import (
    ApiException,
    ResourceNotFoundException,
    ValidationException,
    NetworkException
)

try:
    trade = sdk.trade_client.get_trade_by_id("invalid-id")
except ResourceNotFoundException as e:
    print(f"Trade not found: {e}")
except ValidationException as e:
    print(f"Invalid data: {e}")
except NetworkException as e:
    print(f"Network error: {e}")
except ApiException as e:
    print(f"API error: {e.status_code} - {e.message}")
```

---

## 📊 Complete Usage Example

```python
from sdk import AmTradeSdk
from sdk.exception import ApiException

def main():
    # Initialize SDK
    sdk = AmTradeSdk(
        api_url="https://api.munish.org",
        api_key="your-api-key"
    )
    
    try:
        # Get portfolio
        portfolio = sdk.portfolio_client.get_portfolio_by_id("portfolio-456")
        print(f"Portfolio: {portfolio['name']}")
        
        # Get recent trades
        trades = sdk.trade_client.get_all_trades(page=0, page_size=10)
        print(f"Recent trades: {trades['totalCount']}")
        
        # Filter winning trades
        winners = sdk.trade_client.filter_trades({
            "portfolio_id": "portfolio-456",
            "statuses": ["WIN"],
            "min_pnl": 1000.0
        })
        print(f"Winning trades (>₹1000): {len(winners['trades'])}")
        
        # Get analytics
        stats = sdk.analytics_client.get_trade_metrics("portfolio-456")
        print(f"Win Rate: {stats['win_rate']}%")
        
        # Create journal entry
        entry = sdk.journal_client.create_entry({
            "trade_id": trades['trades'][0]['id'],
            "title": "Trade Review",
            "notes": "Excellent execution"
        })
        print(f"Journal entry: {entry['id']}")
        
    except ApiException as e:
        print(f"Error: {e}")
    finally:
        sdk.close()

if __name__ == "__main__":
    main()
```

---

## 🔗 API Endpoints

| Feature | Endpoint | Method |
|---------|----------|--------|
| Get All Trades | `/api/v1/trades` | GET |
| Get Trade by ID | `/api/v1/trades/{id}` | GET |
| Create Trade | `/api/v1/trades` | POST |
| Filter Trades | `/api/v1/trades/filter` | POST |
| Get Portfolio | `/api/v1/portfolio-summary/{id}` | GET |
| Get Analytics | `/api/v1/metrics/{portfolio_id}` | GET |
| Create Journal | `/api/v1/journal` | POST |

---

## 📄 License

Part of AM Trade Management System - All rights reserved

---

**Last Updated:** December 6, 2025  
**Version:** 1.0.0  
**API Base URL:** https://api.munish.org
