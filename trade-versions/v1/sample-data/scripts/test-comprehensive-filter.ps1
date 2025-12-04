# Favorite Filter API - Comprehensive Filter Example (PowerShell)
# Run this script to test all filter creation endpoints

$baseUrl = "http://localhost:8073/api/v1/filters"
$userId = "ssd2658"

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Favorite Filter API - PowerShell Examples" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# 1. Create Comprehensive Filter
Write-Host "1. Creating comprehensive filter with all options..." -ForegroundColor Yellow
$comprehensiveFilter = @{
    name = "Comprehensive Filter Example"
    description = "Filter with all possible configuration options"
    isDefault = $false
    filterConfig = @{
        portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
        dateRange = @{
            startDate = "2025-01-01"
            endDate = "2025-12-31"
        }
        timePeriod = @{
            period = "LAST_30_DAYS"
            customStartDate = $null
            customEndDate = $null
        }
        metricTypes = @(
            "PERFORMANCE",
            "RISK",
            "DISTRIBUTION",
            "TIMING",
            "PATTERN",
            "PROFIT_LOSS",
            "WIN_RATE",
            "RISK_REWARD",
            "DRAWDOWN",
            "SHARPE_RATIO"
        )
        instruments = @(
            "NIFTY",
            "BANKNIFTY",
            "FINNIFTY",
            "NIFTY25JULFUT",
            "RELIANCE",
            "TCS",
            "INFY"
        )
        instrumentFilters = @{
            marketSegments = @(
                "EQUITY",
                "INDEX",
                "EQUITY_FUTURES",
                "INDEX_FUTURES",
                "EQUITY_OPTIONS",
                "INDEX_OPTIONS"
            )
            baseSymbols = @("NIFTY", "BANKNIFTY", "RELIANCE", "TCS")
            indexTypes = @("NIFTY", "BANKNIFTY", "FINNIFTY")
            derivativeTypes = @("FUTURES", "OPTIONS")
        }
        tradeCharacteristics = @{
            strategies = @(
                "Momentum Trading",
                "Scalping",
                "Swing Trading",
                "Options Strategy",
                "Breakout Strategy",
                "Mean Reversion"
            )
            tags = @(
                "futures",
                "momentum",
                "intraday",
                "win",
                "loss",
                "nifty",
                "index-futures",
                "technical-setup",
                "bullish-flag"
            )
            directions = @("LONG", "SHORT")
            statuses = @("OPEN", "CLOSED", "CANCELLED")
            minHoldingTimeHours = 1
            maxHoldingTimeHours = 168
        }
        profitLossFilters = @{
            minProfitLoss = -10000.0
            maxProfitLoss = 50000.0
            minPositionSize = 1000.0
            maxPositionSize = 100000.0
        }
        groupBy = @(
            "STRATEGY",
            "SYMBOL",
            "DAY_OF_WEEK",
            "MONTH",
            "TRADE_TYPE",
            "INSTRUMENT_TYPE",
            "DIRECTION"
        )
    }
} | ConvertTo-Json -Depth 10

try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl?userId=$userId" -Method Post -Body $comprehensiveFilter -ContentType "application/json"
    Write-Host "✓ Success: Filter created with ID: $($response1.id)" -ForegroundColor Green
    $filterId1 = $response1.id
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 2. Create High Risk Intraday Filter
Write-Host "2. Creating high-risk intraday filter..." -ForegroundColor Yellow
$highRiskFilter = @{
    name = "High Risk Intraday Trades"
    description = "Filter for high-risk intraday trades with momentum strategy"
    isDefault = $false
    filterConfig = @{
        portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
        instruments = @("NIFTY", "BANKNIFTY")
        tradeCharacteristics = @{
            statuses = @("LOSS")
            strategies = @("Momentum Trading")
            tags = @("intraday", "high-risk")
            maxHoldingTimeHours = 24
        }
        profitLossFilters = @{
            minProfitLoss = -10000.0
        }
        dateRange = @{
            startDate = "2025-01-01"
            endDate = "2025-12-31"
        }
    }
} | ConvertTo-Json -Depth 10

