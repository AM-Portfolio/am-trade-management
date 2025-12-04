# Trade Controller API - Code Generation Guide

## Overview
This guide provides comprehensive instructions for generating code models from the TradeController API JSON Schema in multiple programming languages: **Java**, **Python**, **Flutter (Dart)**, and **TypeScript/JavaScript**.

## Prerequisites

### Java
- **JDK**: 11 or higher
- **Maven**: 3.6 or higher
- **Plugin**: `jsonschema2pojo-maven-plugin`

### Python
- **Python**: 3.8 or higher
- **Tool**: `datamodel-code-generator`
- **Dependencies**: `pydantic`, `typing-extensions`

### Flutter (Dart)
- **Dart SDK**: 2.12 or higher
- **Tool**: `quicktype`
- **Package Manager**: pub

### TypeScript/JavaScript
- **Node.js**: 14 or higher
- **npm**: 6 or higher
- **Tools**: `quicktype` or `json-schema-to-typescript`

---

## 1. Java Code Generation

### Method 1: Using Maven Plugin (Recommended)

#### Step 1: Add Plugin to `pom.xml`

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jsonschema2pojo</groupId>
            <artifactId>jsonschema2pojo-maven-plugin</artifactId>
            <version>1.2.1</version>
            <configuration>
                <sourceDirectory>${basedir}/postman</sourceDirectory>
                <targetPackage>com.am.trade.models.generated</targetPackage>
                <outputDirectory>${project.build.directory}/generated-sources/json-schema</outputDirectory>
                <sourceType>jsonschema</sourceType>
                <includeAdditionalProperties>false</includeAdditionalProperties>
                <includeConstructors>true</includeConstructors>
                <constructorsRequiredPropertiesOnly>false</constructorsRequiredPropertiesOnly>
                <includeSetters>true</includeSetters>
                <includeGetters>true</includeGetters>
                <includeToString>true</includeToString>
                <includeHashcodeAndEquals>true</includeHashcodeAndEquals>
                <annotationStyle>jackson2</annotationStyle>
                <useLongIntegers>true</useLongIntegers>
                <useBigDecimals>true</useBigDecimals>
                <dateType>java.time.LocalDateTime</dateType>
                <dateTimeType>java.time.LocalDateTime</dateTimeType>
                <includes>
                    <include>trade-controller-api-schema.json</include>
                </includes>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

#### Step 2: Generate Java Classes

```bash
mvn clean generate-sources
```

#### Step 3: Verify Generated Classes

Generated classes will be in:
```
target/generated-sources/json-schema/com/am/trade/models/generated/
├── TradeDetails.java
├── InstrumentInfo.java
├── DerivativeInfo.java
├── EntryExitInfo.java
├── TradeMetrics.java
├── Attachment.java
├── TradePsychologyData.java
├── TradeEntryExistReasoning.java
├── FilterTradeDetailsRequest.java
├── FilterTradeDetailsResponse.java
├── FilterSummary.java
├── MetricsFilterConfig.java
└── ErrorResponse.java
```

### Method 2: Using Command Line Tool

```bash
# Install the CLI tool
npm install -g jsonschema2pojo

# Generate Java classes
jsonschema2pojo --source postman/trade-controller-api-schema.json \
                --target src/main/java \
                --package com.am.trade.models.generated \
                --annotation-style jackson2 \
                --use-big-decimals \
                --use-long-integers
```

---

## 2. Python Code Generation

### Method 1: Using datamodel-code-generator (Recommended)

#### Step 1: Install the Tool

```bash
pip install datamodel-code-generator[http]
```

#### Step 2: Generate Python Models with Pydantic

```bash
datamodel-codegen \
  --input postman/trade-controller-api-schema.json \
  --output models/trade_models.py \
  --input-file-type jsonschema \
  --output-model-type pydantic_v2.BaseModel \
  --use-standard-collections \
  --use-schema-description \
  --use-field-description \
  --field-constraints \
  --snake-case-field \
  --target-python-version 3.10
```

#### Step 3: Verify Generated Models

The generated `trade_models.py` will contain:

