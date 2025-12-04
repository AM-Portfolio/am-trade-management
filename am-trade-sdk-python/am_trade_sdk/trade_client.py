"""Trade API client for AM Trade SDK."""

from typing import Any, Dict, List, Optional

from am_trade_sdk.base_client import BaseApiClient
from am_trade_sdk.models import Trade, TradeFilter


class TradeClient(BaseApiClient):
    """Client for trade operations."""

    def get_trade_by_id(self, trade_id: str) -> Trade:
        """
        Get trade by ID.

        Args:
            trade_id: Trade ID

        Returns:
            Trade: Trade object

        Raises:
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        response = self.get(f"/api/v1/trades/{trade_id}")
        return Trade(**response.get('data', response))

    def get_all_trades(
        self,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get all trades with pagination.

        Args:
            page: Page number (0-indexed)
            page_size: Page size

        Returns:
            dict: Paged response with trades

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            "/api/v1/trades",
            params={"page": page, "page_size": page_size}
        )

    def create_trade(self, trade_data: Dict[str, Any]) -> Trade:
        """
        Create new trade.

        Args:
            trade_data: Trade data

        Returns:
            Trade: Created trade

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post("/api/v1/trades", data=trade_data)
        return Trade(**response.get('data', response))

    def update_trade(
        self,
        trade_id: str,
        trade_data: Dict[str, Any]
    ) -> Trade:
        """
        Update existing trade.

        Args:
            trade_id: Trade ID
            trade_data: Updated trade data

        Returns:
            Trade: Updated trade

        Raises:
            ValidationException: If validation fails
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/trades/{trade_id}",
            data=trade_data
        )
        return Trade(**response.get('data', response))

    def delete_trade(self, trade_id: str) -> bool:
        """
        Delete trade.

        Args:
            trade_id: Trade ID

        Returns:
            bool: True if deleted

        Raises:
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        self.delete(f"/api/v1/trades/{trade_id}")
        return True

    def filter_trades(
        self,
        **filter_params
    ) -> Dict[str, Any]:
        """
        Filter trades with advanced criteria.

        Args:
            **filter_params: Filter parameters

        Returns:
            dict: Filtered trades response

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        filter_data = TradeFilter(**filter_params)
        return self.post(
            "/api/v1/trades/filter",
            data=filter_data.dict(exclude_none=True)
        )

    def get_trades_by_portfolio(
        self,
        portfolio_id: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get trades by portfolio.

        Args:
            portfolio_id: Portfolio ID
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged trades response

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/trades/portfolio/{portfolio_id}",
            params={"page": page, "page_size": page_size}
        )

    def get_trades_by_symbol(
        self,
        symbol: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get trades by symbol.

        Args:
            symbol: Trade symbol
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged trades response

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/trades/symbol/{symbol}",
            params={"page": page, "page_size": page_size}
        )

    def get_trade_stats(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get trade statistics.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Trade statistics

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/trades/stats/{portfolio_id}"
        )

    def batch_create_trades(
        self,
        trades: List[Dict[str, Any]]
    ) -> List[Trade]:
        """
        Create multiple trades.

        Args:
            trades: List of trade data

        Returns:
            List[Trade]: Created trades

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post(
            "/api/v1/trades/batch",
            data={"trades": trades}
        )
        trades_data = response.get('data', [])
        return [Trade(**t) for t in trades_data]

    def batch_delete_trades(
        self,
        trade_ids: List[str]
    ) -> Dict[str, Any]:
        """
        Delete multiple trades.

        Args:
            trade_ids: List of trade IDs

        Returns:
            dict: Deletion result

        Raises:
            ApiException: If API returns error
        """
        return self.post(
            "/api/v1/trades/batch/delete",
            data={"ids": trade_ids}
        )