try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl?userId=$userId" -Method Post -Body $highRiskFilter -ContentType "application/json"
    Write-Host "✓ Success: Filter created with ID: $($response2.id)" -ForegroundColor Green
    $filterId2 = $response2.id
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 3. Create Winning Trades Filter
Write-Host "3. Creating winning trades filter..." -ForegroundColor Yellow
$winningFilter = @{
    name = "Winning Trades"
    description = "All winning trades with profit above 5000"
    isDefault = $false
    filterConfig = @{
        portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
        tradeCharacteristics = @{
            statuses = @("WIN", "CLOSED")
        }
        profitLossFilters = @{
            minProfitLoss = 5000.0
        }
        timePeriod = @{
            period = "LAST_30_DAYS"
        }
    }
} | ConvertTo-Json -Depth 10

try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl?userId=$userId" -Method Post -Body $winningFilter -ContentType "application/json"
    Write-Host "✓ Success: Filter created with ID: $($response3.id)" -ForegroundColor Green
    $filterId3 = $response3.id
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 4. Create Options Strategy Filter
Write-Host "4. Creating options strategy filter..." -ForegroundColor Yellow
$optionsFilter = @{
    name = "Options Trading Analysis"
    description = "Filter for analyzing options trading performance"
    isDefault = $false
    filterConfig = @{
        portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
        instruments = @("NIFTY", "BANKNIFTY", "FINNIFTY")
        instrumentFilters = @{
            marketSegments = @("INDEX_OPTIONS")
            indexTypes = @("NIFTY", "BANKNIFTY")
            derivativeTypes = @("OPTIONS")
        }
        tradeCharacteristics = @{
            strategies = @("Options Strategy")
            tags = @("options", "premium-selling")
        }
        metricTypes = @("PERFORMANCE", "RISK", "WIN_RATE", "RISK_REWARD")
        groupBy = @("STRATEGY", "SYMBOL")
    }
} | ConvertTo-Json -Depth 10

try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl?userId=$userId" -Method Post -Body $optionsFilter -ContentType "application/json"
    Write-Host "✓ Success: Filter created with ID: $($response4.id)" -ForegroundColor Green
    $filterId4 = $response4.id
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 5. Get All Filters
Write-Host "5. Getting all filters..." -ForegroundColor Yellow
try {
    $allFilters = Invoke-RestMethod -Uri "$baseUrl?userId=$userId" -Method Get
    Write-Host "✓ Success: Retrieved $($allFilters.Count) filters" -ForegroundColor Green
    $allFilters | ForEach-Object {
        Write-Host "  - $($_.name) (ID: $($_.id))" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 6. Get Default Filter
Write-Host "6. Getting default filter..." -ForegroundColor Yellow
try {
    $defaultFilter = Invoke-RestMethod -Uri "$baseUrl/default?userId=$userId" -Method Get
    if ($defaultFilter) {
        Write-Host "✓ Success: Default filter is '$($defaultFilter.name)'" -ForegroundColor Green
    } else {
        Write-Host "✓ No default filter set" -ForegroundColor Gray
    }
} catch {
    Write-Host "✓ No default filter found (this is normal)" -ForegroundColor Gray
}

Write-Host ""

# 7. Set First Filter as Default (if created)
if ($filterId1) {
    Write-Host "7. Setting first filter as default..." -ForegroundColor Yellow
    try {
        $defaultSet = Invoke-RestMethod -Uri "$baseUrl/$filterId1/default?userId=$userId" -Method Put
        Write-Host "✓ Success: Filter '$($defaultSet.name)' set as default" -ForegroundColor Green
    } catch {
        Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "All operations completed!" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Created Filter IDs:" -ForegroundColor Yellow
if ($filterId1) { Write-Host "  1. Comprehensive: $filterId1" -ForegroundColor Gray }
if ($filterId2) { Write-Host "  2. High Risk: $filterId2" -ForegroundColor Gray }
if ($filterId3) { Write-Host "  3. Winning: $filterId3" -ForegroundColor Gray }
if ($filterId4) { Write-Host "  4. Options: $filterId4" -ForegroundColor Gray }