```python
from __future__ import annotations
from datetime import date, datetime
from decimal import Decimal
from enum import Enum
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, ConfigDict

class Exchange(str, Enum):
    NSE = "NSE"
    BSE = "BSE"
    MCX = "MCX"
    NCDEX = "NCDEX"

class TradeStatus(str, Enum):
    OPEN = "OPEN"
    CLOSED = "CLOSED"
    WIN = "WIN"
    LOSS = "LOSS"
    BREAKEVEN = "BREAKEVEN"
    CANCELLED = "CANCELLED"

class InstrumentInfo(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    
    symbol: Optional[str] = Field(None, description="Trading symbol")
    isin: Optional[str] = Field(None, description="ISIN")
    raw_symbol: Optional[str] = Field(None, alias="rawSymbol")
    # ... more fields

class TradeDetails(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    
    trade_id: str = Field(..., alias="tradeId")
    portfolio_id: str = Field(..., alias="portfolioId")
    instrument_info: InstrumentInfo = Field(..., alias="instrumentInfo")
    status: TradeStatus
    # ... more fields
```

#### Step 4: Usage Example

```python
from models.trade_models import TradeDetails, InstrumentInfo, TradeStatus
from datetime import datetime
from decimal import Decimal

# Create a trade
trade = TradeDetails(
    trade_id="TRD-2025-001",
    portfolio_id="163d0143-4fcb-480c-ac20-622f14e0e293",
    instrument_info=InstrumentInfo(
        symbol="BANKNIFTY",
        raw_symbol="BANKNIFTY2091722500CE"
    ),
    status=TradeStatus.WIN,
    # ... more fields
)

# Serialize to JSON
trade_json = trade.model_dump_json()

# Deserialize from JSON
trade_from_json = TradeDetails.model_validate_json(trade_json)
```

---

## 3. Flutter (Dart) Code Generation

### Method 1: Using quicktype (Recommended)

#### Step 1: Install quicktype

```bash
npm install -g quicktype
```

#### Step 2: Generate Dart Models

```bash
quicktype \
  --src postman/trade-controller-api-schema.json \
  --lang dart \
  --out lib/models/trade_models.dart \
  --no-combine-classes \
  --just-types \
  --use-hive \
  --part-name trade_models
```

#### Step 3: Add Dependencies to `pubspec.yaml`

```yaml
dependencies:
  flutter:
    sdk: flutter
  json_annotation: ^4.8.1
  
dev_dependencies:
  build_runner: ^2.4.6
  json_serializable: ^6.7.1
```

#### Step 4: Generate Serialization Code

```bash
flutter pub get
flutter pub run build_runner build --delete-conflicting-outputs
```

#### Step 5: Verify Generated Models

The generated `trade_models.dart` will contain:

```dart
import 'package:json_annotation/json_annotation.dart';

part 'trade_models.g.dart';

enum Exchange {
  @JsonValue("NSE")
  nse,
  @JsonValue("BSE")
  bse,
  @JsonValue("MCX")
  mcx,
  @JsonValue("NCDEX")
  ncdex,
}

enum TradeStatus {
  @JsonValue("OPEN")
  open,
  @JsonValue("CLOSED")
  closed,
  @JsonValue("WIN")
  win,
  @JsonValue("LOSS")
  loss,
  @JsonValue("BREAKEVEN")
  breakeven,
  @JsonValue("CANCELLED")
  cancelled,
}

@JsonSerializable()
class InstrumentInfo {
  final String? symbol;
  final String? isin;
  final String? rawSymbol;
  final Exchange? exchange;
  
  InstrumentInfo({
    this.symbol,
    this.isin,
    this.rawSymbol,
    this.exchange,
  });
  
  factory InstrumentInfo.fromJson(Map<String, dynamic> json) => 
      _$InstrumentInfoFromJson(json);
  Map<String, dynamic> toJson() => _$InstrumentInfoToJson(this);
}

@JsonSerializable()
class TradeDetails {
  final String tradeId;
  final String portfolioId;
  final InstrumentInfo instrumentInfo;
  final TradeStatus status;
  
  TradeDetails({
    required this.tradeId,
    required this.portfolioId,
    required this.instrumentInfo,
    required this.status,
  });
  
  factory TradeDetails.fromJson(Map<String, dynamic> json) => 
      _$TradeDetailsFromJson(json);
  Map<String, dynamic> toJson() => _$TradeDetailsToJson(this);
}
```

