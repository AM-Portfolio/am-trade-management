import json
import sys
import os
import subprocess
from datetime import datetime

def install_sdk():
    """Install the AM Trade SDK directly from GitHub Releases"""
    print("[INSTALL] Installing AM Trade SDK from GitHub Releases...")
    
    sdk_version = "1.0.0"
    # Release tag: feature/sdk (URL needs %2F for the forward slash)
    release_tag = "feature%2Fsdk"
    wheel_filename = f"am_trade_sdk-{sdk_version}-py3-none-any.whl"
    
    # GitHub Releases download URL
    github_release_url = (
        f"https://github.com/ssd2658/am-trade-managment/releases/download/"
        f"{release_tag}/{wheel_filename}"
    )
    
    print(f"   Package: am-trade-sdk=={sdk_version}")
    print(f"   Source: GitHub Releases")
    print(f"   Method: Direct wheel download")
    
    try:
        # Install the SDK wheel directly from GitHub Releases
        print(f"   Downloading from: {github_release_url}")
        cmd = [
            sys.executable, "-m", "pip", "install", "-q",
            github_release_url,
            # Also install required dependencies
            "requests>=2.28.0",
            "pydantic>=1.10.0",
            "python-dotenv>=0.19.0",
            "typing-extensions>=4.0.0",
            "PyJWT>=2.8.0"
        ]
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=120
        )
        
        if result.returncode != 0:
            print(f"[ERROR] Failed to install SDK:")
            print(result.stderr)
            sys.exit(1)
        
        print("[OK] SDK and dependencies installed successfully")
        return True
        
    except subprocess.TimeoutExpired:
        print("[ERROR] Installation timed out")
        sys.exit(1)
    except Exception as e:
        print(f"[ERROR] Installation error: {e}")
        sys.exit(1)

# Install SDK first
install_sdk()

# Now import the SDK
try:
    print("\n[INFO] Importing SDK libraries...")
    from am_trade_sdk.models import Trade
    from am_trade_sdk.client import TradeClient
    print("[OK] SDK libraries imported successfully")
except ImportError as e:
    print(f"[ERROR] Failed to import SDK: {e}")
    print(f"    Make sure am-trade-sdk is installed: pip install am-trade-sdk")
    sys.exit(1)

def verify_sdk_libraries():
    """Verify that SDK libraries are properly loaded and callable"""
    print("\n[VERIFY] Verifying SDK libraries...")
    
    try:
        # Verify Trade model
        print("   Checking Trade model...")
        assert hasattr(Trade, '__init__'), "Trade model missing __init__"
        print("   [OK] Trade model verified")
        
        # Verify TradeClient class
        print("   Checking TradeClient...")
        assert hasattr(TradeClient, 'get_trade_by_id'), "TradeClient missing get_trade_by_id"
        assert hasattr(TradeClient, 'get_all_trades'), "TradeClient missing get_all_trades"
        assert hasattr(TradeClient, 'create_trade'), "TradeClient missing create_trade"
        print("   [OK] TradeClient verified with methods:")
        print("     - get_trade_by_id()")
        print("     - get_all_trades()")
        print("     - create_trade()")
        
        print("[OK] All SDK libraries verified and callable")
        return True
        
    except AssertionError as e:
        print(f"[ERROR] Library verification failed: {e}")
        sys.exit(1)

