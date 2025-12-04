"""User-facing Data Transfer Objects (DTOs) for AM Trade SDK.

These are simplified DTOs exposed to SDK users that only contain necessary fields.
They are separate from backend models and transformed before API calls.
"""

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


class TradeCreateDTO(BaseModel):
    """Data Transfer Object for creating a trade.
    
    This is what SDK users provide. It's transformed into internal models
    before sending to the backend.
    """

    portfolio_id: str = Field(..., description="Portfolio ID")
    symbol: str = Field(..., min_length=1, max_length=20, description="Trade symbol")
    trade_type: TradeType = Field(..., description="Trade type")
    quantity: float = Field(..., gt=0, description="Trade quantity")
    entry_price: float = Field(..., gt=0, description="Entry price")
    entry_date: datetime = Field(..., description="Entry date")
    notes: Optional[str] = Field(None, max_length=500, description="Trade notes")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeUpdateDTO(BaseModel):
    """Data Transfer Object for updating a trade."""

    symbol: Optional[str] = Field(None, min_length=1, max_length=20)
    quantity: Optional[float] = Field(None, gt=0)
    entry_price: Optional[float] = Field(None, gt=0)
    exit_price: Optional[float] = Field(None, gt=0)
    entry_date: Optional[datetime] = None
    exit_date: Optional[datetime] = None
    status: Optional[TradeStatus] = None
    notes: Optional[str] = Field(None, max_length=500)

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeResponseDTO(BaseModel):
    """Data Transfer Object for trade response from backend.
    
    This is what users receive. It hides internal backend fields.
    """

    id: str = Field(..., description="Trade ID")
    portfolio_id: str = Field(..., description="Portfolio ID")
    symbol: str = Field(..., description="Trade symbol")
    trade_type: str = Field(..., description="Trade type")
    quantity: float = Field(..., description="Trade quantity")
    entry_price: float = Field(..., description="Entry price")
    exit_price: Optional[float] = Field(None, description="Exit price")
    entry_date: datetime = Field(..., description="Entry date")
    exit_date: Optional[datetime] = Field(None, description="Exit date")
    status: str = Field(..., description="Trade status")
    notes: Optional[str] = Field(None, description="Trade notes")
    pnl: Optional[float] = Field(None, description="Profit/Loss")
    pnl_percentage: Optional[float] = Field(None, description="PnL percentage")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PortfolioCreateDTO(BaseModel):
    """Data Transfer Object for creating a portfolio."""

    name: str = Field(..., min_length=1, max_length=100, description="Portfolio name")
    description: Optional[str] = Field(None, max_length=500, description="Portfolio description")
    initial_capital: float = Field(..., gt=0, description="Initial capital")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PortfolioResponseDTO(BaseModel):
    """Data Transfer Object for portfolio response."""

    id: str = Field(..., description="Portfolio ID")
    name: str = Field(..., description="Portfolio name")
    description: Optional[str] = Field(None, description="Portfolio description")
    initial_capital: float = Field(..., description="Initial capital")
    current_value: float = Field(..., description="Current portfolio value")
    total_pnl: float = Field(..., description="Total profit/loss")
    total_pnl_percentage: float = Field(..., description="Total PnL percentage")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class TradeFilterDTO(BaseModel):
    """Data Transfer Object for trade filter."""

    portfolio_id: Optional[str] = Field(None, description="Filter by portfolio")
    symbol: Optional[str] = Field(None, description="Filter by symbol")
    status: Optional[TradeStatus] = Field(None, description="Filter by status")
    trade_type: Optional[TradeType] = Field(None, description="Filter by trade type")
    min_pnl: Optional[float] = Field(None, description="Minimum PnL")
    max_pnl: Optional[float] = Field(None, description="Maximum PnL")
    start_date: Optional[datetime] = Field(None, description="Start date")
    end_date: Optional[datetime] = Field(None, description="End date")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class JournalEntryCreateDTO(BaseModel):
    """Data Transfer Object for creating a journal entry."""

    trade_id: Optional[str] = Field(None, description="Associated trade ID")
    title: str = Field(..., min_length=1, max_length=200, description="Entry title")
    content: str = Field(..., description="Entry content")
    tags: Optional[List[str]] = Field(None, description="Entry tags")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class JournalEntryResponseDTO(BaseModel):
    """Data Transfer Object for journal entry response."""

    id: str = Field(..., description="Entry ID")
    trade_id: Optional[str] = Field(None, description="Associated trade ID")
    title: str = Field(..., description="Entry title")
    content: str = Field(..., description="Entry content")
    tags: Optional[List[str]] = Field(None, description="Entry tags")
    created_at: datetime = Field(..., description="Creation timestamp")
    updated_at: datetime = Field(..., description="Update timestamp")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class FilterCreateDTO(BaseModel):
    """Data Transfer Object for creating a favorite filter."""

    name: str = Field(..., min_length=1, max_length=100, description="Filter name")
    criteria: Dict[str, Any] = Field(..., description="Filter criteria")
    description: Optional[str] = Field(None, max_length=500, description="Filter description")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class FilterResponseDTO(BaseModel):
    """Data Transfer Object for filter response."""

    id: str = Field(..., description="Filter ID")
    name: str = Field(..., description="Filter name")
    criteria: Dict[str, Any] = Field(..., description="Filter criteria")
    description: Optional[str] = Field(None, description="Filter description")
    is_shared: bool = Field(..., description="Is filter shared")
    created_by: str = Field(..., description="Created by user")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class PagedResponseDTO(BaseModel):
    """Data Transfer Object for paginated responses."""

    content: List[Dict[str, Any]] = Field(..., description="Response content")
    page_number: int = Field(..., description="Current page number")
    page_size: int = Field(..., description="Page size")
    total_elements: int = Field(..., description="Total number of elements")
    total_pages: int = Field(..., description="Total number of pages")
    is_last: bool = Field(..., description="Is this the last page")

    class Config:
        """Pydantic config."""

        use_enum_values = True


class ApiResponseDTO(BaseModel):
    """Data Transfer Object for API responses."""

    success: bool = Field(..., description="Response success status")
    data: Optional[Dict[str, Any]] = Field(None, description="Response data")
    message: Optional[str] = Field(None, description="Response message")
    status_code: int = Field(..., description="HTTP status code")

    class Config:
        """Pydantic config."""

        use_enum_values = True
