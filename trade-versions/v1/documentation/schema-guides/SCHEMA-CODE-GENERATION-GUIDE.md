# FavoriteFilterController API Schema - Multi-Language Code Generation Guide

## Overview

This document provides comprehensive guidance on using the JSON Schema (`favorite-filter-api-schema.json`) to generate data models for the FavoriteFilterController API across multiple programming languages and frameworks.

**Supported Languages:**
- Java (Spring Boot)
- Python (Pydantic, Dataclasses)
- Flutter/Dart
- JavaScript/TypeScript

## Schema File Location
```
/postman/favorite-filter-api-schema.json
```

## API Endpoints Summary

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/filters` | POST | Create a new favorite filter |
| `/api/v1/filters` | GET | Get all favorite filters for a user |
| `/api/v1/filters/{filterId}` | GET | Get a specific filter by ID |
| `/api/v1/filters/{filterId}` | PUT | Update an existing filter |
| `/api/v1/filters/{filterId}` | DELETE | Delete a filter |
| `/api/v1/filters/bulk` | DELETE | Delete multiple filters |
| `/api/v1/filters/default` | GET | Get user's default filter |
| `/api/v1/filters/{filterId}/default` | PUT | Set filter as default |
| `/api/v1/filters/save-current` | POST | Save current filter as favorite |
| `/api/v1/filters/apply/{filterId}` | GET | Get metrics using saved filter |

---

## 1. Java Code Generation

### Option 1: Using jsonschema2pojo

#### Installation (Maven)
```xml
<plugin>
    <groupId>org.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>1.2.1</version>
    <configuration>
        <sourceDirectory>${basedir}/postman</sourceDirectory>
        <targetPackage>am.trade.api.generated</targetPackage>
        <sourceType>jsonschema</sourceType>
        <annotationStyle>jackson2</annotationStyle>
        <includeJsr303Annotations>true</includeJsr303Annotations>
        <includeLombokAnnotations>true</includeLombokAnnotations>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### Generate Command
```bash
mvn jsonschema2pojo:generate
```

#### Generated Java Classes
```java
// FavoriteFilterRequest.java
package am.trade.api.generated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FavoriteFilterRequest {
    
    @JsonProperty("name")
    @NotNull
    private String name;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("isDefault")
    private Boolean isDefault = false;
    
    @JsonProperty("filterConfig")
    @NotNull
    private MetricsFilterConfig filterConfig;
}
```

### Option 2: Using OpenAPI Generator

#### Installation (Maven)
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>${project.basedir}/postman/favorite-filter-api-schema.json</inputSpec>
                <generatorName>java</generatorName>
                <configOptions>
                    <sourceFolder>src/gen/java/main</sourceFolder>
                    <dateLibrary>java8</dateLibrary>
                    <useBeanValidation>true</useBeanValidation>
                    <performBeanValidation>true</performBeanValidation>
                    <useJakartaEe>true</useJakartaEe>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Generate Command
```bash
mvn openapi-generator:generate
```

---

## 2. Python Code Generation

### Option 1: Using datamodel-code-generator (Pydantic)

#### Installation
```bash
pip install datamodel-code-generator
```

#### Generate Pydantic Models
```bash
datamodel-codegen \
  --input postman/favorite-filter-api-schema.json \
  --input-file-type jsonschema \
  --output generated/models.py \
  --output-model-type pydantic.BaseModel \
  --use-standard-collections \
  --use-schema-description \
  --field-constraints
```

