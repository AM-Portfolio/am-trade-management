"""
Trade Model for SDK.

This is "Model A" - a wrapper around the raw API response data 
that provides a clean, Pythonic interface for SDK users.
"""

from datetime import datetime
from typing import Any, Dict, Optional

class Trade:
    """
    Trade model representing a single trade.
    
    Attributes:
        id (str): Trade ID
        portfolio_id (str): Portfolio ID
        symbol (str): Stock symbol
        trade_type (str): Trade type (BUY, SELL, etc.)
        quantity (float): Trade quantity
        entry_price (float): Entry price
        entry_date (datetime): Entry date
        exit_price (Optional[float]): Exit price
        exit_date (Optional[datetime]): Exit date
        status (str): Trade status
        notes (Optional[str]): Trade notes
        pnl (Optional[float]): Profit/Loss
        pnl_percentage (Optional[float]): P&L Percentage
    """

    def __init__(self, data: Dict[str, Any]):
        """
        Initialize Trade model from API response dictionary.

        Args:
            data: Dictionary containing trade data
        """
        self._data = data
        
        self.id = data.get("id")
        self.portfolio_id = data.get("portfolio_id")
        self.symbol = data.get("symbol")
        self.trade_type = data.get("trade_type")
        self.quantity = float(data.get("quantity", 0.0))
        self.entry_price = float(data.get("entry_price", 0.0))
        
        # Handle date parsing
        self.entry_date = self._parse_date(data.get("entry_date"))
        
        # Optional fields
        self.exit_price = float(data.get("exit_price")) if data.get("exit_price") is not None else None
        self.exit_date = self._parse_date(data.get("exit_date"))
        
        self.status = data.get("status")
        self.notes = data.get("notes")
        
        self.pnl = float(data.get("pnl")) if data.get("pnl") is not None else None
        self.pnl_percentage = float(data.get("pnl_percentage")) if data.get("pnl_percentage") is not None else None

    def _parse_date(self, date_str: Any) -> Optional[datetime]:
        """Parse datetime string to object."""
        if not date_str:
            return None
        if isinstance(date_str, datetime):
            return date_str
        try:
            return datetime.fromisoformat(str(date_str).replace('Z', '+00:00'))
        except (ValueError, TypeError):
            return None

    def __repr__(self):
        """String representation of the trade."""
        return (
            f"<Trade {self.symbol} {self.trade_type} "
            f"qty={self.quantity} price={self.entry_price} "
            f"status={self.status}>"
        )
    
    def to_dict(self) -> Dict[str, Any]:
        """Return the raw data dictionary."""
        return self._data