#### Step 6: Usage Example

```dart
import 'package:your_app/models/trade_models.dart';
import 'dart:convert';

void main() {
  // Create a trade
  final trade = TradeDetails(
    tradeId: "TRD-2025-001",
    portfolioId: "163d0143-4fcb-480c-ac20-622f14e0e293",
    instrumentInfo: InstrumentInfo(
      symbol: "BANKNIFTY",
      rawSymbol: "BANKNIFTY2091722500CE",
    ),
    status: TradeStatus.win,
  );
  
  // Serialize to JSON
  final tradeJson = jsonEncode(trade.toJson());
  
  // Deserialize from JSON
  final tradeFromJson = TradeDetails.fromJson(jsonDecode(tradeJson));
}
```

---

## 4. TypeScript/JavaScript Code Generation

### Method 1: Using quicktype (Recommended)

#### Step 1: Install quicktype

```bash
npm install -g quicktype
```

#### Step 2: Generate TypeScript Interfaces

```bash
quicktype \
  --src postman/trade-controller-api-schema.json \
  --lang typescript \
  --out src/models/TradeModels.ts \
  --just-types \
  --nice-property-names \
  --explicit-unions
```

#### Step 3: Verify Generated TypeScript

The generated `TradeModels.ts` will contain:

```typescript
export enum Exchange {
    NSE = "NSE",
    BSE = "BSE",
    MCX = "MCX",
    NCDEX = "NCDEX",
}

export enum TradeStatus {
    OPEN = "OPEN",
    CLOSED = "CLOSED",
    WIN = "WIN",
    LOSS = "LOSS",
    BREAKEVEN = "BREAKEVEN",
    CANCELLED = "CANCELLED",
}

export interface InstrumentInfo {
    symbol?: string;
    isin?: string;
    rawSymbol?: string;
    exchange?: Exchange;
    segment?: string;
    series?: string;
    indexType?: string;
    derivativeInfo?: DerivativeInfo;
    description?: string;
    currency?: string;
    lotSize?: string;
}

export interface TradeDetails {
    tradeId: string;
    portfolioId: string;
    instrumentInfo: InstrumentInfo;
    symbol?: string;
    strategy?: string;
    status: TradeStatus;
    tradePositionType: "LONG" | "SHORT";
    entryInfo: EntryExitInfo;
    exitInfo?: EntryExitInfo;
    metrics?: TradeMetrics;
    tradeExecutions?: TradeModel[];
    notes?: string;
    tags?: string[];
    userId?: string;
    attachments?: Attachment[];
    psychologyData?: TradePsychologyData;
    entryReasoning?: TradeEntryExistReasoning;
    exitReasoning?: TradeEntryExistReasoning;
}

// Conversion utilities
export class Convert {
    public static toTradeDetails(json: string): TradeDetails {
        return JSON.parse(json);
    }

    public static tradeDetailsToJson(value: TradeDetails): string {
        return JSON.stringify(value);
    }
}
```

#### Step 4: Usage Example

```typescript
import { TradeDetails, TradeStatus, Convert } from './models/TradeModels';

// Create a trade
const trade: TradeDetails = {
    tradeId: "TRD-2025-001",
    portfolioId: "163d0143-4fcb-480c-ac20-622f14e0e293",
    instrumentInfo: {
        symbol: "BANKNIFTY",
        rawSymbol: "BANKNIFTY2091722500CE",
    },
    status: TradeStatus.WIN,
    tradePositionType: "LONG",
    entryInfo: {
        timestamp: "2025-01-15T10:30:00Z",
        price: 125.50,
        quantity: 100,
    },
};

// Serialize to JSON
const tradeJson = Convert.tradeDetailsToJson(trade);

// Deserialize from JSON
const tradeFromJson = Convert.toTradeDetails(tradeJson);
```

