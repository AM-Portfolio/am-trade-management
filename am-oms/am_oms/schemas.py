from datetime import datetime, timezone
from decimal import Decimal
from enum import Enum
from uuid import uuid4
from pydantic import BaseModel


def money(v: Decimal | str | int | float) -> str:
    return f"{Decimal(str(v)).quantize(Decimal('0.01')):.2f}"


class WalletKind(str, Enum):
    PAPER = "PAPER"
    LIVE = "LIVE"


class Venue(str, Enum):
    PAPER = "PAPER"
    LIVE = "LIVE"


class InstrumentType(str, Enum):
    EQUITY = "EQUITY"
    OPTION = "OPTION"


class OrderSide(str, Enum):
    BUY = "BUY"
    SELL = "SELL"


class OrderType(str, Enum):
    MARKET = "MARKET"
    LIMIT = "LIMIT"
    STOP = "STOP"
    STOP_LIMIT = "STOP_LIMIT"
    SUPER = "SUPER"
    TRAIL = "TRAIL"


class OrderStatus(str, Enum):
    NEW = "NEW"
    ACCEPTED = "ACCEPTED"
    FILLED = "FILLED"
    REJECTED = "REJECTED"
    CANCELLED = "CANCELLED"
    PARTIALLY_FILLED = "PARTIALLY_FILLED"
    EXPIRED = "EXPIRED"


class OptionType(str, Enum):
    CE = "CE"
    PE = "PE"


class OptionLeg(BaseModel):
    underlying: str
    expiry: str
    strike: str
    optionType: OptionType
    lotSize: int = 1


class WalletCreateRequest(BaseModel):
    kind: WalletKind
    currency: str = "INR"
    seedAmount: str | None = None


class WalletSnapshot(BaseModel):
    available: str
    reserved: str


class WalletResponse(BaseModel):
    walletId: str
    kind: WalletKind
    currency: str
    available: str
    reserved: str
    portfolioId: str
    portfolioUuid: str
    ownerId: str
    createdAt: datetime
    updatedAt: datetime


class WalletListResponse(BaseModel):
    items: list[WalletResponse]


class OrderCreateRequest(BaseModel):
    walletId: str
    venue: Venue
    instrumentType: InstrumentType
    symbol: str
    side: OrderSide
    orderType: OrderType
    quantity: str
    limitPrice: str | None = None
    triggerPrice: str | None = None
    targetPrice: str | None = None
    stopLoss: str | None = None
    trailJump: str | None = None
    entryType: OrderType | None = None
    option: OptionLeg | None = None


class OrderResponse(BaseModel):
    orderId: str
    walletId: str
    ownerId: str
    venue: Venue
    instrumentType: InstrumentType
    symbol: str
    side: OrderSide
    orderType: OrderType
    quantity: str
    status: OrderStatus
    fillPrice: str | None = None
    filledQuantity: str | None = None
    rejectReason: str | None = None
    limitPrice: str | None = None
    triggerPrice: str | None = None
    targetPrice: str | None = None
    stopLoss: str | None = None
    trailJump: str | None = None
    ocoGroupId: str | None = None
    parentOrderId: str | None = None
    option: OptionLeg | None = None
    walletSnapshot: WalletSnapshot
    createdAt: datetime
    updatedAt: datetime
    idempotencyKey: str


class OrderListResponse(BaseModel):
    items: list[OrderResponse]


class PositionResponse(BaseModel):
    walletId: str
    symbol: str
    qty: str


class PositionListResponse(BaseModel):
    items: list[PositionResponse]


def now() -> datetime:
    return datetime.now(timezone.utc)


def uid() -> str:
    return str(uuid4())
