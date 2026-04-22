"""Data models for AM Trade SDK using Pydantic."""

from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class TradeStatus(str, Enum):
    """Trade status enumeration."""

    OPEN = "OPEN"
    CLOSED = "CLOSED"
    PENDING = "PENDING"
    CANCELLED = "CANCELLED"


class TradeType(str, Enum):
    """Trade type enumeration."""

    BUY = "BUY"
    SELL = "SELL"
    SHORT = "SHORT"
    COVER = "COVER"


class Trade(BaseModel):
    """Trade model."""

    id: str = Field(..., description="Trade ID")
    portfolio_id: str = Field(..., description="Portfolio ID")
    symbol: str = Field(..., description="Trade symbol")
    trade_type: TradeType = Field(..., description="Trade type")
    quantity: float = Field(..., description="Trade quantity")
    entry_price: float = Field(..., description="Entry price")
    exit_price: Optional[float] = Field(None, description="Exit price")
    entry_date: datetime = Field(..., description="Entry date")
    exit_date: Optional[datetime] = Field(None, description="Exit date")
    status: TradeStatus = Field(..., description="Trade status")
    notes: Optional[str] = Field(None, description="Trade notes")
    pnl: Optional[float] = Field(None, description="Profit/Loss")
    pnl_percentage: Optional[float] = Field(None, description="PnL percentage")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Update timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeFilter(BaseModel):
    """Trade filter model."""

    portfolio_id: Optional[str] = None
    symbol: Optional[str] = None
    status: Optional[TradeStatus] = None
    trade_type: Optional[TradeType] = None
    min_pnl: Optional[float] = None
    max_pnl: Optional[float] = None
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    page: int = Field(default=0, ge=0)
    page_size: int = Field(default=20, ge=1, le=100)

    class Config:
        """Pydantic config."""

        use_enum_values = True


class Journal(BaseModel):
    """Journal entry model."""

    id: str = Field(..., description="Journal ID")
    trade_id: Optional[str] = Field(None, description="Trade ID")
    portfolio_id: str = Field(..., description="Portfolio ID")
    title: str = Field(..., description="Journal title")
    content: str = Field(..., description="Journal content")
    tags: List[str] = Field(default=[], description="Tags")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Update timestamp")


class Portfolio(BaseModel):
    """Portfolio model."""

    id: str = Field(..., description="Portfolio ID")
    name: str = Field(..., description="Portfolio name")
    description: Optional[str] = Field(None, description="Description")
    currency: str = Field(default="USD", description="Currency")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Update timestamp")


class FavoriteFilter(BaseModel):
    """Favorite filter model."""

    id: str = Field(..., description="Filter ID")
    name: str = Field(..., description="Filter name")
    description: Optional[str] = Field(None, description="Description")
    filter_criteria: Dict[str, Any] = Field(..., description="Filter criteria")
    is_shared: bool = Field(default=False, description="Is shared")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Update timestamp")


class ApiResponse(BaseModel):
    """Generic API response model."""

    success: bool = Field(..., description="Success status")
    data: Any = Field(None, description="Response data")
    message: Optional[str] = Field(None, description="Response message")
    error: Optional[Dict[str, Any]] = Field(None, description="Error details")
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class PagedResponse(BaseModel):
    """Paged response model."""

    content: List[Any] = Field(..., description="Page content")
    page_number: int = Field(..., description="Page number")
    page_size: int = Field(..., description="Page size")
    total_elements: int = Field(..., description="Total elements")
    total_pages: int = Field(..., description="Total pages")
    last: bool = Field(..., description="Is last page")
