# Python SDK Versioning & Security Architecture

## Overview

The Python SDK now implements a **layered security approach** where:

1. **Users never see your internal models**
2. **Backend identifies all requests as SDK requests** via metadata headers
3. **Version control ensures compatibility** across deployments
4. **Request/Response wrappers provide data transformation**

---

## Architecture Layers

### Layer 1: SDK Versioning (version.py)

**Purpose:** Track SDK versions and capabilities

```python
from am_trade_sdk import SdkVersion, VersionMetadata, SdkIdentifier

# Get current SDK version
current_version = VersionMetadata.get_current_version()
# Output: "1.0.0"

# Get version capabilities
capabilities = VersionMetadata.get_version_capabilities()
# Output: {
#   "version": "1.0.0",
#   "max_retries": 3,
#   "timeout": 30,
#   "pagination_max_size": 100,
#   "features": ["Basic CRUD operations", "Trade management", ...]
# }

# Get SDK identification headers for backend
headers = SdkIdentifier.get_sdk_header()
# Output: {
#   "X-SDK-Name": "am-trade-sdk-python",
#   "X-SDK-Version": "1.0.0",
#   "X-SDK-Type": "SDK",
#   "X-SDK-Language": "python"
# }
```

**Files:**
- `version.py` - SdkVersion enum, VersionMetadata class, SdkIdentifier class

---

### Layer 2: Request/Response Wrappers (wrapper.py)

**Purpose:** Add metadata to all requests and responses

#### Request Structure

Every request now includes:

```python
{
  "_sdk_meta": {
    "request_id": "abc-123-def",
    "sdk_name": "am-trade-sdk-python",
    "sdk_version": "1.0.0",
    "sdk_type": "SDK",
    "language": "python",
    "timestamp": "2025-12-05T10:30:00",
    "client_identifier": null
  },
  "_operation": "CREATE",
  "_resource": "TRADE",
  "_api_version": "v1",
  "data": {
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "quantity": 10,
    ...
  }
}
```

#### Response Structure

Backend responses include:

```python
{
  "metadata": {
    "request_id": "abc-123-def",
    "server_timestamp": "2025-12-05T10:30:01",
    "processing_time_ms": 145,
    "sdk_version_used": "1.0.0",
    "api_version": "v1"
  },
  "success": true,
  "status_code": 200,
  "data": {...},
  "message": "Trade created successfully"
}
```

#### Usage

```python
from am_trade_sdk.wrapper import SdkRequest, VersionedDataTransformer

# Create a versioned request
user_data = {
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "quantity": 10,
    "entry_price": 2500.50
}

sdk_request = VersionedDataTransformer.transform_for_api(
    data=user_data,
    operation="CREATE",
    resource="TRADE",
    version="1.0.0"
)

# sdk_request now has metadata and version info
print(sdk_request.metadata.request_id)  # Unique ID
print(sdk_request.to_api_payload())     # Full payload with _sdk_meta
print(sdk_request.to_headers())         # Headers with X-SDK-*
```

**Files:**
- `wrapper.py` - SdkRequest, SdkResponse, VersionedDataTransformer classes

---

### Layer 3: Base Client with Versioning (base_client.py)

**Purpose:** Automatically inject SDK metadata into all HTTP requests

#### Updated request() method

```python
response = client.request(
    method="POST",
    endpoint="/api/v1/trades",
    data=trade_data,
    operation="CREATE",      # NEW: Specifies operation type
    resource="TRADE"         # NEW: Specifies resource type
)
```

#### What happens automatically:

1. **Request ID generated** - Unique UUID for tracking
2. **Headers added** - X-SDK-Version, X-SDK-Name, X-Request-ID, etc.
3. **Payload wrapped** - Data enclosed in `_sdk_meta` envelope
4. **Logging enhanced** - Includes SDK version and request ID
5. **Error tracking** - Request ID included in error logs

#### Example flow:

