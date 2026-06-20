import logging
from unittest.mock import MagicMock, patch
from datetime import datetime
from sdk.client.trade_client import TradeClient
from sdk.config import SdkConfig
from sdk.models import Trade, PagedResponse

# Mock the BaseApiClient.get method
def mock_get(self, endpoint, **kwargs):
    if endpoint == "/api/v1/trades":
        return {
            "content": [
                {
                    "id": "trade1",
                    "portfolio_id": "port1",
                    "symbol": "AAPL",
                    "trade_type": "BUY",
                    "quantity": 10,
                    "entry_price": 150.0,
                    "entry_date": "2023-10-01T10:00:00Z",
                    "status": "OPEN"
                },
                {
                    "id": "trade2",
                    "portfolio_id": "port1",
                    "symbol": "GOOG",
                    "trade_type": "SELL",
                    "quantity": 5,
                    "entry_price": 2800.0,
                    "entry_date": "2023-10-02T11:00:00Z",
                    "status": "CLOSED"
                }
            ],
            "page_number": 0,
            "page_size": 20,
            "total_elements": 2,
            "total_pages": 1,
            "is_last": True,
            "is_first": True
        }
    return {}

def verify_sdk_changes():
    # Setup mock config
    config = SdkConfig(api_url="http://localhost:8080", api_key="test-key")
    
    # Initialize client
    client = TradeClient(config)
    
    # Patch the get method
    with patch('sdk.http.base_client.BaseApiClient.get', side_effect=mock_get, autospec=True):
        print("Verifying get_all_trades...")
        
        # Call the method
        paged_response = client.get_all_trades()
        
        # Validation
        assert isinstance(paged_response, PagedResponse), "Should return PagedResponse"
        assert len(paged_response) == 2, "Should have 2 items"
        
        trade1 = paged_response[0]
        assert isinstance(trade1, Trade), "Item should be Trade object"
        assert trade1.symbol == "AAPL"
        assert trade1.quantity == 10.0
        assert isinstance(trade1.entry_date, datetime)
        
        print("✓ get_all_trades returned correct models")
        print(f"  First trade: {trade1}")

if __name__ == "__main__":
    try:
        verify_sdk_changes()
        print("\nSUCCESS: SDK Verification Passed!")
    except AssertionError as e:
        print(f"\nFAILURE: Assertion failed - {e}")
    except Exception as e:
        print(f"\nFAILURE: Exception occurred - {e}")
