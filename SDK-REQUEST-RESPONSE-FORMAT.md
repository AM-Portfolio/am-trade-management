# SDK Request/Response Format - Backend Integration

## Request Format (What Backend Receives)

### HTTP Headers
```
POST /api/v1/trades HTTP/1.1
Host: localhost:8073
Content-Type: application/json
User-Agent: am-trade-sdk-python/1.0.0
Authorization: Bearer your-api-key

X-SDK-Name: am-trade-sdk-python
X-SDK-Version: 1.0.0
X-SDK-Type: SDK
X-SDK-Language: python
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
X-Client-Timestamp: 2025-12-05T10:30:00.123456
```

### Request Body

#### Example: Create Trade
```json
{
  "_sdk_meta": {
    "request_id": "550e8400-e29b-41d4-a716-446655440000",
    "sdk_name": "am-trade-sdk-python",
    "sdk_version": "1.0.0",
    "sdk_type": "SDK",
    "language": "python",
    "timestamp": "2025-12-05T10:30:00.123456",
    "client_identifier": null
  },
  "_operation": "CREATE",
  "_resource": "TRADE",
  "_api_version": "v1",
  "data": {
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "trade_type": "BUY",
    "quantity": 10.0,
    "entry_price": 2500.50,
    "entry_date": "2025-12-05T00:00:00",
    "notes": "Intraday trade"
  }
}
```

#### Example: Update Trade
```json
{
  "_sdk_meta": {
    "request_id": "660e8400-e29b-41d4-a716-446655440001",
    "sdk_name": "am-trade-sdk-python",
    "sdk_version": "1.0.0",
    "sdk_type": "SDK",
    "language": "python",
    "timestamp": "2025-12-05T11:45:00.654321",
    "client_identifier": null
  },
  "_operation": "UPDATE",
  "_resource": "TRADE",
  "_api_version": "v1",
  "data": {
    "exit_price": 2520.75,
    "exit_date": "2025-12-05T15:30:00",
    "status": "CLOSED"
  }
}
```

#### Example: Get Trade with Filtering
```json
{
  "_sdk_meta": {
    "request_id": "770e8400-e29b-41d4-a716-446655440002",
    "sdk_name": "am-trade-sdk-python",
    "sdk_version": "1.0.0",
    "sdk_type": "SDK",
    "language": "python",
    "timestamp": "2025-12-05T12:00:00.987654",
    "client_identifier": null
  },
  "_operation": "READ",
  "_resource": "TRADE",
  "_api_version": "v1",
  "data": {
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "status": "OPEN",
    "min_pnl": -5000.0,
    "max_pnl": 50000.0
  }
}
```

#### Example: Batch Create Trades
```json
{
  "_sdk_meta": {
    "request_id": "880e8400-e29b-41d4-a716-446655440003",
    "sdk_name": "am-trade-sdk-python",
    "sdk_version": "1.1.0",
    "sdk_type": "SDK",
    "language": "python",
    "timestamp": "2025-12-05T13:15:00.111111",
    "client_identifier": null
  },
  "_operation": "CREATE_BATCH",
  "_resource": "TRADE",
  "_api_version": "v1",
  "data": [
    {
      "portfolio_id": "port-123",
      "symbol": "RELIANCE",
      "trade_type": "BUY",
      "quantity": 10.0,
      "entry_price": 2500.50,
      "entry_date": "2025-12-05"
    },
    {
      "portfolio_id": "port-123",
      "symbol": "TCS",
      "trade_type": "SELL",
      "quantity": 5.0,
      "entry_price": 4000.00,
      "entry_date": "2025-12-05"
    }
  ]
}
```

---

## Response Format (What Backend Should Return)

### Success Response (HTTP 200)
```json
{
  "metadata": {
    "request_id": "550e8400-e29b-41d4-a716-446655440000",
    "server_timestamp": "2025-12-05T10:30:01.234567",
    "processing_time_ms": 145,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": true,
  "status_code": 200,
  "data": {
    "id": "trade-789",
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "trade_type": "BUY",
    "quantity": 10.0,
    "entry_price": 2500.50,
    "entry_date": "2025-12-05T00:00:00",
    "exit_price": null,
    "exit_date": null,
    "status": "OPEN",
    "notes": "Intraday trade",
    "pnl": null,
    "pnl_percentage": null
  },
  "message": "Trade created successfully"
}
```