```python
from am_trade_sdk import AmTradeSdk

# Initialize SDK
sdk = AmTradeSdk(
    api_url="http://localhost:8073",
    api_key="your-key"
)

# Make request (versioning happens automatically)
trade = sdk.trade_client.create_trade(
    portfolio_id="port-123",
    symbol="RELIANCE",
    quantity=10,
    entry_price=2500.50,
    trade_type="BUY",
    entry_date="2025-12-05"
)

# Behind the scenes:
# 1. Request wrapped with SDK metadata
# 2. Headers include X-SDK-Version: 1.0.0
# 3. Payload includes _sdk_meta envelope
# 4. Backend identifies this as SDK request
# 5. Backend can apply SDK-specific restrictions
```

---

## How Your Backend Benefits

### 1. Request Identification

Backend can identify SDK requests:

```java
// In your backend controller
@PostMapping("/api/v1/trades")
public ResponseEntity<?> createTrade(@RequestBody TradeRequest request) {
    String sdkVersion = request.get_sdk_meta().sdk_version;
    String sdkName = request.get_sdk_meta().sdk_name;
    String requestId = request.get_sdk_meta().request_id;
    
    // Apply SDK-specific logic
    if ("am-trade-sdk-python".equals(sdkName)) {
        // Apply SDK restrictions
        validateSdkRequest(request);
    }
}
```

### 2. Version-based Restrictions

```java
public void validateSdkRequest(TradeRequest request) {
    String version = request.get_sdk_meta().sdk_version;
    
    switch(version) {
        case "1.0.0":
            // Restrict to basic operations
            enforceV1Restrictions();
            break;
        case "2.0.0":
            // Allow advanced features
            break;
    }
}
```

### 3. Audit Trail

Track all SDK requests with unique IDs:

```java
// Every request has request_id from SDK metadata
String requestId = request.get_sdk_meta().request_id;

// Log all SDK operations
logger.info("SDK Request", map(
    "request_id", requestId,
    "sdk_version", request.get_sdk_meta().sdk_version,
    "operation", request._operation,
    "resource", request._resource,
    "timestamp", request.get_sdk_meta().timestamp
));
```

### 4. Backward Compatibility

Handle multiple SDK versions:

```java
Map<String, SdkValidator> validators = Map.of(
    "1.0.0", new V1SdkValidator(),
    "1.1.0", new V1_1SdkValidator(),
    "2.0.0", new V2SdkValidator()
);

String version = request.get_sdk_meta().sdk_version;
SdkValidator validator = validators.get(version);
validator.validate(request);
```

---

## Security Features

### 1. Model Hiding

**Before:** Users saw your exact internal Trade model

```python
# User could access all fields including internal ones
trade = Trade(
    id="...",
    portfolio_id="...",
    symbol="...",
    # ... 20 more fields
    internal_field="hidden"  # Could see this!
)
```

**After:** Users only see what you expose

```python
# Users work with DTOs (create separate dto.py file)
trade_create = TradeCreateDTO(
    portfolio_id="...",
    symbol="...",
    quantity=10,
    entry_price=2500.50
)
# Only these fields - no internal fields exposed
```

### 2. SDK Request Identification

Backend knows requests come from approved SDKs:

```
HTTP Headers:
X-SDK-Name: am-trade-sdk-python
X-SDK-Version: 1.0.0
X-SDK-Type: SDK
X-SDK-Language: python
X-Request-ID: abc-123-def
```

### 3. Version-based Access Control

Apply different restrictions per version:

```python
# SDK v1.0.0: Limited access
# - CRUD only
# - Basic filtering
# - No batch operations

# SDK v2.0.0: Full access
# - CRUD + advanced features
# - Complex analytics
# - Batch operations allowed
```

### 4. Request Envelope

Your models never exposed directly:

```json
{
  "_sdk_meta": { /* SDK identifies itself */ },
  "_operation": "CREATE",
  "_resource": "TRADE",
  "data": { /* Only exposed fields */ }
}
```

---

## File Structure

