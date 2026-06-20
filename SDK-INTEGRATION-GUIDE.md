# Using AM Trade SDK in Another Repository

**Date:** December 5, 2025  
**SDKs Available:** Java (JAR) and Python (Package)

---

## 📋 Quick Overview

The AM Trade SDK provides two distributions:
- **Java SDK:** Published to Maven Central (or local Maven repo)
- **Python SDK:** Published to PyPI (or local pip repo)

Choose based on your project's language and dependencies.

---

## 🚀 Option 1: Using Java SDK in Another Java/Spring Boot Project

### Step 1: Add Maven Dependency

In your `pom.xml`, add the dependency to your project:

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.yourcompany</groupId>
    <artifactId>your-project</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <!-- AM Trade SDK -->
        <dependency>
            <groupId>am.trade</groupId>
            <artifactId>am-trade-sdk-core</artifactId>
            <version>1.0.0</version>
        </dependency>
        
        <!-- Other dependencies -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>3.1.0</version>
        </dependency>
    </dependencies>
</project>
```

### Step 2: Initialize SDK in Your Spring Boot Application

```java
package com.yourcompany.service;

import am.trade.sdk.AmTradeSdk;
import am.trade.sdk.config.SdkConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class AmTradeConfig {
    
    @Value("${am-trade.api-url:http://localhost:8073}")
    private String apiUrl;
    
    @Value("${am-trade.api-key}")
    private String apiKey;
    
    @Bean
    public AmTradeSdk amTradeSdk() {
        SdkConfiguration config = SdkConfiguration.builder()
            .apiUrl(apiUrl)
            .apiKey(apiKey)
            .timeout(30)
            .retryAttempts(3)
            .build();
        
        return new AmTradeSdk(config);
    }
}
```

### Step 3: Inject and Use in Your Service

```java
package com.yourcompany.service;

import am.trade.sdk.AmTradeSdk;
import am.trade.sdk.dto.TradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;

@Service
public class TradeIntegrationService {
    
    @Autowired
    private AmTradeSdk amTradeSdk;
    
    /**
     * Example: Get all trades from AM Trade system
     */
    public void getAllTrades() {
        try {
            Map<String, Object> trades = amTradeSdk.getTradeClient()
                .getAllTrades(0, 20);
            
            System.out.println("Total trades: " + trades.get("totalCount"));
        } catch (Exception e) {
            System.err.println("Error fetching trades: " + e.getMessage());
        }
    }
    
    /**
     * Example: Get FREE tier trades (NEW)
     */
    public void getFreeTabTrades() {
        try {
            Map<String, Object> freeTrades = amTradeSdk.getTradeClient()
                .getTradesByFreeTab(0, 20);
            
            System.out.println("FREE tier trades: " + freeTrades.get("totalCount"));
            // Automatically filtered to show only 10 fields
        } catch (Exception e) {
            System.err.println("Error fetching FREE tab trades: " + e.getMessage());
        }
    }
    
