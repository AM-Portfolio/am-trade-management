"""
Tests for the TradeClient SDK.
Verifies behavior, data model usage, and "personality" (Pythonic interface).
"""

import unittest
from unittest.mock import MagicMock, patch
from datetime import datetime
from typing import Dict, Any

from sdk.client.trade_client import TradeClient
from sdk.models import Trade, PagedResponse
from sdk.dto import TradeResponse, TradeCreateRequest


class TestTradeClient(unittest.TestCase):
    """Test suite for TradeClient."""

    def setUp(self):
        """Set up test fixtures."""
        # Mock the base client
        self.mock_base_client_patcher = patch('sdk.client.trade_client.BaseApiClient.__init__')
        self.mock_base_init = self.mock_base_client_patcher.start()
        self.mock_base_init.return_value = None  # Don't actually init base
        
        self.client = TradeClient(base_url="http://test.api", api_key="secret")
        # Mock the requests methods on the instance
        self.client.get = MagicMock()
        self.client.post = MagicMock()
        self.client.put = MagicMock()
        self.client.delete = MagicMock()
        self.client.filter_response_by_tier = MagicMock(side_effect=lambda resp, type: resp) # Passthrough

    def tearDown(self):
        """Tear down test fixtures."""
        self.mock_base_client_patcher.stop()

    def test_get_trade_by_id_returns_trade_object(self):
        """Test that get_trade_by_id returns a proper Trade object (Model A)."""
        # Arrange
        mock_response = {
            "id": "trade-123",
            "portfolio_id": "port-456",
            "symbol": "AAPL",
            "trade_type": "BUY",
            "quantity": 100.0,
            "entry_price": 150.0,
            "entry_date": "2023-01-01T10:00:00",
            "status": "OPEN",
            "pnl": 0.0,
            "pnl_percentage": 0.0
        }
        self.client.get.return_value = mock_response

        # Act
        trade = self.client.get_trade_by_id("trade-123")

        # Assert
        self.assertIsInstance(trade, Trade)
        self.assertEqual(trade.id, "trade-123")
        self.assertEqual(trade.symbol, "AAPL")
        self.assertEqual(trade.quantity, 100.0)
        # Verify "personality" - we can access attributes using dot notation
        self.assertEqual(trade.entry_price, 150.0)
        self.client.get.assert_called_once_with("/api/v1/trades/trade-123", operation="READ", resource="TRADE")

    def test_get_all_trades_returns_paged_response_of_trades(self):
        """Test that get_all_trades returns a PagedResponse containing Trade objects."""
        # Arrange
        mock_response = {
            "content": [
                {
                    "id": "t1",
                    "portfolio_id": "p1",
                    "symbol": "GOOGL",
                    "trade_type": "SELL",
                    "status": "CLOSED"
                },
                {
                    "id": "t2",
                    "portfolio_id": "p1",
                    "symbol": "MSFT",
                    "trade_type": "BUY",
                    "status": "OPEN"
                }
            ],
            "page_number": 0,
            "page_size": 20,
            "total_elements": 2,
            "total_pages": 1,
            "is_last": True,
            "is_first": True
        }
        self.client.get.return_value = mock_response

        # Act
        response = self.client.get_all_trades(page=0, page_size=20)

        # Assert
        self.assertIsInstance(response, PagedResponse)
        self.assertEqual(len(response.items), 2)
        self.assertIsInstance(response.items[0], Trade)
        self.assertEqual(response.items[0].symbol, "GOOGL")
        self.assertEqual(response.items[1].symbol, "MSFT")
        self.assertEqual(response.total_elements, 2)

    def test_create_trade_converts_response_to_object(self):
        """Test creation flow wraps response in Trade object."""
        # Arrange
        trade_data = {
            "id": "new-trade",
            "symbol": "TSLA",
            "quantity": 10,
            "entry_price": 200.0
        }
        self.client.post.return_value = trade_data

        # Act
        result = self.client.create_trade(
            portfolio_id="p1",
            symbol="TSLA",
            trade_type="BUY",
            quantity=10,
            entry_price=200.0,
            entry_date="2023-01-01"
        )

        # Assert
        self.assertIsInstance(result, Trade)
        self.assertEqual(result.symbol, "TSLA")
        self.client.post.assert_called_once()
        
    def test_trade_model_personality(self):
        """Test the 'personality' of the Trade model (str representation)."""
        trade_data = {
            "id": "t1",
            "symbol": "NVDA",
            "trade_type": "BUY",
            "quantity": 50,
            "status": "OPEN",
            "pnl": 500.0
        }
        trade = Trade(trade_data)
        
        # Verify nicely formatted string representation
        str_repr = str(trade)
        self.assertIn("NVDA", str_repr)
        self.assertIn("BUY", str_repr)
        self.assertIn("OPEN", str_repr)
        self.assertIn("500.0", str_repr)

if __name__ == '__main__':
    unittest.main()