#### Generated Python Code (Pydantic)
```python
# models.py
from datetime import date, datetime
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field, validator
from enum import Enum

class TimePeriodFilter(str, Enum):
    TODAY = "TODAY"
    YESTERDAY = "YESTERDAY"
    LAST_7_DAYS = "LAST_7_DAYS"
    LAST_30_DAYS = "LAST_30_DAYS"
    THIS_MONTH = "THIS_MONTH"
    LAST_MONTH = "LAST_MONTH"
    THIS_YEAR = "THIS_YEAR"
    LAST_YEAR = "LAST_YEAR"

class DateRangeFilter(BaseModel):
    """Filter criteria for date ranges"""
    start_date: date = Field(..., description="Start date for the metrics calculation period")
    end_date: date = Field(..., description="End date for the metrics calculation period")
    
    class Config:
        json_schema_extra = {
            "example": {
                "start_date": "2025-01-01",
                "end_date": "2025-12-31"
            }
        }

class TradeCharacteristicsFilter(BaseModel):
    """Filter criteria for trade characteristics"""
    strategies: Optional[List[str]] = Field(None, description="Filter trades by specific strategies")
    tags: Optional[List[str]] = Field(None, description="Filter trades by specific tags")
    directions: Optional[List[str]] = Field(None, description="Filter trades by trade direction")
    statuses: Optional[List[str]] = Field(None, description="Filter trades by specific trade statuses")
    min_holding_time_hours: Optional[int] = Field(None, ge=0, description="Minimum holding time in hours")
    max_holding_time_hours: Optional[int] = Field(None, ge=0, description="Maximum holding time in hours")

class MetricsFilterConfig(BaseModel):
    """Configuration for metrics filters that can be saved as favorites"""
    portfolio_ids: Optional[List[str]] = None
    date_range: Optional[Dict[str, Any]] = None
    time_period: Optional[Dict[str, Any]] = None
    metric_types: Optional[List[str]] = None
    group_by: Optional[List[str]] = None
    instruments: Optional[List[str]] = None
    instrument_filters: Optional[Dict[str, Any]] = None
    trade_characteristics: Optional[Dict[str, Any]] = None
    profit_loss_filters: Optional[Dict[str, Any]] = None
    
    class Config:
        json_schema_extra = {
            "example": {
                "portfolio_ids": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
                "instruments": ["NIFTY", "BANKNIFTY"],
                "trade_characteristics": {
                    "statuses": ["LOSS"],
                    "strategies": ["Momentum Trading"]
                }
            }
        }

class FavoriteFilterRequest(BaseModel):
    """Request DTO for creating or updating a favorite filter"""
    name: str = Field(..., min_length=1, max_length=100, description="Name of the favorite filter")
    description: Optional[str] = Field(None, max_length=500, description="Optional description")
    is_default: bool = Field(False, description="Whether this filter should be set as default")
    filter_config: MetricsFilterConfig = Field(..., description="Filter configuration")
    
    class Config:
        json_schema_extra = {
            "example": {
                "name": "High Risk Intraday Trades",
                "description": "Filter for high-risk intraday trades",
                "is_default": False,
                "filter_config": {
                    "portfolio_ids": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
                }
            }
        }

class FavoriteFilterResponse(BaseModel):
    """Response DTO for favorite filter operations"""
    id: Optional[str] = None
    name: Optional[str] = None
    description: Optional[str] = None
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None
    is_default: Optional[bool] = None
    filter_config: Optional[MetricsFilterConfig] = None

class BulkDeleteRequest(BaseModel):
    """Request DTO for bulk deletion of favorite filters"""
    user_id: str = Field(..., min_length=1, description="User ID")
    filter_ids: List[str] = Field(..., min_items=1, description="List of filter IDs to delete")

class BulkDeleteResponse(BaseModel):
    """Response DTO for bulk deletion of favorite filters"""
    deleted_count: Optional[int] = Field(None, ge=0)
    total_requested: Optional[int] = Field(None, ge=0)
    message: Optional[str] = None
```

### Option 2: Using Python Dataclasses

```bash
datamodel-codegen \
  --input postman/favorite-filter-api-schema.json \
  --output generated/dataclass_models.py \
  --output-model-type dataclasses.dataclass
```

---

## 3. Flutter/Dart Code Generation

### Using quicktype

#### Installation
```bash
npm install -g quicktype
```

#### Generate Dart Models
```bash
quicktype \
  --src postman/favorite-filter-api-schema.json \
  --src-lang schema \
  --lang dart \
  --out lib/models/favorite_filter_models.dart \
  --null-safety \
  --final-props \
  --part-name favorite_filter_models
```