### Success Response - Paginated (HTTP 200)
```json
{
  "metadata": {
    "request_id": "770e8400-e29b-41d4-a716-446655440002",
    "server_timestamp": "2025-12-05T12:00:02.654321",
    "processing_time_ms": 234,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": true,
  "status_code": 200,
  "data": {
    "content": [
      {
        "id": "trade-001",
        "portfolio_id": "port-123",
        "symbol": "RELIANCE",
        "trade_type": "BUY",
        "quantity": 10.0,
        "status": "OPEN",
        "pnl": 2000.0,
        "pnl_percentage": 0.8
      },
      {
        "id": "trade-002",
        "portfolio_id": "port-123",
        "symbol": "TCS",
        "trade_type": "SELL",
        "quantity": 5.0,
        "status": "CLOSED",
        "pnl": -500.0,
        "pnl_percentage": -0.25
      }
    ],
    "page_number": 0,
    "page_size": 2,
    "total_elements": 15,
    "total_pages": 8,
    "is_last": false
  },
  "message": "Trades retrieved successfully"
}
```

### Error Response - Validation Error (HTTP 400)
```json
{
  "metadata": {
    "request_id": "660e8400-e29b-41d4-a716-446655440001",
    "server_timestamp": "2025-12-05T11:45:03.111111",
    "processing_time_ms": 89,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": false,
  "status_code": 400,
  "data": null,
  "message": "Validation failed",
  "errors": {
    "quantity": "Quantity must be greater than 0",
    "entry_price": "Entry price is required",
    "portfolio_id": "Portfolio not found"
  }
}
```

### Error Response - SDK Version Incompatible (HTTP 426)
```json
{
  "metadata": {
    "request_id": "990e8400-e29b-41d4-a716-446655440099",
    "server_timestamp": "2025-12-05T14:20:05.999999",
    "processing_time_ms": 45,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": false,
  "status_code": 426,
  "data": null,
  "message": "SDK version not compatible",
  "errors": {
    "sdk_version": "Version 0.9.0 is no longer supported. Please upgrade to 1.0.0 or later",
    "minimum_version": "1.0.0",
    "current_version": "0.9.0"
  }
}
```

### Error Response - Unauthorized SDK (HTTP 403)
```json
{
  "metadata": {
    "request_id": "aaa0e8400-e29b-41d4-a716-446655440aaa",
    "server_timestamp": "2025-12-05T15:30:07.555555",
    "processing_time_ms": 15,
    "sdk_version_used": "unknown",
    "api_version": "v1"
  },
  "success": false,
  "status_code": 403,
  "data": null,
  "message": "Unauthorized SDK",
  "errors": {
    "sdk_name": "Unknown SDK attempting to access API",
    "reason": "Only am-trade-sdk-python and am-trade-sdk-java are allowed"
  }
}
```

### Error Response - Feature Not Available in Version (HTTP 403)
```json
{
  "metadata": {
    "request_id": "bbb0e8400-e29b-41d4-a716-446655440bbb",
    "server_timestamp": "2025-12-05T16:45:09.777777",
    "processing_time_ms": 22,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": false,
  "status_code": 403,
  "data": null,
  "message": "Feature not available in this SDK version",
  "errors": {
    "feature": "Batch operations",
    "available_from_version": "1.1.0",
    "current_version": "1.0.0",
    "upgrade_url": "https://docs.amtrade.com/sdk-versions#1-1-0"
  }
}
```

---

## Backend Java Implementation Example

