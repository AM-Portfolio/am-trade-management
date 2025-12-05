"""AM Trade Management SDK for Python."""

__version__ = "1.0.0"

from sdk.client.analytics_client import AnalyticsClient
from sdk.http.base_client import BaseApiClient
from sdk.client.client import AmTradeSdk
from sdk.config import SdkConfig, ConfigBuilder
from sdk.dto import (
    DTOTransformer,
    ErrorResponse,
    FavoriteFilterCreateRequest,
    FavoriteFilterResponse,
    JournalEntryCreateRequest,
    JournalEntryResponse,
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
    ApiResponse,
    UserTier,
    TierConfig,
    TokenAnalyzer,
    TierValidator,
    FieldFilter,
    TierContext,
)
from sdk.models import (
    Trade,
    PagedResponse,
)
from sdk.exception import (
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
from sdk.client.filter_client import FilterClient
from sdk.client.journal_client import JournalClient
from sdk.client.portfolio_client import PortfolioClient
from sdk.client.trade_client import TradeClient
from sdk.version import (
    SdkVersion,
    VersionMetadata,
    SdkIdentifier,
)
from sdk.client.wrapper import (
    SdkRequest,
    SdkResponse,
    SdkRequestMetadata,
    SdkResponseMetadata,
    VersionedDataTransformer,
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
    "ErrorResponse",
    "SuccessResponse",
    "DTOTransformer",
    # Models
    "Trade",
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