#### Generated Dart Code
```dart
// favorite_filter_models.dart
import 'dart:convert';

class FavoriteFilterRequest {
  final String name;
  final String? description;
  final bool isDefault;
  final MetricsFilterConfig filterConfig;

  FavoriteFilterRequest({
    required this.name,
    this.description,
    this.isDefault = false,
    required this.filterConfig,
  });

  factory FavoriteFilterRequest.fromJson(Map<String, dynamic> json) => FavoriteFilterRequest(
    name: json["name"],
    description: json["description"],
    isDefault: json["isDefault"] ?? false,
    filterConfig: MetricsFilterConfig.fromJson(json["filterConfig"]),
  );

  Map<String, dynamic> toJson() => {
    "name": name,
    "description": description,
    "isDefault": isDefault,
    "filterConfig": filterConfig.toJson(),
  };
}

class MetricsFilterConfig {
  final List<String>? portfolioIds;
  final Map<String, dynamic>? dateRange;
  final Map<String, dynamic>? timePeriod;
  final List<String>? metricTypes;
  final List<String>? groupBy;
  final List<String>? instruments;
  final Map<String, dynamic>? instrumentFilters;
  final Map<String, dynamic>? tradeCharacteristics;
  final Map<String, dynamic>? profitLossFilters;

  MetricsFilterConfig({
    this.portfolioIds,
    this.dateRange,
    this.timePeriod,
    this.metricTypes,
    this.groupBy,
    this.instruments,
    this.instrumentFilters,
    this.tradeCharacteristics,
    this.profitLossFilters,
  });

  factory MetricsFilterConfig.fromJson(Map<String, dynamic> json) => MetricsFilterConfig(
    portfolioIds: json["portfolioIds"] == null 
      ? null 
      : List<String>.from(json["portfolioIds"].map((x) => x)),
    dateRange: json["dateRange"],
    timePeriod: json["timePeriod"],
    metricTypes: json["metricTypes"] == null 
      ? null 
      : List<String>.from(json["metricTypes"].map((x) => x)),
    groupBy: json["groupBy"] == null 
      ? null 
      : List<String>.from(json["groupBy"].map((x) => x)),
    instruments: json["instruments"] == null 
      ? null 
      : List<String>.from(json["instruments"].map((x) => x)),
    instrumentFilters: json["instrumentFilters"],
    tradeCharacteristics: json["tradeCharacteristics"],
    profitLossFilters: json["profitLossFilters"],
  );

  Map<String, dynamic> toJson() => {
    "portfolioIds": portfolioIds == null 
      ? null 
      : List<dynamic>.from(portfolioIds!.map((x) => x)),
    "dateRange": dateRange,
    "timePeriod": timePeriod,
    "metricTypes": metricTypes == null 
      ? null 
      : List<dynamic>.from(metricTypes!.map((x) => x)),
    "groupBy": groupBy == null 
      ? null 
      : List<dynamic>.from(groupBy!.map((x) => x)),
    "instruments": instruments == null 
      ? null 
      : List<dynamic>.from(instruments!.map((x) => x)),
    "instrumentFilters": instrumentFilters,
    "tradeCharacteristics": tradeCharacteristics,
    "profitLossFilters": profitLossFilters,
  };
}

class FavoriteFilterResponse {
  final String? id;
  final String? name;
  final String? description;
  final DateTime? createdAt;
  final DateTime? updatedAt;
  final bool? isDefault;
  final MetricsFilterConfig? filterConfig;

  FavoriteFilterResponse({
    this.id,
    this.name,
    this.description,
    this.createdAt,
    this.updatedAt,
    this.isDefault,
    this.filterConfig,
  });

  factory FavoriteFilterResponse.fromJson(Map<String, dynamic> json) => FavoriteFilterResponse(
    id: json["id"],
    name: json["name"],
    description: json["description"],
    createdAt: json["createdAt"] == null ? null : DateTime.parse(json["createdAt"]),
    updatedAt: json["updatedAt"] == null ? null : DateTime.parse(json["updatedAt"]),
    isDefault: json["isDefault"],
    filterConfig: json["filterConfig"] == null 
      ? null 
      : MetricsFilterConfig.fromJson(json["filterConfig"]),
  );

  Map<String, dynamic> toJson() => {
    "id": id,
    "name": name,
    "description": description,
    "createdAt": createdAt?.toIso8601String(),
    "updatedAt": updatedAt?.toIso8601String(),
    "isDefault": isDefault,
    "filterConfig": filterConfig?.toJson(),
  };
}
```

---

## 4. JavaScript/TypeScript Code Generation

### Using quicktype for TypeScript

#### Generate TypeScript Interfaces
```bash
quicktype \
  --src postman/favorite-filter-api-schema.json \
  --src-lang schema \
  --lang typescript \
  --out src/models/FavoriteFilterModels.ts \
  --just-types \
  --nice-property-names \
  --explicit-unions
```

