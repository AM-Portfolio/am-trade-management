import json
import sys
import os
from datetime import datetime

# Add the SDK to path so we can import from it (assuming we are running from am-trade-sdk-test/python-verifier)
sys.path.append(os.path.abspath("../../am-trade-sdk-python"))

try:
    from sdk.models import Trade
except ImportError as e:
    print(f"Failed to import SDK: {e}")
    sys.exit(1)

def verify_trade():
    json_path = "../trade_sample.json"
    if not os.path.exists(json_path):
        print(f"Error: {json_path} not found. Run java-dumper first.")
        sys.exit(1)

    with open(json_path, 'r') as f:
        data = json.load(f)

    print(f"Loaded JSON: {json.dumps(data, indent=2)}")

    try:
        # Attempt to parse into Model A
        trade = Trade(data)
        print("\nSuccessfully parsed Trade object:")
        print(trade)

        # Assertions
        assert trade.id == "trade-test-uuid-1234", f"ID mismatch: {trade.id}"
        assert trade.symbol == "AAPL", f"Symbol mismatch: {trade.symbol}"
        assert trade.trade_type == "BUY", f"Trade type mismatch: {trade.trade_type}"
        assert trade.quantity == 100.0, f"Quantity mismatch: {trade.quantity}"
        assert trade.entry_price == 150.25, f"Entry price mismatch: {trade.entry_price}"
        
        # Date verification (Model A converts string to datetime)
        assert isinstance(trade.entry_date, datetime), "entry_date is not a datetime object"
        
        # Status verification
        assert trade.status == "WIN", f"Status mismatch: {trade.status}"

        print("\n✅ VERIFICATION PASSED: Python SDK correctly parses Java SDK output.")

    except Exception as e:
        print(f"\n❌ VERIFICATION FAILED: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    verify_trade()
