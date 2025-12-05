"""
User-Facing Data Transfer Objects (DTOs) - Public API for SDK Users.

These DTOs are what SDK users interact with. They are intentionally limited
to prevent direct exposure of backend models. Never expose internal models directly.

Users CANNOT directly hit backend APIs with these - they must use the SDK.
Matches Java SDK DTO structure exactly for consistency across languages.
"""

from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field, validator


class TradeTypeEnum(str, Enum):
    """Trade type enumeration - Limited subset."""

    BUY = "BUY"
    SELL = "SELL"
    SHORT = "SHORT"
    COVER = "COVER"


class TradeStatusEnum(str, Enum):
    """Trade status enumeration - Limited subset."""

    OPEN = "OPEN"
    CLOSED = "CLOSED"
    PENDING = "PENDING"
    CANCELLED = "CANCELLED"


class TradeCreateRequest(BaseModel):
    """
    DTO for creating a trade - User provides this.

    This is what SDK users provide. Backend will NOT accept this directly.
    Only SDK can send this with proper metadata wrapper.
    
    Matches: Java TradeDTO.TradeCreateRequest
    """

    portfolio_id: str = Field(..., min_length=1, max_length=50, description="Portfolio ID")
    symbol: str = Field(..., min_length=1, max_length=20, description="Stock symbol")
    trade_type: TradeTypeEnum = Field(..., description="Type of trade")
    quantity: float = Field(..., gt=0, le=100000, description="Quantity (1-100000)")
    entry_price: float = Field(..., gt=0, le=999999.99, description="Entry price")
    entry_date: datetime = Field(..., description="Entry date and time")
    notes: Optional[str] = Field(None, max_length=500, description="Optional notes (max 500 chars)")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeUpdateRequest(BaseModel):
    """
    DTO for updating a trade - User provides this.

    Only these fields can be updated by SDK users.
    Matches: Java TradeDTO.TradeUpdateRequest
    """

    exit_price: Optional[float] = Field(None, gt=0, le=999999.99, description="Exit price")
    exit_date: Optional[datetime] = Field(None, description="Exit date and time")
    status: Optional[TradeStatusEnum] = Field(None, description="Trade status")
    notes: Optional[str] = Field(None, max_length=500, description="Updated notes")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeResponse(BaseModel):
    """
    DTO for trade responses - Backend returns this.

    This is what users GET back. Contains only non-sensitive fields.
    Internal backend fields are NEVER included.
    Matches: Java TradeDTO.TradeResponse
    """

    id: str = Field(..., description="Trade ID")
    portfolio_id: str = Field(..., description="Portfolio ID")
    symbol: str = Field(..., description="Stock symbol")
    trade_type: str = Field(..., description="Trade type")
    quantity: float = Field(..., description="Trade quantity")
    entry_price: float = Field(..., description="Entry price")
    entry_date: datetime = Field(..., description="Entry date")
    exit_price: Optional[float] = Field(None, description="Exit price (if closed)")
    exit_date: Optional[datetime] = Field(None, description="Exit date (if closed)")
    status: str = Field(..., description="Current status")
    notes: Optional[str] = Field(None, description="Trade notes")
    pnl: Optional[float] = Field(None, description="Profit/Loss")
    pnl_percentage: Optional[float] = Field(None, description="P&L percentage")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PortfolioCreateRequest(BaseModel):
    """
    DTO for creating a portfolio.
    
    Matches: Java PortfolioDTO.PortfolioCreateRequest
    """

    name: str = Field(..., min_length=1, max_length=100, description="Portfolio name")
    description: Optional[str] = Field(None, max_length=500, description="Portfolio description")
    initial_capital: float = Field(..., gt=0, le=9999999.99, description="Initial capital")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PortfolioUpdateRequest(BaseModel):
    """
    DTO for updating a portfolio.
    
    Matches: Java PortfolioDTO.PortfolioUpdateRequest
    """

    name: Optional[str] = Field(None, min_length=1, max_length=100)
    description: Optional[str] = Field(None, max_length=500)

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PortfolioResponse(BaseModel):
    """
    DTO for portfolio responses - Limited fields only.
    
    Matches: Java PortfolioDTO.PortfolioResponse
    """

    id: str = Field(..., description="Portfolio ID")
    name: str = Field(..., description="Portfolio name")
    description: Optional[str] = Field(None, description="Portfolio description")
    initial_capital: float = Field(..., description="Initial capital invested")
    current_value: float = Field(..., description="Current portfolio value")
    total_pnl: float = Field(..., description="Total profit/loss")
    total_pnl_percentage: float = Field(..., description="Total PnL percentage")
    total_trades: int = Field(default=0, description="Number of trades")
    active_trades: int = Field(default=0, description="Number of active trades")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeFilterRequest(BaseModel):
    """
    DTO for filtering trades.
    
    Matches filter request pattern from Java SDK.
    """

    portfolio_id: Optional[str] = Field(None, description="Filter by portfolio")
    symbol: Optional[str] = Field(None, max_length=20, description="Filter by symbol")
    status: Optional[TradeStatusEnum] = Field(None, description="Filter by status")
    trade_type: Optional[TradeTypeEnum] = Field(None, description="Filter by trade type")
    min_pnl: Optional[float] = Field(None, description="Minimum PnL")
    max_pnl: Optional[float] = Field(None, description="Maximum PnL")
    start_date: Optional[datetime] = Field(None, description="Start date")
    end_date: Optional[datetime] = Field(None, description="End date")
    page: int = Field(default=0, ge=0, description="Page number (0-indexed)")
    page_size: int = Field(default=20, ge=1, le=100, description="Items per page (1-100)")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class JournalEntryCreateRequest(BaseModel):
    """DTO for creating journal entry."""

    trade_id: Optional[str] = Field(None, description="Associated trade ID")
    title: str = Field(..., min_length=1, max_length=200, description="Entry title")
    content: str = Field(..., min_length=1, max_length=5000, description="Entry content")
    tags: Optional[List[str]] = Field(None, max_items=10, description="Tags (max 10)")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class JournalEntryResponse(BaseModel):
    """DTO for journal entry responses."""

    id: str = Field(..., description="Entry ID")
    trade_id: Optional[str] = Field(None, description="Associated trade ID")
    title: str = Field(..., description="Entry title")
    content: str = Field(..., description="Entry content")
    tags: Optional[List[str]] = Field(None, description="Entry tags")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Last update timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class FavoriteFilterCreateRequest(BaseModel):
    """DTO for creating favorite filter."""

    name: str = Field(..., min_length=1, max_length=100, description="Filter name")
    criteria: Dict[str, Any] = Field(..., description="Filter criteria")
    description: Optional[str] = Field(None, max_length=500, description="Filter description")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class FavoriteFilterResponse(BaseModel):
    """DTO for filter responses."""

    id: str = Field(..., description="Filter ID")
    name: str = Field(..., description="Filter name")
    criteria: Dict[str, Any] = Field(..., description="Filter criteria")
    description: Optional[str] = Field(None, description="Filter description")
    is_shared: bool = Field(..., description="Is filter shared")
    created_by: str = Field(..., description="Created by (user ID)")
    created_at: datetime = Field(..., description="Creation timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PagedResponse(BaseModel):
    """DTO for paginated responses."""

    content: List[Dict[str, Any]] = Field(..., description="Page content")
    page_number: int = Field(..., ge=0, description="Page number (0-indexed)")
    page_size: int = Field(..., ge=1, description="Items per page")
    total_elements: int = Field(..., ge=0, description="Total items")
    total_pages: int = Field(..., ge=0, description="Total pages")
    is_last: bool = Field(..., description="Is this last page")
    is_first: bool = Field(..., description="Is this first page")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class ErrorResponse(BaseModel):
    """DTO for error responses."""

    status_code: int = Field(..., description="HTTP status code")
    message: str = Field(..., description="Error message")
    error_code: str = Field(..., description="Error code")
    details: Optional[Dict[str, Any]] = Field(None, description="Additional error details")
    request_id: Optional[str] = Field(None, description="Request ID for tracking")
    timestamp: datetime = Field(default_factory=datetime.utcnow, description="Error timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class SuccessResponse(BaseModel):
    """DTO for successful responses."""

    data: Optional[Dict[str, Any]] = Field(None, description="Response data")
    message: str = Field(..., description="Success message")
    request_id: Optional[str] = Field(None, description="Request ID for tracking")
    timestamp: datetime = Field(default_factory=datetime.utcnow, description="Response timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class DTOTransformer:
    """
    Transform between user DTOs and internal models.

    This layer ensures users NEVER see internal fields.
    Always use this transformer - NEVER expose internal models directly.
    """

    @staticmethod
    def to_trade_response(internal_trade: Dict[str, Any]) -> TradeResponse:
        """
        Transform internal trade model to user DTO.

        Args:
            internal_trade: Internal trade dict

        Returns:
            TradeResponseDTO with only safe fields
        """
        # Only include safe fields - leave internal fields out
        return TradeResponse(
            id=internal_trade.get("id"),
            portfolio_id=internal_trade.get("portfolio_id"),
            symbol=internal_trade.get("symbol"),
            trade_type=internal_trade.get("trade_type"),
            quantity=internal_trade.get("quantity"),
            entry_price=internal_trade.get("entry_price"),
            entry_date=internal_trade.get("entry_date"),
            exit_price=internal_trade.get("exit_price"),
            exit_date=internal_trade.get("exit_date"),
            status=internal_trade.get("status"),
            notes=internal_trade.get("notes"),
            pnl=internal_trade.get("pnl"),
            pnl_percentage=internal_trade.get("pnl_percentage"),
        )

    @staticmethod
    def to_portfolio_response(internal_portfolio: Dict[str, Any]) -> PortfolioResponse:
        """Transform internal portfolio to user DTO."""
        return PortfolioResponse(
            id=internal_portfolio.get("id"),
            name=internal_portfolio.get("name"),
            description=internal_portfolio.get("description"),
            initial_capital=internal_portfolio.get("initial_capital"),
            current_value=internal_portfolio.get("current_value"),
            total_pnl=internal_portfolio.get("total_pnl"),
            total_pnl_percentage=internal_portfolio.get("total_pnl_percentage"),
            total_trades=internal_portfolio.get("total_trades", 0),
            active_trades=internal_portfolio.get("active_trades", 0),
        )

    @staticmethod
    def from_trade_create_request(dto: TradeCreateRequest) -> Dict[str, Any]:
        """Convert create request DTO to internal format."""
        return {
            "portfolio_id": dto.portfolio_id,
            "symbol": dto.symbol,
            "trade_type": dto.trade_type,
            "quantity": dto.quantity,
            "entry_price": dto.entry_price,
            "entry_date": dto.entry_date,
            "notes": dto.notes,
        }

    @staticmethod
    def from_trade_update_request(dto: TradeUpdateRequest) -> Dict[str, Any]:
        """Convert update request DTO to internal format."""
        return {
            k: v
            for k, v in {
                "exit_price": dto.exit_price,
                "exit_date": dto.exit_date,
                "status": dto.status,
                "notes": dto.notes,
            }.items()
            if v is not None
        }