#### Generated TypeScript Code
```typescript
// FavoriteFilterModels.ts

export interface DateRangeFilter {
  /**
   * Start date for the metrics calculation period (ISO 8601 format: YYYY-MM-DD)
   */
  startDate: string;
  /**
   * End date for the metrics calculation period (ISO 8601 format: YYYY-MM-DD)
   */
  endDate: string;
}

export enum TimePeriodFilter {
  TODAY = "TODAY",
  YESTERDAY = "YESTERDAY",
  LAST_7_DAYS = "LAST_7_DAYS",
  LAST_14_DAYS = "LAST_14_DAYS",
  LAST_30_DAYS = "LAST_30_DAYS",
  THIS_WEEK = "THIS_WEEK",
  LAST_WEEK = "LAST_WEEK",
  THIS_MONTH = "THIS_MONTH",
  LAST_MONTH = "LAST_MONTH",
  THIS_QUARTER = "THIS_QUARTER",
  LAST_QUARTER = "LAST_QUARTER",
  THIS_YEAR = "THIS_YEAR",
  LAST_YEAR = "LAST_YEAR",
  LAST_3_MONTHS = "LAST_3_MONTHS",
  LAST_6_MONTHS = "LAST_6_MONTHS",
  LAST_12_MONTHS = "LAST_12_MONTHS",
  LAST_2_YEARS = "LAST_2_YEARS",
  LAST_3_YEARS = "LAST_3_YEARS",
  LAST_5_YEARS = "LAST_5_YEARS",
  CUSTOM = "CUSTOM",
}

export interface TradeCharacteristicsFilter {
  strategies?: string[];
  tags?: string[];
  directions?: Array<"LONG" | "SHORT">;
  statuses?: Array<"OPEN" | "CLOSED" | "WIN" | "LOSS" | "BREAKEVEN">;
  minHoldingTimeHours?: number;
  maxHoldingTimeHours?: number;
}

export interface ProfitLossFilter {
  minProfitLoss?: number;
  maxProfitLoss?: number;
  minPositionSize?: number;
  maxPositionSize?: number;
}

export interface InstrumentFilterCriteria {
  marketSegments?: Array<
    "EQUITY" | "INDEX" | "EQUITY_FUTURES" | "INDEX_FUTURES" | 
    "EQUITY_OPTIONS" | "INDEX_OPTIONS"
  >;
  baseSymbols?: string[];
  indexTypes?: Array<"NIFTY" | "BANKNIFTY" | "FINNIFTY" | "MIDCPNIFTY">;
  derivativeTypes?: Array<"FUTURES" | "OPTIONS">;
}

export interface MetricsFilterConfig {
  portfolioIds?: string[];
  dateRange?: Record<string, any>;
  timePeriod?: Record<string, any>;
  metricTypes?: Array<"PERFORMANCE" | "RISK" | "DISTRIBUTION" | "TIMING" | "PATTERN">;
  groupBy?: Array<"STRATEGY" | "SYMBOL" | "DAY_OF_WEEK" | "MONTH" | "PORTFOLIO">;
  instruments?: string[];
  instrumentFilters?: Record<string, any>;
  tradeCharacteristics?: Record<string, any>;
  profitLossFilters?: Record<string, any>;
}

export interface FavoriteFilterRequest {
  /**
   * Name of the favorite filter
   */
  name: string;
  /**
   * Optional description of the filter
   */
  description?: string;
  /**
   * Whether this filter should be set as default
   */
  isDefault?: boolean;
  /**
   * Filter configuration
   */
  filterConfig: MetricsFilterConfig;
}

export interface FavoriteFilterResponse {
  id?: string;
  name?: string;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
  isDefault?: boolean;
  filterConfig?: MetricsFilterConfig;
}

export interface BulkDeleteRequest {
  userId: string;
  filterIds: string[];
}

export interface BulkDeleteResponse {
  deletedCount?: number;
  totalRequested?: number;
  message?: string;
}

export interface ErrorResponse {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
  details?: string[];
}
```

### Using json-schema-to-typescript

#### Installation
```bash
npm install -g json-schema-to-typescript
```

#### Generate TypeScript
```bash
json2ts -i postman/favorite-filter-api-schema.json -o src/models/
```

---

## 5. Sample Payloads (JSON Examples)

### Create Filter Request
```json
{
  "name": "High Risk Intraday Trades",
  "description": "Filter for high-risk intraday trades with momentum strategy",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "instruments": ["NIFTY", "BANKNIFTY"],
    "tradeCharacteristics": {
      "statuses": ["LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday", "high-risk"],
      "maxHoldingTimeHours": 24
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.00
    },
    "dateRange": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    }
  }
}
```

### Bulk Delete Request
```json
{
  "userId": "ssd2658",
  "filterIds": [
    "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "a12bc34d-56ef-7890-b123-4567890abcde",
    "b23cd45e-67fg-8901-c234-5678901bcdef"
  ]
}
```

