"""Trade API client for AM Trade SDK."""

from typing import Any, Dict, List, Optional

from sdk.http.base_client import BaseApiClient
from sdk.dto import (
    DTOTransformer,
    TradeCreateRequest,
    TradeFilterRequest,
    TradeResponse,
    TradeUpdateRequest,
)


class TradeClient(BaseApiClient):
    """Client for trade operations using DTOs only."""

    def get_trade_by_id(self, trade_id: str) -> TradeResponse:
        """
        Get trade by ID.

        Args:
            trade_id: Trade ID

        Returns:
            TradeResponse: Trade object (DTO only) - filtered by user tier

        Raises:
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        response = self.get(f"/api/v1/trades/{trade_id}", operation="READ", resource="TRADE")
        # Filter response based on user tier
        response = self.filter_response_by_tier(response, "trade")
        return TradeResponse(**response.get('data', response))

    def get_all_trades(
        self,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get all trades with pagination.

        Args:
            page: Page number (0-indexed)
            page_size: Page size (limited by user tier)

        Returns:
            dict: Paged response with trades - filtered by user tier

        Raises:
            ApiException: If API returns error
        """
        response = self.get(
            "/api/v1/trades",
            params={"page": page, "page_size": page_size}
        )
        # Filter response based on user tier
        response = self.filter_response_by_tier(response, "trade")
        return response

    def create_trade(
        self,
        portfolio_id: str,
        symbol: str,
        trade_type: str,
        quantity: float,
        entry_price: float,
        entry_date: str,
        notes: str = None
    ) -> TradeResponse:
        """
        Create new trade using DTO.

        Args:
            portfolio_id: Portfolio ID
            symbol: Stock symbol
            trade_type: Trade type (BUY, SELL, SHORT, COVER)
            quantity: Trade quantity
            entry_price: Entry price
            entry_date: Entry date
            notes: Optional notes

        Returns:
            TradeResponse: Created trade (DTO only)

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        # Create request DTO (validates user input)
        request_dto = TradeCreateRequest(
            portfolio_id=portfolio_id,
            symbol=symbol,
            trade_type=trade_type,
            quantity=quantity,
            entry_price=entry_price,
            entry_date=entry_date,
            notes=notes
        )
        
        response = self.post(
            "/api/v1/trades",
            data=request_dto.dict(exclude_none=True),
            operation="CREATE",
            resource="TRADE"
        )
        return TradeResponse(**response.get('data', response))

    def update_trade(
        self,
        trade_id: str,
        exit_price: float = None,
        exit_date: str = None,
        status: str = None,
        notes: str = None
    ) -> TradeResponse:
        """
        Update existing trade using DTO.

        Args:
            trade_id: Trade ID
            exit_price: Exit price
            exit_date: Exit date
            status: Trade status
            notes: Updated notes

        Returns:
            TradeResponse: Updated trade (DTO only)

        Raises:
            ValidationException: If validation fails
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        # Create update DTO (validates user input)
        update_dto = TradeUpdateRequest(
            exit_price=exit_price,
            exit_date=exit_date,
            status=status,
            notes=notes
        )
        
        response = self.put(
            f"/api/v1/trades/{trade_id}",
            data=update_dto.dict(exclude_none=True),
            operation="UPDATE",
            resource="TRADE"
        )
        return TradeResponse(**response.get('data', response))

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
        self.delete(f"/api/v1/trades/{trade_id}", operation="DELETE", resource="TRADE")
        return True

    def filter_trades(
        self,
        portfolio_id: str = None,
        symbol: str = None,
        status: str = None,
        trade_type: str = None,
        min_pnl: float = None,
        max_pnl: float = None,
        start_date: str = None,
        end_date: str = None,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Filter trades with advanced criteria using DTO.

        Args:
            portfolio_id: Filter by portfolio
            symbol: Filter by symbol
            status: Filter by status
            trade_type: Filter by trade type
            min_pnl: Minimum PnL
            max_pnl: Maximum PnL
            start_date: Start date
            end_date: End date
            page: Page number
            page_size: Page size

        Returns:
            dict: Filtered trades response

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        # Create filter DTO (validates user input)
        filter_dto = TradeFilterRequest(
            portfolio_id=portfolio_id,
            symbol=symbol,
            status=status,
            trade_type=trade_type,
            min_pnl=min_pnl,
            max_pnl=max_pnl,
            start_date=start_date,
            end_date=end_date,
            page=page,
            page_size=page_size
        )
        
        return self.post(
            "/api/v1/trades/filter",
            data=filter_dto.dict(exclude_none=True),
            operation="READ",
            resource="TRADE"
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
    ) -> List[TradeResponse]:
        """
        Create multiple trades using DTOs.

        Args:
            trades: List of trade data

        Returns:
            List[TradeResponse]: Created trades (DTOs only)

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        # Validate each trade with DTO
        validated_trades = []
        for trade_data in trades:
            trade_dto = TradeCreateRequest(**trade_data)
            validated_trades.append(trade_dto.dict(exclude_none=True))
        
        response = self.post(
            "/api/v1/trades/batch",
            data={"trades": validated_trades},
            operation="CREATE_BATCH",
            resource="TRADE"
        )
        trades_data = response.get('data', [])
        return [TradeResponse(**t) for t in trades_data]

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

    def get_trades_by_free_tab(
        self,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get trades available in FREE tier.

        Returns only trades with fields accessible to FREE tier users:
        id, portfolio_id, symbol, trade_type, quantity, entry_price, entry_date, status, pnl, pnl_percentage

        Args:
            page: Page number (0-indexed)
            page_size: Page size (max 20 for FREE tier)

        Returns:
            dict: Paged response containing FREE tier trades - filtered by tier automatically

        Raises:
            ApiException: If API returns error
        """
        # Enforce FREE tier limits
        effective_page_size = min(page_size, 20)
        if page_size > 20:
            print(f"Warning: Requested page size {page_size} exceeds FREE tier limit of 20, using 20")

        try:
            params = {
                "page": page,
                "size": effective_page_size,
                "tier": "FREE"
            }
            response = self.get(
                "/api/v1/trades/free-tab",
                params=params,
                operation="READ",
                resource="TRADE"
            )
            # Filter response based on user tier (FREE tier fields only)
            response = self.filter_response_by_tier(response, "trade")
            return response
        except Exception as e:
            raise Exception(f"Failed to fetch FREE tier trades: {str(e)}")

    def get_trades_by_free_tab_and_symbol(
        self,
        symbol: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get trades available in FREE tier filtered by symbol.

        Args:
            symbol: Trade symbol to filter by
            page: Page number (0-indexed)
            page_size: Page size (max 20 for FREE tier)

        Returns:
            dict: Paged response containing filtered FREE tier trades

        Raises:
            ValueError: If symbol is empty
            ApiException: If API returns error
        """
        if not symbol or not symbol.strip():
            raise ValueError("Symbol cannot be empty")

        # Enforce FREE tier limits
        effective_page_size = min(page_size, 20)

        try:
            params = {
                "page": page,
                "size": effective_page_size,
                "tier": "FREE"
            }
            response = self.get(
                f"/api/v1/trades/free-tab/symbol/{symbol}",
                params=params,
                operation="READ",
                resource="TRADE"
            )
            # Filter response based on user tier
            response = self.filter_response_by_tier(response, "trade")
            return response
        except Exception as e:
            raise Exception(f"Failed to fetch FREE tier trades for symbol {symbol}: {str(e)}")

    def get_trades_by_free_tab_and_status(
        self,
        status: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get trades available in FREE tier filtered by status.

        Args:
            status: Trade status to filter by (WIN, LOSS, OPEN, CLOSED, etc.)
            page: Page number (0-indexed)
            page_size: Page size (max 20 for FREE tier)

        Returns:
            dict: Paged response containing filtered FREE tier trades

        Raises:
            ValueError: If status is empty
            ApiException: If API returns error
        """
        if not status or not status.strip():
            raise ValueError("Status cannot be empty")

        # Enforce FREE tier limits
        effective_page_size = min(page_size, 20)

        try:
            params = {
                "page": page,
                "size": effective_page_size,
                "tier": "FREE"
            }
            response = self.get(
                f"/api/v1/trades/free-tab/status/{status}",
                params=params,
                operation="READ",
                resource="TRADE"
            )
            # Filter response based on user tier
            response = self.filter_response_by_tier(response, "trade")
            return response
        except Exception as e:
            raise Exception(f"Failed to fetch FREE tier trades with status {status}: {str(e)}")

