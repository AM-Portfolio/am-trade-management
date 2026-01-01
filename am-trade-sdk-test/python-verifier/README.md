# Python SDK Verifier

## Overview

The Python Verifier is a comprehensive verification tool that validates the AM Trade Python SDK installation, library functionality, and data model correctness.

## What It Does

The verifier performs **three key verification steps**:

### 1. **SDK Installation** (`install_sdk()`)
- Installs the AM Trade SDK using `pip install -e` (editable/development mode)
- Resolves all dependencies automatically
- Validates successful installation
- Provides clear error messages if installation fails

### 2. **Library Verification** (`verify_sdk_libraries()`)
- Imports Trade model from `am_trade_sdk.models`
- Imports TradeClient from `am_trade_sdk.client`
- Verifies Trade class has proper methods
- Verifies TradeClient has FREE tab functionality
- Confirms all libraries are properly loaded and callable

### 3. **Trade Data Verification** (`verify_trade()`)
- Loads sample trade JSON data
- Creates Trade object from JSON using SDK
- Validates all trade attributes match expected values
- Confirms proper data type conversions (string → datetime)

## Prerequisites

- Python 3.8 or higher
- `pip` package manager
- Network access (for pip install)
- Java SDK must have been run first to generate `trade_sample.json`

## Installation & Usage

### Step 1: Ensure SDK Structure
```
am-trade-managment/
├── am-trade-sdk-python/        ← Python SDK to install
│   ├── pyproject.toml
│   ├── README.md
│   └── am_trade_sdk/
│       ├── models.py
│       └── client.py
│
└── am-trade-sdk-test/
    └── python-verifier/        ← You are here
        ├── verify.py           ← Main verifier script
        └── README.md           ← This file
```

### Step 2: Generate Test Data

First, run the Java dumper to create sample data:
```bash
cd am-trade-sdk-test/java-dumper
# Run the Java dumper to generate trade_sample.json
```

### Step 3: Run the Verifier

```bash
cd am-trade-sdk-test/python-verifier
python verify.py
```

## Expected Output

### Success Output

```
======================================================================
🚀 AM Trade SDK - Python Verifier
======================================================================
📦 Installing AM Trade SDK...
   SDK path: A:\...\am-trade-sdk-python
✅ SDK installed successfully

📚 Importing SDK libraries...
✅ SDK libraries imported successfully

🔍 Verifying SDK libraries...
   Checking Trade model...
   ✓ Trade model verified
   Checking TradeClient...
   ✓ TradeClient verified
✅ All SDK libraries verified and callable

📋 Loaded JSON: {
  "id": "trade-test-uuid-1234",
  "symbol": "AAPL",
  ...
}

📦 Creating Trade object from JSON...
✅ Successfully created Trade object:
   Trade(id='trade-test-uuid-1234', symbol='AAPL', ...)

🔍 Verifying Trade attributes...
   ✓ ID: trade-test-uuid-1234
   ✓ Symbol: AAPL
   ✓ Trade Type: BUY
   ✓ Quantity: 100.0
   ✓ Entry Price: 150.25
   ✓ Entry Date: 2024-01-15 10:30:00
   ✓ Status: WIN

✅ VERIFICATION PASSED: Python SDK correctly parses Java SDK output.

======================================================================
✅ ALL VERIFICATIONS PASSED
======================================================================
```

### Error Scenarios

**SDK Not Found**
```
📦 Installing AM Trade SDK...
   SDK path: A:\...\am-trade-sdk-python
❌ Error: SDK path not found at A:\...\am-trade-sdk-python
```
**Solution**: Verify paths are correct relative to script location

**Import Failure**
```
📚 Importing SDK libraries...
❌ Failed to import SDK: No module named 'am_trade_sdk'
```
**Solution**: Check SDK installation, verify pyproject.toml exists

**Test Data Not Found**
```
📋 Loaded JSON: Error: ../trade_sample.json not found. Run java-dumper first.
```
**Solution**: Run Java dumper first to generate trade_sample.json

## How It Uses Libraries

### Installation Phase
```python
# Uses subprocess to run pip install
subprocess.run(
    [sys.executable, "-m", "pip", "install", "-e", sdk_path],
    ...
)
```
This installs the SDK so it can be imported anywhere in the Python environment.

### Import Phase
```python
# Now libraries are available system-wide
from am_trade_sdk.models import Trade
from am_trade_sdk.client import TradeClient
```
Libraries loaded directly from the installed package.

### Verification Phase
```python
# Directly call library methods and verify functionality
trade = Trade(**data)              # Instantiate Trade model
assert hasattr(TradeClient, 'get_trades_by_free_tab')  # Verify methods exist
```
Direct library usage to validate proper installation and functionality.