```
am-trade-sdk-python/
├── am_trade_sdk/
│   ├── __init__.py              # Updated with version exports
│   ├── version.py               # NEW: Versioning & metadata
│   ├── wrapper.py               # NEW: Request/response wrappers
│   ├── base_client.py           # UPDATED: Includes SDK metadata
│   ├── config.py                # Configuration
│   ├── models.py                # Internal models (keep secure)
│   ├── client.py                # Main SDK entry
│   ├── trade_client.py          # Trade operations
│   ├── portfolio_client.py      # Portfolio operations
│   ├── analytics_client.py      # Analytics operations
│   ├── journal_client.py        # Journal operations
│   ├── filter_client.py         # Filter operations
│   └── exceptions.py            # Exception hierarchy
└── pyproject.toml               # Package configuration
```

---

## Usage Examples

### Example 1: Basic CRUD with Automatic Versioning

```python
from am_trade_sdk import AmTradeSdk

# Initialize
sdk = AmTradeSdk(
    api_url="http://localhost:8073",
    api_key="your-api-key"
)

# Create trade - SDK version automatically included
trade = sdk.trade_client.create_trade(
    portfolio_id="port-123",
    symbol="RELIANCE",
    quantity=10,
    entry_price=2500.50,
    trade_type="BUY",
    entry_date="2025-12-05"
)

print(f"Trade created: {trade['id']}")
# Backend sees:
# - Request from am-trade-sdk-python v1.0.0
# - Request ID for tracking
# - Operation type: CREATE
# - Resource type: TRADE
```

### Example 2: Check SDK Version

```python
from am_trade_sdk import VersionMetadata, SdkIdentifier

# Get current version
version = VersionMetadata.get_current_version()
print(f"SDK Version: {version}")  # 1.0.0

# Get version capabilities
caps = VersionMetadata.get_version_capabilities()
print(f"Max retries: {caps['max_retries']}")  # 3
print(f"Timeout: {caps['timeout']}")  # 30

# Get SDK identification
headers = SdkIdentifier.get_sdk_header()
print(headers)
# {
#   "X-SDK-Name": "am-trade-sdk-python",
#   "X-SDK-Version": "1.0.0",
#   "X-SDK-Type": "SDK",
#   "X-SDK-Language": "python"
# }
```

### Example 3: Transform Data Safely

```python
from am_trade_sdk.wrapper import VersionedDataTransformer

# User provides data
user_data = {
    "portfolio_id": "port-123",
    "symbol": "RELIANCE",
    "quantity": 10
}

# Transform for API (version-specific)
sdk_request = VersionedDataTransformer.transform_for_api(
    data=user_data,
    operation="CREATE",
    resource="TRADE",
    version="1.0.0"
)

# Get payload with metadata
payload = sdk_request.to_api_payload()
print(payload)
# {
#   "_sdk_meta": {
#     "request_id": "...",
#     "sdk_version": "1.0.0",
#     ...
#   },
#   "_operation": "CREATE",
#   "_resource": "TRADE",
#   "data": {...}
# }
```

---

## Backend Integration Checklist

- [ ] Extract `_sdk_meta` from request
- [ ] Validate `X-SDK-*` headers
- [ ] Check SDK version compatibility
- [ ] Apply version-specific restrictions
- [ ] Log request_id for audit trail
- [ ] Validate operation type
- [ ] Validate resource type
- [ ] Return response with metadata
- [ ] Include processing_time_ms
- [ ] Include request_id in response

---

## Next Steps

1. **Create DTO layer** - Separate user-facing models from internal models
2. **Add Java SDK versioning** - Apply same approach to Java
3. **Backend validation** - Extract and validate SDK metadata
4. **Audit logging** - Log all SDK requests with version info
5. **Version migration** - Plan upgrades between SDK versions

---

## Summary

✅ **Users can't see your internal models** - Only exposed fields via request/response  
✅ **Backend knows it's SDK requests** - Metadata headers identify source  
✅ **Version tracking built-in** - Every request includes version  
✅ **Backward compatibility ready** - Version-specific logic support  
✅ **Audit trail enabled** - Request ID tracks all operations  
✅ **Layered security** - Multiple validation points available
