# Trade Journal API - Quick Reference Guide

## Version 2.0.0 - Enhanced Attachment Support

### 📋 Overview
The Trade Journal API has been enhanced with rich attachment support, allowing structured metadata for files and categorized URL fields for better organization.

---

## 🆕 What's New in v2.0.0

### Enhanced Attachment Fields
- **`attachments`** - List of structured attachments with metadata (replaces simple imageUrls)
- **`chartUrls`** - Dedicated field for chart analysis images
- **`documentUrls`** - Field for PDF documents, notes, spreadsheets
- **`videoUrls`** - Field for video analysis or recordings
- **`externalUrls`** - Field for external references (news, articles)

### Backward Compatibility
- **`imageUrls`** - Still supported but marked as DEPRECATED
- Existing integrations will continue to work
- Recommended to migrate to new `attachments` field

---

## 📊 Attachment Object Structure

```json
{
  "fileName": "string (required)",
  "fileUrl": "string (required)",
  "fileType": "string (optional - MIME type)",
  "uploadedAt": "string (optional - ISO 8601 timestamp)",
  "description": "string (optional)"
}
```

### Example Attachment
```json
{
  "fileName": "daily_chart.png",
  "fileUrl": "https://storage.example.com/charts/nifty_20251130.png",
  "fileType": "image/png",
  "uploadedAt": "2025-11-30T10:30:00Z",
  "description": "Daily chart showing key support levels"
}
```

---

## 🔗 API Endpoints

### 1. Create Journal Entry
**POST** `/api/v1/journal`

#### Required Fields
```json
{
  "userId": "string",
  "title": "string",
  "content": "string",
  "entryDate": "ISO 8601 timestamp"
}
```

#### Optional Fields
- `tradeId` - Link to specific trade
- `mood` - Trading psychology (CONFIDENT, ANXIOUS, NEUTRAL, etc.)
- `marketSentiment` - Integer 1-10 (1=bearish, 10=bullish)
- `tags` - Array of strings
- `customFields` - Object for extensibility
- `attachments` - Array of Attachment objects
- `chartUrls` - Array of chart image URLs
- `documentUrls` - Array of document URLs
- `videoUrls` - Array of video URLs
- `externalUrls` - Array of external reference URLs
- `relatedTradeIds` - Array of related trade IDs
- `imageUrls` - DEPRECATED, use attachments

---

### 2. Get Journal Entry by ID
**GET** `/api/v1/journal/{entryId}`

**Path Parameters:**
- `entryId` (required) - Journal entry ID

**Response:** TradeJournalEntryResponse object

---

### 3. Update Journal Entry
**PUT** `/api/v1/journal/{entryId}`

**Path Parameters:**
- `entryId` (required) - Journal entry ID

**Request Body:** Same as Create (TradeJournalEntryRequest)

---

### 4. Delete Journal Entry
**DELETE** `/api/v1/journal/{entryId}`

**Path Parameters:**
- `entryId` (required) - Journal entry ID

**Response:** 204 No Content

---

### 5. Get Entries by User
**GET** `/api/v1/journal/user/{userId}`

**Path Parameters:**
- `userId` (required) - User ID

**Response:** Array of TradeJournalEntryResponse objects

---

### 6. Get Entries by Trade
**GET** `/api/v1/journal/trade/{tradeId}`

**Path Parameters:**
- `tradeId` (required) - Trade ID

**Response:** Array of TradeJournalEntryResponse objects

---

### 7. Get Entries by Date Range
**GET** `/api/v1/journal/date-range`

**Query Parameters:**
- `userId` (required) - User ID
- `startDate` (required) - ISO date (e.g., 2025-11-01)
- `endDate` (required) - ISO date (e.g., 2025-11-30)

**Response:** Array of TradeJournalEntryResponse objects

---

## 🚀 Quick Start Examples

### Minimal Journal Entry
```bash
curl -X POST http://localhost:8073/api/v1/journal \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_12345",
    "title": "Quick Market Note",
    "content": "Observed strong bullish momentum in IT sector.",
    "entryDate": "2025-11-30T10:30:00Z"
  }'
```

### Journal with Attachments (New Way)
```bash
curl -X POST http://localhost:8073/api/v1/journal \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_12345",
    "tradeId": "trade_67890",
    "title": "HDFC Bank Analysis",
    "content": "Detailed technical analysis with chart patterns.",
    "mood": "CONFIDENT",
    "marketSentiment": 7,
    "tags": ["swing-trade", "hdfc-bank"],
    "entryDate": "2025-11-30T14:30:00Z",
    "attachments": [
      {
        "fileName": "hdfc_chart.png",
        "fileUrl": "https://storage.example.com/charts/hdfc.png",
        "fileType": "image/png",
        "uploadedAt": "2025-11-30T14:35:00Z",
        "description": "Weekly chart with support/resistance"
      }
    ],
    "chartUrls": [
      "https://tradingview.com/chart/HDFCBANK"
    ]
  }'
```

