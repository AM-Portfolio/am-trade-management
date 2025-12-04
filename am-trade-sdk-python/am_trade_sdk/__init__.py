"""AM Trade Management SDK for Python."""

__version__ = "1.0.0"

from am_trade_sdk.analytics_client import AnalyticsClient
from am_trade_sdk.base_client import BaseApiClient
from am_trade_sdk.client import AmTradeSdk
from am_trade_sdk.config import SdkConfig, ConfigBuilder
from am_trade_sdk.dto import (
    DTOTransformer,
    ErrorResponse,
    FavoriteFilterCreateRequest,
    FavoriteFilterResponse,
    JournalEntryCreateRequest,
    JournalEntryResponse,
    PagedResponse,
    PortfolioCreateRequest,
    PortfolioResponse,
    PortfolioUpdateRequest,
    SuccessResponse,
    TradeCreateRequest,
    TradeFilterRequest,
    TradeResponse,
    TradeStatusEnum,
    TradeTypeEnum,
    TradeUpdateRequest,
)
from am_trade_sdk.exceptions import (
    AmTradeSdkException,
    ApiException,
    AuthenticationException,
    ConfigurationException,
    ConflictException,
    NetworkException,
    RateLimitException,
    ResourceNotFoundException,
    TimeoutException,
    ValidationException,
)
from am_trade_sdk.filter_client import FilterClient
from am_trade_sdk.journal_client import JournalClient
from am_trade_sdk.models import (
    ApiResponse,
    FavoriteFilter,
    Journal,
    PagedResponse,
    Portfolio,
    Trade,
    TradeFilter,
    TradeStatus,
    TradeType,
)
from am_trade_sdk.portfolio_client import PortfolioClient
from am_trade_sdk.trade_client import TradeClient
from am_trade_sdk.version import (
    SdkVersion,
    VersionMetadata,
    SdkIdentifier,
)
from am_trade_sdk.wrapper import (
    SdkRequest,
    SdkResponse,
    SdkRequestMetadata,
    SdkResponseMetadata,
    VersionedDataTransformer,
)
from am_trade_sdk.tier import (
    UserTier,
    TierConfig,
    TokenAnalyzer,
    TierValidator,
    FieldFilter,
    TierContext,
)

__all__ = [
    # Main SDK
    "AmTradeSdk",
    # Configuration
    "SdkConfig",
    "ConfigBuilder",
    # Clients
    "BaseApiClient",
    "TradeClient",
    "PortfolioClient",
    "AnalyticsClient",
    "JournalClient",
    "FilterClient",
    # User-Facing DTOs (USE THESE!)
    "TradeStatusEnum",
    "TradeTypeEnum",
    "TradeCreateRequest",
    "TradeUpdateRequest",
    "TradeResponse",
    "TradeFilterRequest",
    "PortfolioCreateRequest",
    "PortfolioUpdateRequest",
    "PortfolioResponse",
    "JournalEntryCreateRequest",
    "JournalEntryResponse",
    "FavoriteFilterCreateRequest",
    "FavoriteFilterResponse",
    "PagedResponse",
    "ErrorResponse",
    "SuccessResponse",
    "DTOTransformer",
    # Internal Models (DEPRECATED - Use DTOs instead)
    "Trade",
    "TradeFilter",
    "TradeStatus",
    "TradeType",
    "Journal",
    "Portfolio",
    "FavoriteFilter",
    "ApiResponse",
    "PagedResponse",
    # Versioning & Metadata
    "SdkVersion",
    "VersionMetadata",
    "SdkIdentifier",
    # Request/Response Wrappers
    "SdkRequest",
    "SdkResponse",
    "SdkRequestMetadata",
    "SdkResponseMetadata",
    "VersionedDataTransformer",
    # Tier-Based Access Control
    "UserTier",
    "TierConfig",
    "TokenAnalyzer",
    "TierValidator",
    "FieldFilter",
    "TierContext",
    # Exceptions
    "AmTradeSdkException",
    "ApiException",
    "ValidationException",
    "NetworkException",
    "TimeoutException",
    "ConfigurationException",
    "AuthenticationException",
    "RateLimitException",
    "ResourceNotFoundException",
    "ConflictException",
]