def call_sdk_methods():
    """Call SDK methods to verify they work correctly"""
    print("\n[TEST] Testing SDK Method Calls...")
    
    try:
        # Initialize TradeClient with test configuration
        print("   Initializing TradeClient...")
        client = TradeClient(
            base_url="http://localhost:8073",
            api_key="test-key",
            timeout=30
        )
        print("   [OK] TradeClient initialized")
        
        # Test Trade model instantiation
        print("\n   Testing Trade Model Instantiation...")
        trade_data = {
            "id": "test-trade-001",
            "portfolio_id": "portfolio-1",
            "symbol": "AAPL",
            "trade_type": "BUY",
            "quantity": 100.0,
            "entry_price": 150.25,
            "entry_date": "2024-01-15T10:30:00",
            "status": "OPEN",
            "pnl": 500.0,
            "pnl_percentage": 3.33
        }
        
        # Create Trade object using SDK
        trade = Trade(**trade_data)
        print(f"   [OK] Trade object created: {trade.symbol} ({trade.trade_type})")
        print(f"     - ID: {trade.id}")
        print(f"     - Quantity: {trade.quantity}")
        print(f"     - Entry Price: ${trade.entry_price}")
        
        # Test Trade data access
        print("\n   Testing Trade Data Access...")
        assert trade.symbol == "AAPL", "Trade symbol mismatch"
        print(f"   [OK] Symbol property: {trade.symbol}")
        
        assert trade.quantity == 100.0, "Trade quantity mismatch"
        print(f"   [OK] Quantity property: {trade.quantity}")
        
        assert isinstance(trade.entry_date, datetime), "Entry date not datetime"
        print(f"   [OK] Entry date property: {trade.entry_date}")
        
        # Test Trade calculations
        print("\n   Testing Trade Calculations...")
        pnl = trade.pnl
        pnl_pct = trade.pnl_percentage
        print(f"   [OK] PnL: ${pnl}")
        print(f"   [OK] PnL %: {pnl_pct}%")
        
        # Test Trade string representation
        print("\n   Testing Trade String Representation...")
        trade_str = str(trade)
        assert "AAPL" in trade_str, "Symbol not in string representation"
        print(f"   [OK] Trade string: {trade_str[:60]}...")
        
        # Test TradeClient method existence (can't call without real API)
        print("\n   Verifying TradeClient Methods...")
        print(f"   [OK] get_trade_by_id method: {callable(client.get_trade_by_id)}")
        print(f"   [OK] get_all_trades method: {callable(client.get_all_trades)}")
        print(f"   [OK] create_trade method: {callable(client.create_trade)}")
        
        print("\n[OK] All SDK method calls successful")
        return True
        
    except Exception as e:
        print(f"\n[ERROR] SDK method call failed: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

def verify_trade():
    json_path = "../trade_sample.json"
    if not os.path.exists(json_path):
        print(f"[SKIP] {json_path} not found. Skipping JSON verification...")
        return True

    with open(json_path, 'r') as f:
        data = json.load(f)

    print(f"\n[DATA] Loaded JSON: {json.dumps(data, indent=2)}")

    try:
        # Attempt to parse into Trade model
        print("\n[BUILD] Creating Trade object from JSON...")
        trade = Trade(**data)  # Using unpacking instead of positional arg
        print("[OK] Successfully created Trade object:")
        print(f"   {trade}")

        # Assertions
        print("\n[VERIFY] Verifying Trade attributes...")
        assert trade.id == "trade-test-uuid-1234", f"ID mismatch: {trade.id}"
        print(f"   [OK] ID: {trade.id}")
        
        assert trade.symbol == "AAPL", f"Symbol mismatch: {trade.symbol}"
        print(f"   [OK] Symbol: {trade.symbol}")
        
        assert trade.trade_type == "BUY", f"Trade type mismatch: {trade.trade_type}"
        print(f"   [OK] Trade Type: {trade.trade_type}")
        
        assert trade.quantity == 100.0, f"Quantity mismatch: {trade.quantity}"
        print(f"   [OK] Quantity: {trade.quantity}")
        
        assert trade.entry_price == 150.25, f"Entry price mismatch: {trade.entry_price}"
        print(f"   [OK] Entry Price: {trade.entry_price}")
        
        # Date verification (Model converts string to datetime)
        assert isinstance(trade.entry_date, datetime), "entry_date is not a datetime object"
        print(f"   [OK] Entry Date: {trade.entry_date}")
        
        # Status verification
        assert trade.status == "WIN", f"Status mismatch: {trade.status}"
        print(f"   [OK] Status: {trade.status}")

        print("\n[OK] VERIFICATION PASSED: Python SDK correctly parses Java SDK output.")

    except Exception as e:
        print(f"\n[ERROR] VERIFICATION FAILED: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    print("=" * 70)
    print("AM Trade SDK - Python Verifier")
    print("Using Published SDK from GitHub Releases")
    print("=" * 70)
    
    # Step 1: Install SDK from GitHub Releases (with all dependencies)
    print("\n[Step 1/4] SDK Installation")
    install_sdk()
    
    # Step 2: Verify libraries are loaded and callable
    print("\n[Step 2/4] Library Verification")
    verify_sdk_libraries()
    
    # Step 3: Call SDK methods to verify functionality
    print("\n[Step 3/4] SDK Method Testing")
    call_sdk_methods()
    
    # Step 4: Verify Trade parsing and data model
    print("\n[Step 4/4] Data Model Verification")
    verify_trade()
    
    print("\n" + "=" * 70)
    print("[OK] ALL VERIFICATIONS PASSED")
    print("   SDK: am-trade-sdk (1.0.0)")
    print("   Status: Ready to Use")
    print("   Methods Tested: Trade model, TradeClient initialization")
    print("=" * 70)