## Workflow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│  Python Verifier (verify.py)                                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. INSTALL SDK                                              │
│  ├─ Resolve SDK path                                        │
│  ├─ Run: pip install -e {sdk_path}                          │
│  └─ Verify success                                          │
│                                                              │
│  2. IMPORT LIBRARIES                                         │
│  ├─ from am_trade_sdk.models import Trade                   │
│  ├─ from am_trade_sdk.client import TradeClient             │
│  └─ Handle import errors                                    │
│                                                              │
│  3. VERIFY LIBRARIES                                         │
│  ├─ Check Trade.__init__ exists                             │
│  ├─ Check TradeClient.get_trades_by_free_tab exists         │
│  └─ Confirm callable and accessible                         │
│                                                              │
│  4. VERIFY DATA                                              │
│  ├─ Load trade_sample.json                                  │
│  ├─ Create Trade object: Trade(**data)                      │
│  ├─ Verify all attributes                                   │
│  ├─ Verify type conversions (str → datetime)                │
│  └─ Assert expected values match                            │
│                                                              │
│  5. REPORT RESULTS                                           │
│  └─ Summary of all verifications                            │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Configuration

### Timeout Settings
```python
timeout=60  # 60 seconds for pip install (adjustable)
```

### SDK Path Resolution
```python
sdk_path = os.path.abspath("../../am-trade-sdk-python")
# Relative to script location: am-trade-sdk-test/python-verifier/verify.py
#                          ├─ ../          = am-trade-sdk-test
#                          └─ ../../       = am-trade-managment
```

### Expected Trade Data Format
```json
{
  "id": "trade-test-uuid-1234",
  "symbol": "AAPL",
  "trade_type": "BUY",
  "quantity": 100.0,
  "entry_price": 150.25,
  "entry_date": "2024-01-15T10:30:00",
  "status": "WIN"
}
```

## Troubleshooting

### Issue: "pip command not found"
```bash
# Solution: Use python -m pip instead
python -m pip install package-name
# (Script already handles this automatically)
```

### Issue: "Permission denied" on pip install
```bash
# Solution: Install in user directory
pip install --user -e {sdk_path}
# Or use virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

### Issue: "Module not found" after install
```bash
# Solution: Refresh pip cache
pip install --no-cache-dir -e {sdk_path}
# Or check SDK has proper __init__.py files
```

### Issue: Trade creation fails
```bash
# Check JSON format in trade_sample.json
# Ensure all required fields present
# Verify field types match model expectations
```

## Extension Points

### Adding More Library Verifications

```python
def verify_sdk_libraries():
    # Add new verifications here
    print("   Checking new feature...")
    assert hasattr(TradeClient, 'new_method'), "Missing new_method"
    print("   ✓ New feature verified")
```

### Adding More Trade Assertions

```python
def verify_trade():
    # Add new assertions
    assert trade.exit_price > 0, "Exit price should be positive"
    assert trade.pnl_percentage != 0, "PnL percentage shouldn't be zero"
```

### Adding Data Validation Tests

```python
def verify_edge_cases():
    """Test edge cases and boundary conditions"""
    # Test with minimal data
    # Test with maximum values
    # Test with null fields
```

## Integration with CI/CD

### GitHub Actions Example
```yaml
- name: Run Python SDK Verifier
  run: |
    cd am-trade-sdk-test/python-verifier
    python verify.py
```

### Jenkins Example
```groovy
stage('Verify Python SDK') {
    steps {
        dir('am-trade-sdk-test/python-verifier') {
            sh 'python verify.py'
        }
    }
}
```

## Performance

- **Installation Time**: 5-15 seconds (depends on network and system)
- **Library Loading**: <1 second
- **Total Execution**: ~10-20 seconds

## Dependencies Used by Verifier

| Dependency | Version | Purpose |
|---|---|---|
| Python | 3.8+ | Runtime environment |
| pip | Any | Package manager |
| subprocess | Built-in | Run pip install |
| json | Built-in | Parse test data |
| datetime | Built-in | Type validation |

## SDK Dependencies (Installed)

| Dependency | Version | Purpose |
|---|---|---|
| requests | Latest | HTTP client |
| pydantic | 1.10+ | Data validation |
| PyJWT | 2.8.0+ | JWT token handling |
| python-dotenv | Latest | Configuration |

## Summary

The Python Verifier provides a **comprehensive validation pipeline** that:

✅ **Installs** the SDK using standard pip install  
✅ **Imports** libraries programmatically  
✅ **Verifies** libraries are properly loaded and callable  
✅ **Validates** data model functionality  
✅ **Reports** detailed results with clear success/failure messaging  

This approach ensures the SDK is correctly packaged, distributed, and ready for use.

---

**Last Updated**: December 6, 2025  
**Status**: Complete and Tested  
**Version**: 1.0.0
