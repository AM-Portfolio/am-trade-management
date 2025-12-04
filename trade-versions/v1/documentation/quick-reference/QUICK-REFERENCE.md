# AM Trade Management - Container & API Quick Reference

## 🐳 Docker Container Information

### Container Details
- **Container Name**: `am-trade-service`
- **External Port**: `8073`
- **Internal Port**: `8080`
- **Network**: `market-data-network`

### Container Commands
```bash
# Start all services
docker-compose up -d

# Check container status
docker-compose ps

# View container logs
docker-compose logs am-trade-service

# Stop all services
docker-compose down

# Restart specific service
docker-compose restart am-trade-service

# Access container shell
docker exec -it am-trade-service /bin/sh

# View container resource usage
docker stats am-trade-service
```

## 🌐 API Access Points

### Base URLs
- **External Access**: `http://localhost:8073`
- **Container Internal**: `http://am-trade-service:8080`
- **Health Check**: `http://localhost:8073/actuator/health`

### Quick Health Check
```bash
# Check if API is responsive
curl -f http://localhost:8073/actuator/health

# Check application info
curl http://localhost:8073/actuator/info

# Check available endpoints
curl http://localhost:8073/actuator
```

## 📊 API Endpoint Categories

### Core Trading Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/trades/details/portfolio/{portfolioId}` | Get trades by portfolio |
| POST | `/api/v1/trades/details` | Create new trade |
| PUT | `/api/v1/trades/details/{tradeId}` | Update existing trade |
| GET | `/api/v1/trades/filter` | Filter trades by criteria |
| POST | `/api/v1/trades/details/batch` | Batch create/update trades |

### Calendar-Based Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/trades/calendar/day` | Get trades for specific day |
| GET | `/api/v1/trades/calendar/month` | Get trades for specific month |

### Analytics Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/analytics/trade-replays` | Create trade replay |
| GET | `/api/v1/analytics/trade-replays/{id}` | Get replay by ID |
| GET | `/api/v1/analytics/trade-replays/symbol/{symbol}` | Get replays by symbol |

### Metrics & Performance
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/metrics` | Get all trade metrics |
| GET | `/api/v1/metrics/portfolio/{portfolioId}` | Get portfolio metrics |
| GET | `/api/v1/portfolio-summary/{portfolioId}` | Get portfolio summary |

### User Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/preferences/{userId}` | Get user preferences |
| PUT | `/api/v1/preferences/{userId}` | Update user preferences |
| POST | `/api/v1/preferences` | Create user preferences |

## 🔧 Environment Variables

### Database Configuration
```env
MONGODB_URI=mongodb://localhost:27017
MONGODB_DATABASE=am_trade_management
MONGODB_AUTHENTICATION_DATABASE=admin
```

### Redis Configuration
```env
REDIS_HOSTNAME=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### Kafka Configuration
```env
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_CONSUMER_GROUP_ID=am-trade-group
```

### Application Configuration
```env
SPRING_PROFILES_ACTIVE=docker
AM_MARKET_DATA_API_URL=http://market-data-api:8080
AM_TRADE_MAX_RETRIES=3
```

## 📋 Sample API Requests

### 1. Health Check
```bash
curl -X GET http://localhost:8073/actuator/health
```

### 2. Get Portfolio Trades
```bash
curl -X GET "http://localhost:8073/api/v1/trades/details/portfolio/portfolio-123?symbols=AAPL,GOOGL"
```

### 3. Create New Trade
```bash
curl -X POST http://localhost:8073/api/v1/trades/details \
  -H "Content-Type: application/json" \
  -d '{
    "portfolioId": "portfolio-123",
    "symbol": "AAPL",
    "userId": "user-123",
    "quantity": 100,
    "price": 150.25,
    "tradeType": "BUY",
    "status": "EXECUTED",
    "strategy": "MOMENTUM",
    "entryDate": "2024-01-15"
  }'
```

### 4. Filter Trades
```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?portfolioIds=portfolio-123&symbols=AAPL&statuses=EXECUTED&startDate=2024-01-01&endDate=2024-12-31"
```

### 5. Get Trade Metrics
```bash
curl -X GET "http://localhost:8073/api/v1/metrics/portfolio/portfolio-123"
```

## 🚨 Troubleshooting Commands

### Container Issues
```bash
# Check if container is running
docker ps | grep am-trade-service

# View recent logs
docker-compose logs --tail=50 am-trade-service

# Check container health
docker inspect am-trade-service | grep -A5 Health

# Check port binding
docker port am-trade-service
```

### Network Issues
```bash
# Test container network connectivity
docker exec am-trade-service ping mongodb
docker exec am-trade-service ping redis

# Check network configuration
docker network inspect market-data-network
```

### Application Issues
```bash
# Check application startup logs
docker-compose logs am-trade-service | grep -i "started\|error\|exception"

# Monitor real-time logs
docker-compose logs -f am-trade-service

# Check Java process inside container
docker exec am-trade-service ps aux | grep java
```

## 📈 Monitoring Commands

### Performance Monitoring
```bash
# Container resource usage
docker stats am-trade-service --no-stream

# Application metrics
curl http://localhost:8073/actuator/metrics

# JVM metrics
curl http://localhost:8073/actuator/metrics/jvm.memory.used

# HTTP metrics
curl http://localhost:8073/actuator/metrics/http.server.requests
```

### Log Analysis
```bash
# Search for errors
docker-compose logs am-trade-service | grep -i error

# Search for specific trade operations
docker-compose logs am-trade-service | grep -i "trade.*created\|trade.*updated"

# Monitor API calls
docker-compose logs am-trade-service | grep -E "GET|POST|PUT|DELETE"
```

## 🔐 Security Notes

### Development Environment
- Default configuration uses no authentication
- All endpoints are publicly accessible
- Use environment variables for sensitive data

### Production Considerations
- Enable authentication/authorization
- Use HTTPS for all communications
- Secure environment variables
- Implement rate limiting
- Add request/response logging

## 📞 Quick Contact Commands

### Application Status
```bash
# One-liner health check
curl -f http://localhost:8073/actuator/health && echo "✅ API is healthy" || echo "❌ API is down"

# Check all actuator endpoints
curl -s http://localhost:8073/actuator | jq '.["_links"] | keys[]'

# Get application info
curl -s http://localhost:8073/actuator/info | jq '.'
```

This quick reference provides immediate access to the most commonly needed commands and endpoints for the AM Trade Management system.