    /**
     * Example: Get FREE tier trades by symbol
     */
    public void getFreeTabTradesBySymbol(String symbol) {
        try {
            Map<String, Object> trades = amTradeSdk.getTradeClient()
                .getTradesByFreeTabAndSymbol(symbol, 0, 20);
            
            System.out.println("FREE tier trades for " + symbol + ": " 
                + trades.get("totalCount"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Example: Create a trade
     */
    public void createTrade() {
        try {
            TradeDTO.TradeCreateRequest request = TradeDTO.TradeCreateRequest.builder()
                .portfolioId("portfolio-123")
                .symbol("NIFTY")
                .tradeType("FUTURES")
                .quantity(10)
                .entryPrice(18500.00)
                .entryDate("2025-01-15")
                .notes("Test trade")
                .build();
            
            TradeDTO.TradeResponse response = amTradeSdk.getTradeClient()
                .createTrade(request);
            
            System.out.println("Created trade: " + response.getId());
        } catch (Exception e) {
            System.err.println("Error creating trade: " + e.getMessage());
        }
    }
    
    /**
     * Example: Get portfolio
     */
    public void getPortfolio(String portfolioId) {
        try {
            Map<String, Object> portfolio = amTradeSdk.getPortfolioClient()
                .getPortfolioById(portfolioId);
            
            System.out.println("Portfolio: " + portfolio.get("name"));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Example: Filter trades
     */
    public void filterTrades() {
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("symbol", "NIFTY");
            filters.put("status", "WIN");
            filters.put("minPnL", 1000.0);
            
            Map<String, Object> results = amTradeSdk.getTradeClient()
                .filterTrades(filters);
            
            System.out.println("Filtered results: " + results.get("totalCount"));
        } catch (Exception e) {
            System.err.println("Error filtering trades: " + e.getMessage());
        }
    }
}
```

### Step 4: Configure Application Properties

In `application.yml` or `application.properties`:

```yaml
am-trade:
  api-url: http://am-trade-system:8073
  api-key: your-api-key-from-backend
```

Or for properties file:
```properties
am-trade.api-url=http://am-trade-system:8073
am-trade.api-key=your-api-key-from-backend
```

### Step 5: Create a REST Controller to Expose Operations

```java
package com.yourcompany.controller;

import com.yourcompany.service.TradeIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/am-trade-integration")
public class TradeIntegrationController {
    
    @Autowired
    private TradeIntegrationService tradeService;
    
    @GetMapping("/trades")
    public ResponseEntity<?> getAllTrades() {
        tradeService.getAllTrades();
        return ResponseEntity.ok("Trades fetched");
    }
    
    @GetMapping("/trades/free-tab")
    public ResponseEntity<?> getFreeTabTrades() {
        tradeService.getFreeTabTrades();
        return ResponseEntity.ok("FREE tab trades fetched");
    }
    
    @GetMapping("/trades/free-tab/{symbol}")
    public ResponseEntity<?> getFreeTabTradesBySymbol(@PathVariable String symbol) {
        tradeService.getFreeTabTradesBySymbol(symbol);
        return ResponseEntity.ok("FREE tab trades for " + symbol + " fetched");
    }
    
    @PostMapping("/trades")
    public ResponseEntity<?> createTrade() {
        tradeService.createTrade();
        return ResponseEntity.ok("Trade created");
    }
}
```

---

## 🐍 Option 2: Using Python SDK in Another Python Project

### Step 1: Install SDK from PyPI

```bash
# Basic installation
pip install am-trade-sdk

# Or specific version
pip install am-trade-sdk==1.0.0

# In requirements.txt
echo "am-trade-sdk>=1.0.0" >> requirements.txt
pip install -r requirements.txt
```

### Step 2: Initialize SDK in Your Application

**Django Example:**

```python
# settings.py
AM_TRADE_SDK_CONFIG = {
    'api_url': os.getenv('AM_TRADE_API_URL', 'http://localhost:8073'),
    'api_key': os.getenv('AM_TRADE_API_KEY'),
    'timeout': 30,
}
```

```python
# services/am_trade_service.py
from am_trade_sdk import AmTradeSdk
from django.conf import settings

class AMTradeService:
    def __init__(self):
        config = settings.AM_TRADE_SDK_CONFIG
        self.sdk = AmTradeSdk(
            api_url=config['api_url'],
            api_key=config['api_key']
        )
    
    def get_all_trades(self, page=0, page_size=20):
        """Get all trades"""
        return self.sdk.trade_client.get_all_trades(page, page_size)
    
    def get_free_tab_trades(self, page=0, page_size=20):
        """Get FREE tier trades (NEW)"""
        return self.sdk.trade_client.get_trades_by_free_tab(page, page_size)
    
    def get_free_tab_trades_by_symbol(self, symbol, page=0, page_size=20):
        """Get FREE tier trades by symbol"""
        return self.sdk.trade_client.get_trades_by_free_tab_and_symbol(
            symbol, page, page_size
        )
    
    def get_free_tab_trades_by_status(self, status, page=0, page_size=20):
        """Get FREE tier trades by status"""
        return self.sdk.trade_client.get_trades_by_free_tab_and_status(
            status, page, page_size
        )
    
    def create_trade(self, portfolio_id, symbol, trade_type, quantity, 
                    entry_price, entry_date, notes=None):
        """Create a new trade"""
        return self.sdk.trade_client.create_trade(
            portfolio_id=portfolio_id,
            symbol=symbol,
            trade_type=trade_type,
            quantity=quantity,
            entry_price=entry_price,
            entry_date=entry_date,
            notes=notes
        )
    
    def get_portfolio(self, portfolio_id):
        """Get portfolio by ID"""
        return self.sdk.portfolio_client.get_portfolio_by_id(portfolio_id)
    
    def filter_trades(self, **filters):
        """Filter trades with custom criteria"""
        return self.sdk.trade_client.filter_trades(**filters)
```

### Step 3: Create Django Views

```python
# views.py
from django.http import JsonResponse
from django.views import View
from services.am_trade_service import AMTradeService

class TradeListView(View):
    def __init__(self):
        self.trade_service = AMTradeService()
    
    def get(self, request):
        """Get all trades"""
        try:
            trades = self.trade_service.get_all_trades()
            return JsonResponse(trades)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

class FreeTabTradeListView(View):
    def __init__(self):
        self.trade_service = AMTradeService()
    
    def get(self, request):
        """Get FREE tier trades"""
        try:
            page = int(request.GET.get('page', 0))
            page_size = int(request.GET.get('page_size', 20))
            
            trades = self.trade_service.get_free_tab_trades(page, page_size)
            return JsonResponse(trades)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

class FreeTabTradeBySymbolView(View):
    def __init__(self):
        self.trade_service = AMTradeService()
    
    def get(self, request, symbol):
        """Get FREE tier trades by symbol"""
        try:
            page = int(request.GET.get('page', 0))
            page_size = int(request.GET.get('page_size', 20))
            
            trades = self.trade_service.get_free_tab_trades_by_symbol(
                symbol, page, page_size
            )
            return JsonResponse(trades)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)

class CreateTradeView(View):
    def __init__(self):
        self.trade_service = AMTradeService()
    
    def post(self, request):
        """Create new trade"""
        try:
            import json
            data = json.loads(request.body)
            
            trade = self.trade_service.create_trade(
                portfolio_id=data['portfolio_id'],
                symbol=data['symbol'],
                trade_type=data['trade_type'],
                quantity=data['quantity'],
                entry_price=data['entry_price'],
                entry_date=data['entry_date'],
                notes=data.get('notes')
            )
            return JsonResponse(trade)
        except Exception as e:
            return JsonResponse({'error': str(e)}, status=500)
```

### Step 4: Configure URLs

```python
# urls.py
from django.urls import path
from views import (
    TradeListView,
    FreeTabTradeListView,
    FreeTabTradeBySymbolView,
    CreateTradeView
)

urlpatterns = [
    path('api/v1/trades/', TradeListView.as_view()),
    path('api/v1/trades/free-tab/', FreeTabTradeListView.as_view()),
    path('api/v1/trades/free-tab/<str:symbol>/', FreeTabTradeBySymbolView.as_view()),
    path('api/v1/trades/create/', CreateTradeView.as_view()),
]
```

### Step 5: FastAPI Example (Alternative)

```python
# main.py
from fastapi import FastAPI, HTTPException
from services.am_trade_service import AMTradeService

app = FastAPI(title="AM Trade Integration API")
trade_service = AMTradeService()

@app.get("/api/v1/trades")
async def get_all_trades(page: int = 0, page_size: int = 20):
    """Get all trades"""
    try:
        return trade_service.get_all_trades(page, page_size)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/v1/trades/free-tab")
async def get_free_tab_trades(page: int = 0, page_size: int = 20):
    """Get FREE tier trades"""
    try:
        return trade_service.get_free_tab_trades(page, page_size)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/api/v1/trades/free-tab/{symbol}")
async def get_free_tab_trades_by_symbol(symbol: str, page: int = 0, 
                                       page_size: int = 20):
    """Get FREE tier trades by symbol"""
    try:
        return trade_service.get_free_tab_trades_by_symbol(symbol, page, page_size)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/trades")
async def create_trade(trade_data: dict):
    """Create new trade"""
    try:
        return trade_service.create_trade(
            portfolio_id=trade_data['portfolio_id'],
            symbol=trade_data['symbol'],
            trade_type=trade_data['trade_type'],
            quantity=trade_data['quantity'],
            entry_price=trade_data['entry_price'],
            entry_date=trade_data['entry_date'],
            notes=trade_data.get('notes')
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

---

## 🔧 Option 3: Local Development (Without Published Package)

If you're developing locally before publishing to Maven Central or PyPI:

### For Java SDK

1. **Build locally:**
```bash
cd am-trade-sdk-core
mvn clean install
```

2. **Use from local Maven repo:**
```xml
<dependency>
    <groupId>am.trade</groupId>
    <artifactId>am-trade-sdk-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### For Python SDK

1. **Install locally in editable mode:**
```bash
cd am-trade-sdk-python
pip install -e .
```

2. **Or build and install:**
```bash
cd am-trade-sdk-python
python -m pip install --upgrade build
python -m build
pip install dist/am_trade_sdk-1.0.0-py3-none-any.whl
```

---

## 📚 Common Use Cases

### Use Case 1: Sync Trades to External Database

**Java:**
```java
@Service
public class TradeSyncService {
    
    @Autowired
    private AmTradeSdk amTradeSdk;
    
    @Autowired
    private TradeRepository tradeRepository;
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void syncTrades() {
        try {
            // Get trades from AM Trade system
            Map<String, Object> trades = amTradeSdk.getTradeClient()
                .getTradesByFreeTab(0, 20);
            
            // Save to local database
            List<Trade> tradeList = (List<Trade>) trades.get("trades");
            tradeRepository.saveAll(tradeList);
            
            log.info("Synced {} trades", tradeList.size());
        } catch (Exception e) {
            log.error("Error syncing trades", e);
        }
    }
}
```

**Python:**
```python
from apscheduler.schedulers.background import BackgroundScheduler
from services.am_trade_service import AMTradeService

scheduler = BackgroundScheduler()
trade_service = AMTradeService()

def sync_trades():
    """Sync trades every 5 minutes"""
    try:
        trades = trade_service.get_free_tab_trades()
        trade_list = trades.get('trades', [])
        # Save to database
        for trade in trade_list:
            # db.session.add(Trade(**trade))
            pass
        # db.session.commit()
        print(f"Synced {len(trade_list)} trades")
    except Exception as e:
        print(f"Error syncing trades: {e}")

scheduler.add_job(sync_trades, 'interval', minutes=5)
scheduler.start()
```

### Use Case 2: Display Dashboard with FREE Tier Data

**Java REST API:**
```java
@GetMapping("/dashboard/summary")
public ResponseEntity<?> getDashboardSummary() {
    try {
        // Get FREE tier trades
        Map<String, Object> freeTrades = amTradeSdk.getTradeClient()
            .getTradesByFreeTab(0, 20);
        
        // Get winning trades
        Map<String, Object> winningTrades = amTradeSdk.getTradeClient()
            .getTradesByFreeTabAndStatus("WIN", 0, 20);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalTrades", freeTrades.get("totalCount"));
        dashboard.put("winningTrades", winningTrades.get("totalCount"));
        dashboard.put("trades", freeTrades.get("trades"));
        
        return ResponseEntity.ok(dashboard);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
```

**Python API:**
```python
@app.get("/dashboard/summary")
async def get_dashboard_summary():
    """Get dashboard summary with FREE tier data"""
    try:
        free_trades = trade_service.get_free_tab_trades(page=0, page_size=20)
        winning_trades = trade_service.get_free_tab_trades_by_status(
            "WIN", page=0, page_size=20
        )
        
        return {
            "totalTrades": free_trades["totalCount"],
            "winningTrades": winning_trades["totalCount"],
            "trades": free_trades["trades"]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
```

### Use Case 3: Export FREE Tier Trades to CSV

**Python:**
```python
import csv
from datetime import datetime

def export_free_tab_trades_to_csv():
    """Export FREE tier trades to CSV"""
    try:
        trades = trade_service.get_free_tab_trades(page=0, page_size=100)
        trade_list = trades.get('trades', [])
        
        filename = f"trades_{datetime.now().strftime('%Y%m%d_%H%M%S')}.csv"
        
        with open(filename, 'w', newline='') as csvfile:
            fieldnames = ['id', 'portfolio_id', 'symbol', 'trade_type', 
                         'quantity', 'entry_price', 'entry_date', 'status', 
                         'pnl', 'pnl_percentage']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            
            writer.writeheader()
            for trade in trade_list:
                writer.writerow(trade)
        
        print(f"Exported {len(trade_list)} trades to {filename}")
        return filename
    except Exception as e:
        print(f"Error exporting trades: {e}")
        return None
```

---

## ⚙️ Configuration Best Practices

### 1. Use Environment Variables

**Java:**
```properties
# application.yml
am-trade:
  api-url: ${AM_TRADE_API_URL}
  api-key: ${AM_TRADE_API_KEY}
  timeout: ${AM_TRADE_TIMEOUT:30}
```

**Python:**
```python
import os
from dotenv import load_dotenv

load_dotenv()

SDK_CONFIG = {
    'api_url': os.getenv('AM_TRADE_API_URL'),
    'api_key': os.getenv('AM_TRADE_API_KEY'),
    'timeout': int(os.getenv('AM_TRADE_TIMEOUT', 30))
}
```

### 2. Create Configuration Classes

**Java:**
```java
@Configuration
public class SdkConfiguration {
    
    @Value("${am-trade.api-url}")
    private String apiUrl;
    
    @Value("${am-trade.api-key}")
    private String apiKey;
    
    @Bean
    public AmTradeSdk amTradeSdk() {
        return new AmTradeSdk(
            SdkConfiguration.builder()
                .apiUrl(apiUrl)
                .apiKey(apiKey)
                .build()
        );
    }
}
```

### 3. Add Error Handling Middleware

**Java:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException e) {
        return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
```

**Python:**
```python
from fastapi import FastAPI, HTTPException
from fastapi.exceptions import RequestValidationError
from starlette.middleware.error import ServerErrorMiddleware

app = FastAPI()

@app.exception_handler(Exception)
async def general_exception_handler(request, exc):
    return JSONResponse(
        status_code=500,
        content={"error": str(exc)}
    )
```

---

## 🧪 Testing Integration

### Java Unit Tests

```java
@SpringBootTest
public class AmTradeSdkIntegrationTest {
    
    @Autowired
    private AmTradeSdk amTradeSdk;
    
    @Test
    public void testGetFreeTabTrades() {
        Map<String, Object> result = amTradeSdk.getTradeClient()
            .getTradesByFreeTab(0, 20);
        
        assertNotNull(result);
        assertTrue(result.containsKey("trades"));
        assertTrue(result.containsKey("totalCount"));
    }
    
    @Test
    public void testGetFreeTabTradesBySymbol() {
        Map<String, Object> result = amTradeSdk.getTradeClient()
            .getTradesByFreeTabAndSymbol("NIFTY", 0, 20);
        
        assertNotNull(result);
        assertFalse(((List<?>) result.get("trades")).isEmpty());
    }
}
```

### Python Unit Tests

```python
import pytest
from services.am_trade_service import AMTradeService

@pytest.fixture
def trade_service():
    return AMTradeService()

def test_get_free_tab_trades(trade_service):
    result = trade_service.get_free_tab_trades()
    assert result is not None
    assert "trades" in result
    assert "totalCount" in result

def test_get_free_tab_trades_by_symbol(trade_service):
    result = trade_service.get_free_tab_trades_by_symbol("NIFTY")
    assert result is not None
    assert len(result["trades"]) > 0
```

---

## 📊 Example: Complete Project Structure

### Java Project Structure
```
your-java-project/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/yourcompany/
│   │   │       ├── config/
│   │   │       │   └── AmTradeConfig.java
│   │   │       ├── controller/
│   │   │       │   └── TradeController.java
│   │   │       ├── service/
│   │   │       │   └── TradeService.java
│   │   │       └── Application.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/.../integration/
│           └── AmTradeIntegrationTest.java
└── README.md
```

### Python Project Structure
```
your-python-project/
├── requirements.txt
├── .env
├── app/
│   ├── main.py
│   ├── config.py
│   ├── routes/
│   │   └── trades.py
│   ├── services/
│   │   └── am_trade_service.py
│   └── models/
│       └── trade.py
├── tests/
│   └── test_am_trade_integration.py
└── README.md
```

---

## 🚀 Quick Reference: Available SDK Methods

### Trade Client

| Method | Purpose | Tier |
|--------|---------|------|
| `getTradeById()` | Get single trade | All |
| `getAllTrades()` | Get all trades paginated | All |
| `getTradesByFreeTab()` | **Get FREE tier trades** | FREE+ |
| `getTradesByFreeTabAndSymbol()` | **Get FREE tier trades by symbol** | FREE+ |
| `getTradesByFreeTabAndStatus()` | **Get FREE tier trades by status** | FREE+ |
| `createTrade()` | Create new trade | PREMIUM+ |
| `updateTrade()` | Update trade | PREMIUM+ |
| `deleteTrade()` | Delete trade | PREMIUM+ |
| `filterTrades()` | Advanced filtering | All |
| `batchCreateTrades()` | Batch create | PREMIUM+ |
| `batchDeleteTrades()` | Batch delete | PREMIUM+ |

### Portfolio Client
- `getPortfolioById()` - Get portfolio
- `createPortfolio()` - Create portfolio
- `getAllPortfolios()` - List portfolios

### Journal Client
- `createJournal()` - Create journal entry
- `getJournalByTradeId()` - Get journal for trade

### Filter Client
- `createFilter()` - Create favorite filter
- `getFilterById()` - Get filter
- `deleteFilter()` - Delete filter

---

## 📞 Support & Resources

1. **SDK Documentation:** See `SDK-README.md` in the SDK root
2. **Tier System Details:** See `TIER-BASED-ACCESS-CONTROL.md`
3. **Quick Reference:** See `TIER-QUICK-REFERENCE.md`
4. **Backend Integration:** See `TIER-BACKEND-IMPLEMENTATION.md`

---

## ✅ Checklist for Integration

- [ ] Add SDK dependency (Maven or pip)
- [ ] Create configuration class/module
- [ ] Set environment variables (api-url, api-key)
- [ ] Initialize SDK in application
- [ ] Create service/client class
- [ ] Add REST endpoints for SDK operations
- [ ] Implement error handling
- [ ] Write integration tests
- [ ] Test FREE tab endpoints locally
- [ ] Deploy and verify in target environment

---

**Status:** Ready for Integration  
**Last Updated:** December 5, 2025