### Journal with Categorized URLs
```bash
curl -X POST http://localhost:8073/api/v1/journal \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user_12345",
    "title": "Market Research - Banking Sector",
    "content": "Comprehensive analysis of banking sector trends.",
    "entryDate": "2025-11-30T16:00:00Z",
    "chartUrls": [
      "https://tradingview.com/chart/BANKNIFTY"
    ],
    "documentUrls": [
      "https://storage.example.com/docs/banking_sector_report.pdf"
    ],
    "videoUrls": [
      "https://youtube.com/watch?v=banking_analysis"
    ],
    "externalUrls": [
      "https://moneycontrol.com/news/banking-sector",
      "https://economictimes.com/banking-outlook"
    ]
  }'
```

---

## 📱 Response Example

```json
{
  "id": "entry_12345",
  "userId": "user_12345",
  "tradeId": "trade_67890",
  "title": "HDFC Bank Analysis",
  "content": "Detailed technical analysis with chart patterns.",
  "mood": "CONFIDENT",
  "marketSentiment": 7,
  "tags": ["swing-trade", "hdfc-bank"],
  "entryDate": "2025-11-30T14:30:00Z",
  "attachments": [
    {
      "fileName": "hdfc_chart.png",
      "fileUrl": "https://storage.example.com/charts/hdfc.png",
      "fileType": "image/png",
      "uploadedAt": "2025-11-30T14:35:00Z",
      "description": "Weekly chart with support/resistance"
    }
  ],
  "chartUrls": [
    "https://tradingview.com/chart/HDFCBANK"
  ],
  "createdAt": "2025-11-30T14:30:15Z",
  "updatedAt": "2025-11-30T14:30:15Z"
}
```

---

## 🎯 Best Practices

### 1. Use Structured Attachments
✅ **Recommended (New Way)**
```json
{
  "attachments": [
    {
      "fileName": "chart.png",
      "fileUrl": "https://example.com/chart.png",
      "fileType": "image/png",
      "description": "Daily chart analysis"
    }
  ]
}
```

❌ **Deprecated (Old Way)**
```json
{
  "imageUrls": [
    "https://example.com/chart.png"
  ]
}
```

### 2. Organize URLs by Category
```json
{
  "chartUrls": ["https://tradingview.com/chart"],
  "documentUrls": ["https://example.com/report.pdf"],
  "videoUrls": ["https://youtube.com/watch?v=analysis"],
  "externalUrls": ["https://news.com/article"]
}
```

### 3. Include Descriptive Metadata
Always provide:
- Clear file names
- File types (MIME types)
- Upload timestamps
- Meaningful descriptions

### 4. Use Custom Fields for Extension
```json
{
  "customFields": {
    "strategy_type": "iron_condor",
    "risk_reward_ratio": 1.5,
    "confidence_level": 8
  }
}
```

---

## ⚠️ Migration Guide

### From v1.0.0 to v2.0.0

**Old Code (v1.0.0)**
```json
{
  "imageUrls": [
    "https://example.com/chart1.png",
    "https://example.com/chart2.png"
  ]
}
```

**New Code (v2.0.0)**
```json
{
  "attachments": [
    {
      "fileName": "chart1.png",
      "fileUrl": "https://example.com/chart1.png",
      "fileType": "image/png",
      "uploadedAt": "2025-11-30T10:00:00Z",
      "description": "Primary chart analysis"
    },
    {
      "fileName": "chart2.png",
      "fileUrl": "https://example.com/chart2.png",
      "fileType": "image/png",
      "uploadedAt": "2025-11-30T10:05:00Z",
      "description": "Secondary timeframe view"
    }
  ]
}
```

---

## 🔍 Search & Filter Tips

### Get All Entries for a User
```bash
GET /api/v1/journal/user/{userId}
```

### Get All Entries for a Specific Trade
```bash
GET /api/v1/journal/trade/{tradeId}
```

### Get Entries in Date Range
```bash
GET /api/v1/journal/date-range?userId=user_12345&startDate=2025-11-01&endDate=2025-11-30
```

---

## 📚 Related Files

- **Schema**: `journal-api-schema.json` - Complete OpenAPI 3.0 schema
- **Examples**: `journal-payload-examples.json` - Comprehensive payload examples
- **Postman**: `TradeJournalController.postman_collection.json` - Postman collection

---

## 🆘 Common Issues

### Issue: Old clients sending imageUrls
**Solution:** Field still supported but deprecated. Migrate to attachments when possible.

### Issue: Missing required fields
**Solution:** Ensure userId, title, content, and entryDate are always provided.

### Issue: Invalid date format
**Solution:** Use ISO 8601 format: `2025-11-30T10:30:00Z`

---

## 📞 Support

For issues or questions:
1. Check `journal-payload-examples.json` for working examples
2. Refer to `journal-api-schema.json` for complete field definitions
3. Review error responses for validation messages

---

**Last Updated:** November 30, 2025  
**Version:** 2.0.0  
**API Base URL:** `http://localhost:8073/api/v1/journal`