### Filter Response
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "name": "High Risk Intraday Trades",
  "description": "Filter for high-risk intraday trades with momentum strategy",
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T14:45:00Z",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "instruments": ["NIFTY", "BANKNIFTY"],
    "tradeCharacteristics": {
      "statuses": ["LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday", "high-risk"],
      "maxHoldingTimeHours": 24
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.00
    },
    "dateRange": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    }
  }
}
```

---

## 6. Important Notes

### MetricsFilterConfig Structure
The `MetricsFilterConfig` uses a Map-based structure (`Map<String, Object>` in Java) for flexibility. When creating filter configurations:

**IMPORTANT:** Nested fields like `statuses`, `strategies`, `tags` must be placed **inside** the `tradeCharacteristics` object, not at the root level of `metricsConfig`.

#### ✅ Correct Structure:
```json
{
  "metricsConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "instruments": ["NIFTY", "BANKNIFTY"],
    "tradeCharacteristics": {
      "statuses": ["LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday"]
    }
  }
}
```

#### ❌ Incorrect Structure:
```json
{
  "metricsConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "symbols": ["NIFTY"],  // Wrong: should be "instruments"
    "statuses": ["LOSS"]    // Wrong: should be inside tradeCharacteristics
  }
}
```

### Field Name Conventions
- **Java/JSON**: camelCase (e.g., `portfolioIds`, `isDefault`)
- **Python**: snake_case (e.g., `portfolio_ids`, `is_default`)
- **Dart/Flutter**: camelCase (e.g., `portfolioIds`, `isDefault`)
- **TypeScript**: camelCase (e.g., `portfolioIds`, `isDefault`)

---

## 7. Testing Generated Models

### Java Testing
```java
@Test
public void testFavoriteFilterRequestSerialization() {
    MetricsFilterConfig config = MetricsFilterConfig.builder()
        .portfolioIds(List.of("163d0143-4fcb-480c-ac20-622f14e0e293"))
        .instruments(List.of("NIFTY", "BANKNIFTY"))
        .tradeCharacteristics(Map.of(
            "statuses", List.of("LOSS"),
            "strategies", List.of("Momentum Trading")
        ))
        .build();
    
    FavoriteFilterRequest request = FavoriteFilterRequest.builder()
        .name("Test Filter")
        .filterConfig(config)
        .build();
    
    String json = objectMapper.writeValueAsString(request);
    assertNotNull(json);
}
```

### Python Testing
```python
def test_favorite_filter_request():
    config = MetricsFilterConfig(
        portfolio_ids=["163d0143-4fcb-480c-ac20-622f14e0e293"],
        instruments=["NIFTY", "BANKNIFTY"],
        trade_characteristics={
            "statuses": ["LOSS"],
            "strategies": ["Momentum Trading"]
        }
    )
    
    request = FavoriteFilterRequest(
        name="Test Filter",
        filter_config=config
    )
    
    json_str = request.model_dump_json()
    assert json_str is not None
```

### TypeScript Testing
```typescript
const config: MetricsFilterConfig = {
  portfolioIds: ["163d0143-4fcb-480c-ac20-622f14e0e293"],
  instruments: ["NIFTY", "BANKNIFTY"],
  tradeCharacteristics: {
    statuses: ["LOSS"],
    strategies: ["Momentum Trading"]
  }
};

const request: FavoriteFilterRequest = {
  name: "Test Filter",
  filterConfig: config
};

const json = JSON.stringify(request);
console.log(json);
```

---

## 8. Code Generation Tools Summary

| Language | Tool | Command |
|----------|------|---------|
| Java | jsonschema2pojo | `mvn jsonschema2pojo:generate` |
| Java | OpenAPI Generator | `mvn openapi-generator:generate` |
| Python | datamodel-code-generator | `datamodel-codegen --input schema.json --output models.py` |
| Dart/Flutter | quicktype | `quicktype --src schema.json --lang dart --out models.dart` |
| TypeScript | quicktype | `quicktype --src schema.json --lang typescript --out models.ts` |
| TypeScript | json-schema-to-typescript | `json2ts -i schema.json -o models/` |

---

## Conclusion

This JSON schema provides a complete, language-agnostic definition of the FavoriteFilterController API. By using the appropriate code generation tools, you can:

1. ✅ Maintain type safety across multiple languages
2. ✅ Ensure consistency between client and server models
3. ✅ Automatically generate validation logic
4. ✅ Reduce manual coding errors
5. ✅ Keep models synchronized with API changes

For questions or issues, refer to the original Java source files in:
- `am-trade-api/src/main/java/am/trade/api/dto/`
- `am-trade-common/src/main/java/am/trade/common/models/`