```java
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class SdkApiController {

    @PostMapping("/trades")
    public ResponseEntity<?> createTrade(@RequestBody SdkTradeRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Extract SDK metadata
            SdkMetadata sdkMeta = request.getSdkMeta();
            String requestId = sdkMeta.getRequestId();
            String sdkVersion = sdkMeta.getSdkVersion();
            String operation = request.getOperation();
            
            log.info("SDK Request received",
                map(
                    "request_id", requestId,
                    "sdk_version", sdkVersion,
                    "operation", operation,
                    "resource", request.getResource()
                )
            );
            
            // Step 2: Validate SDK
            validateSdk(sdkMeta, requestId);
            
            // Step 3: Apply version-specific restrictions
            applyVersionRestrictions(sdkVersion, request.getData(), requestId);
            
            // Step 4: Process request
            Map<String, Object> tradeData = request.getData();
            Trade createdTrade = tradeService.createTrade(
                (String) tradeData.get("portfolio_id"),
                (String) tradeData.get("symbol"),
                (String) tradeData.get("trade_type"),
                ((Number) tradeData.get("quantity")).doubleValue(),
                ((Number) tradeData.get("entry_price")).doubleValue(),
                requestId  // Pass request ID for audit trail
            );
            
            // Step 5: Build response with metadata
            long processingTimeMs = System.currentTimeMillis() - startTime;
            
            SdkResponse response = new SdkResponse();
            response.setMetadata(new SdkResponseMetadata(
                requestId,
                LocalDateTime.now(),
                processingTimeMs,
                sdkVersion,
                "v1"
            ));
            response.setSuccess(true);
            response.setStatusCode(200);
            response.setData(mapToResponse(createdTrade));
            response.setMessage("Trade created successfully");
            
            log.info("SDK Request completed successfully",
                map("request_id", requestId, "processing_time_ms", processingTimeMs)
            );
            
            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            return buildErrorResponse(request.getSdkMeta(), 400, e, startTime);
        } catch (IncompatibleSdkVersionException e) {
            return buildErrorResponse(request.getSdkMeta(), 426, e, startTime);
        } catch (UnauthorizedSdkException e) {
            return buildErrorResponse(null, 403, e, startTime);
        } catch (Exception e) {
            log.error("SDK Request failed", e);
            return buildErrorResponse(request.getSdkMeta(), 500, e, startTime);
        }
    }
    
    private void validateSdk(SdkMetadata sdkMeta, String requestId) {
        String sdkName = sdkMeta.getSdkName();
        String version = sdkMeta.getSdkVersion();
        
        // Check allowed SDKs
        if (!isAllowedSdk(sdkName)) {
            throw new UnauthorizedSdkException(
                "Unknown SDK: " + sdkName,
                requestId
            );
        }
        
        // Check version compatibility
        if (!isVersionSupported(version)) {
            throw new IncompatibleSdkVersionException(
                "SDK version " + version + " not supported",
                version,
                getMinimumVersion(),
                requestId
            );
        }
    }
    
    private void applyVersionRestrictions(String version, Map<String, Object> data, String requestId) {
        if ("1.0.0".equals(version)) {
            // V1.0.0: No batch operations
            if (data instanceof List) {
                throw new FeatureNotAvailableInVersion(
                    "Batch operations not available in SDK version 1.0.0",
                    "Batch operations",
                    "1.1.0",
                    version,
                    requestId
                );
            }
        }
    }
    
    private ResponseEntity<?> buildErrorResponse(
            SdkMetadata sdkMeta,
            int statusCode,
            Exception exception,
            long startTime) {
        
        String requestId = sdkMeta != null ? sdkMeta.getRequestId() : "unknown";
        String sdkVersion = sdkMeta != null ? sdkMeta.getSdkVersion() : "unknown";
        
        SdkResponse response = new SdkResponse();
        response.setMetadata(new SdkResponseMetadata(
            requestId,
            LocalDateTime.now(),
            System.currentTimeMillis() - startTime,
            sdkVersion,
            "v1"
        ));
        response.setSuccess(false);
        response.setStatusCode(statusCode);
        response.setMessage(exception.getMessage());
        response.setErrors(extractErrorDetails(exception));
        
        log.error("SDK Request error",
            map(
                "request_id", requestId,
                "status_code", statusCode,
                "message", exception.getMessage()
            ),
            exception
        );
        
        return ResponseEntity.status(statusCode).body(response);
    }
    
    private Map<String, Object> map(String... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
```

---

## Field Mapping Guide

### SDK Request Fields