### Method 2: Using json-schema-to-typescript

#### Step 1: Install the Tool

```bash
npm install -g json-schema-to-typescript
```

#### Step 2: Generate TypeScript Types

```bash
json2ts \
  --input postman/trade-controller-api-schema.json \
  --output src/models/TradeModels.d.ts \
  --unreachableDefinitions
```

---

## API Endpoint Reference

### Base URL
```
http://localhost:8073/api/v1/trades
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/details/portfolio/{portfolioId}` | Get trades by portfolio and symbols |
| POST | `/details` | Add a new trade |
| PUT | `/details/{tradeId}` | Update an existing trade |
| GET | `/filter` | Filter trades by criteria |
| POST | `/details/batch` | Add/update multiple trades |
| POST | `/details/by-ids` | Get trades by IDs |
| POST | `/details/filter` | Filter trades using favorite filter |

---

## Testing the Generated Models

### Java Test Example

```java
@Test
public void testTradeDetailsCreation() {
    TradeDetails trade = new TradeDetails();
    trade.setTradeId("TRD-2025-001");
    trade.setPortfolioId("163d0143-4fcb-480c-ac20-622f14e0e293");
    trade.setStatus(TradeStatus.WIN);
    
    assertNotNull(trade);
    assertEquals("TRD-2025-001", trade.getTradeId());
}
```

### Python Test Example

```python
def test_trade_details_creation():
    trade = TradeDetails(
        trade_id="TRD-2025-001",
        portfolio_id="163d0143-4fcb-480c-ac20-622f14e0e293",
        instrument_info=InstrumentInfo(symbol="BANKNIFTY"),
        status=TradeStatus.WIN,
        trade_position_type="LONG",
        entry_info=EntryExitInfo(
            timestamp=datetime.now(),
            price=Decimal("125.50"),
            quantity=100
        )
    )
    
    assert trade.trade_id == "TRD-2025-001"
    assert trade.status == TradeStatus.WIN
```

---

## Best Practices

### 1. Version Control
- Keep the JSON schema in version control
- Regenerate models when schema changes
- Document breaking changes

### 2. Validation
- Use schema validation in CI/CD pipelines
- Validate API responses against schema
- Use generated models for type safety

### 3. Code Organization
```
project/
├── postman/
│   └── trade-controller-api-schema.json
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── generated/
│   │   │       └── models/
│   │   └── python/
│   │       └── models/
│   └── test/
├── lib/                          # Flutter
│   └── models/
└── typescript/
    └── models/
```

### 4. Continuous Integration
Add model generation to your CI/CD pipeline:

```yaml
# GitHub Actions example
- name: Generate Models
  run: |
    mvn generate-sources
    datamodel-codegen --input postman/trade-controller-api-schema.json --output models/
    quicktype --src postman/trade-controller-api-schema.json --lang dart --out lib/models/
```

---

## Troubleshooting

### Java Issues
- **Issue**: Classes not generated
- **Solution**: Verify `sourceDirectory` path in `pom.xml`

### Python Issues
- **Issue**: Import errors
- **Solution**: Install `pydantic[email]` for complete validation

### Flutter Issues
- **Issue**: Build runner fails
- **Solution**: Run `flutter pub get` and clear build cache

### TypeScript Issues
- **Issue**: Type errors
- **Solution**: Enable `strictNullChecks` in `tsconfig.json`

---

## Additional Resources

- [JSON Schema Specification](https://json-schema.org/)
- [jsonschema2pojo Documentation](https://www.jsonschema2pojo.org/)
- [datamodel-code-generator](https://github.com/koxudaxi/datamodel-code-generator)
- [quicktype Documentation](https://quicktype.io/)
- [Pydantic Documentation](https://docs.pydantic.dev/)

---

## Next Steps

1. Generate models for your preferred language(s)
2. Integrate generated models into your application
3. Implement API client using generated types
4. Set up automated testing with generated models
5. Configure CI/CD for automatic model regeneration

For questions or issues, refer to the project documentation or contact the development team.