| Field | Type | Required | Example | Purpose |
|-------|------|----------|---------|---------|
| `_sdk_meta.request_id` | String (UUID) | ✓ | `550e8400-e29b...` | Unique request tracking |
| `_sdk_meta.sdk_name` | String | ✓ | `am-trade-sdk-python` | SDK identification |
| `_sdk_meta.sdk_version` | String | ✓ | `1.0.0` | SDK version |
| `_sdk_meta.sdk_type` | String | ✓ | `SDK` | Request type |
| `_sdk_meta.language` | String | ✓ | `python` | Implementation language |
| `_sdk_meta.timestamp` | ISO DateTime | ✓ | `2025-12-05T10:30:00` | Client time |
| `_operation` | String | ✓ | `CREATE`, `READ`, `UPDATE`, `DELETE` | Operation type |
| `_resource` | String | ✓ | `TRADE`, `PORTFOLIO`, etc. | Resource type |
| `_api_version` | String | ✓ | `v1` | API version |
| `data` | Object/Array | ✓ | `{...}` | Actual request data |

### SDK Response Fields

| Field | Type | Required | Example | Purpose |
|-------|------|----------|---------|---------|
| `metadata.request_id` | String (UUID) | ✓ | `550e8400-e29b...` | Request tracking |
| `metadata.server_timestamp` | ISO DateTime | ✓ | `2025-12-05T10:30:01` | Server time |
| `metadata.processing_time_ms` | Integer | ✓ | `145` | Request duration |
| `metadata.sdk_version_used` | String | ✓ | `1.0.0` | SDK version |
| `metadata.api_version` | String | ✓ | `v1` | API version |
| `success` | Boolean | ✓ | `true`/`false` | Operation success |
| `status_code` | Integer | ✓ | `200`, `400`, `500` | HTTP status |
| `data` | Object/Array | - | `{...}` | Response data (if success) |
| `message` | String | ✓ | `"Success"` | Status message |
| `errors` | Object | - | `{...}` | Error details (if failed) |

---

## HTTP Headers Reference

### Request Headers (Sent by SDK)

```
X-SDK-Name: am-trade-sdk-python          # SDK identifier
X-SDK-Version: 1.0.0                     # SDK version
X-SDK-Type: SDK                          # Request type
X-SDK-Language: python                   # Language
X-Request-ID: [UUID]                     # Unique request ID
X-Client-Timestamp: 2025-12-05T10:30:00  # Client timestamp
```

### Response Headers (Sent by Backend)

```
X-Request-ID: [UUID]                     # Echo request ID
X-Processing-Time: 145                   # Processing time (ms)
Content-Type: application/json           # Response format
X-API-Version: v1                        # API version
```

---

## Validation Rules

### For Backend

1. **SDK Identification Required**
   - Check `X-SDK-Name` header
   - Allowed values: `am-trade-sdk-python`, `am-trade-sdk-java`
   - If missing: Return 403 Unauthorized

2. **Version Compatibility Check**
   - Read `_sdk_meta.sdk_version`
   - Compare against supported versions
   - If unsupported: Return 426 Upgrade Required

3. **Request ID Format**
   - Must be valid UUID format
   - Used for request tracking
   - Include in response

4. **Operation Type Validation**
   - Valid values: `CREATE`, `READ`, `UPDATE`, `DELETE`, `CREATE_BATCH`, etc.
   - Validate against resource type
   - Apply version-specific restrictions

5. **Resource Type Validation**
   - Valid values: `TRADE`, `PORTFOLIO`, `JOURNAL`, `FILTER`, `ANALYTICS`
   - Ensure operation is allowed for resource

6. **Payload Data Validation**
   - Extract from `data` field
   - Validate field types and ranges
   - Apply version-specific field requirements

---

## Summary

The SDK request/response format enables:

✅ **Request Identification** - Every request has unique ID  
✅ **Version Tracking** - SDK version included automatically  
✅ **Operation Tracking** - Operation and resource types logged  
✅ **Performance Monitoring** - Processing time tracked  
✅ **Audit Trail** - Complete request/response history  
✅ **Version-Based Access Control** - Apply per-version restrictions  
✅ **Error Handling** - Structured error responses with context  
✅ **Backward Compatibility** - Support multiple SDK versions  

Backend teams can now easily identify SDK requests and apply appropriate restrictions!
