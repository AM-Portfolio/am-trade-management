"""User tier management and field access control."""

from enum import Enum
from typing import Dict, List, Set
import jwt
from datetime import datetime
import logging

logger = logging.getLogger(__name__)


class UserTier(str, Enum):
    """User tier enumeration."""

    FREE = "FREE"
    PREMIUM = "PREMIUM"
    ENTERPRISE = "ENTERPRISE"


class TierConfig:
    """Configuration for tier-based field access."""

    # Fields accessible by each tier
    TIER_FIELDS = {
        UserTier.FREE: {
            "trade": {
                "id", "portfolio_id", "symbol", "trade_type", "quantity",
                "entry_price", "entry_date", "status", "pnl", "pnl_percentage"
            },
            "portfolio": {
                "id", "name", "initial_capital", "current_value",
                "total_pnl", "total_pnl_percentage", "total_trades"
            },
            "journal": {
                "id", "trade_id", "title", "created_at", "updated_at"
            },
            "filter": {
                "id", "name", "description", "is_shared", "created_at"
            }
        },
        UserTier.PREMIUM: {
            "trade": {
                "id", "portfolio_id", "symbol", "trade_type", "quantity",
                "entry_price", "entry_date", "exit_price", "exit_date",
                "status", "notes", "pnl", "pnl_percentage",
                # Premium fields
                "transaction_cost", "fee_paid", "tax_impact"
            },
            "portfolio": {
                "id", "name", "description", "initial_capital", "current_value",
                "total_pnl", "total_pnl_percentage", "total_trades", "active_trades",
                # Premium fields
                "portfolio_roi", "sharpe_ratio", "max_drawdown"
            },
            "journal": {
                "id", "trade_id", "title", "content", "tags",
                "created_at", "updated_at",
                # Premium fields
                "created_by", "last_modified_by"
            },
            "filter": {
                "id", "name", "criteria", "description", "is_shared",
                "created_by", "created_at",
                # Premium fields
                "shared_with", "usage_count"
            }
        },
        UserTier.ENTERPRISE: {
            "trade": {
                "id", "portfolio_id", "symbol", "trade_type", "quantity",
                "entry_price", "entry_date", "exit_price", "exit_date",
                "status", "notes", "pnl", "pnl_percentage",
                # All premium fields
                "transaction_cost", "fee_paid", "tax_impact",
                # Enterprise fields
                "margin_used", "leverage_ratio", "internal_id", "metadata",
                "risk_score", "volatility", "correlation_matrix"
            },
            "portfolio": {
                "id", "name", "description", "initial_capital", "current_value",
                "total_pnl", "total_pnl_percentage", "total_trades", "active_trades",
                # All premium fields
                "portfolio_roi", "sharpe_ratio", "max_drawdown",
                # Enterprise fields
                "internal_id", "metadata", "accounting_basis",
                "tax_lot_method", "performance_attribution", "risk_metrics"
            },
            "journal": {
                "id", "trade_id", "title", "content", "tags",
                "created_at", "updated_at", "created_by", "last_modified_by",
                # Enterprise fields
                "internal_notes", "audit_trail", "attachments_count"
            },
            "filter": {
                "id", "name", "criteria", "description", "is_shared",
                "created_by", "created_at", "shared_with", "usage_count",
                # Enterprise fields
                "internal_id", "filter_rules", "optimization_score"
            }
        }
    }

    # Rate limits per tier
    RATE_LIMITS = {
        UserTier.FREE: {
            "requests_per_minute": 10,
            "max_batch_size": 10,
            "max_page_size": 20
        },
        UserTier.PREMIUM: {
            "requests_per_minute": 100,
            "max_batch_size": 100,
            "max_page_size": 500
        },
        UserTier.ENTERPRISE: {
            "requests_per_minute": 1000,
            "max_batch_size": 1000,
            "max_page_size": 5000
        }
    }

    # Features per tier
    TIER_FEATURES = {
        UserTier.FREE: {
            "basic_crud": True,
            "filtering": True,
            "basic_analytics": False,
            "advanced_analytics": False,
            "batch_operations": False,
            "export_import": False,
            "api_access": False
        },
        UserTier.PREMIUM: {
            "basic_crud": True,
            "filtering": True,
            "basic_analytics": True,
            "advanced_analytics": False,
            "batch_operations": True,
            "export_import": True,
            "api_access": False
        },
        UserTier.ENTERPRISE: {
            "basic_crud": True,
            "filtering": True,
            "basic_analytics": True,
            "advanced_analytics": True,
            "batch_operations": True,
            "export_import": True,
            "api_access": True
        }
    }


class TokenAnalyzer:
    """Extract tier information from JWT token."""

    @staticmethod
    def extract_tier_from_token(token: str, secret_key: str = None) -> UserTier:
        """
        Extract user tier from JWT token.

        Args:
            token: JWT token from config
            secret_key: Secret key for verification (optional)

        Returns:
            UserTier enum value
        """
        try:
            # Try to decode token
            if secret_key:
                payload = jwt.decode(token, secret_key, algorithms=["HS256", "RS256"])
            else:
                # Decode without verification to get payload
                payload = jwt.decode(token, options={"verify_signature": False})

            tier_str = payload.get("tier", "FREE").upper()

            # Validate tier
            if tier_str in UserTier.__members__:
                tier = UserTier[tier_str]
                logger.info(f"User tier extracted: {tier.value}")
                return tier
            else:
                logger.warning(f"Unknown tier in token: {tier_str}, defaulting to FREE")
                return UserTier.FREE

        except Exception as e:
            logger.warning(f"Failed to extract tier from token: {str(e)}, defaulting to FREE")
            return UserTier.FREE

    @staticmethod
    def extract_user_id_from_token(token: str) -> str:
        """Extract user ID from JWT token."""
        try:
            payload = jwt.decode(token, options={"verify_signature": False})
            user_id = payload.get("user_id") or payload.get("sub") or "unknown"
            return str(user_id)
        except Exception:
            return "unknown"

    @staticmethod
    def is_token_expired(token: str) -> bool:
        """Check if JWT token is expired."""
        try:
            payload = jwt.decode(token, options={"verify_signature": False})
            exp = payload.get("exp")

            if exp:
                return datetime.fromtimestamp(exp) < datetime.utcnow()
            return False

        except Exception:
            return True


class TierValidator:
    """Validate user tier and access permissions."""

    def __init__(self, user_tier: UserTier):
        """
        Initialize validator.

        Args:
            user_tier: User's tier level
        """
        self.user_tier = user_tier
        self.logger = logging.getLogger(self.__class__.__name__)

    def has_feature(self, feature: str) -> bool:
        """
        Check if user tier has access to feature.

        Args:
            feature: Feature name

        Returns:
            True if feature is available for tier
        """
        return TierConfig.TIER_FEATURES[self.user_tier].get(feature, False)

    def get_accessible_fields(self, resource_type: str) -> Set[str]:
        """
        Get fields accessible by user tier for resource.

        Args:
            resource_type: Resource type (trade, portfolio, etc.)

        Returns:
            Set of accessible field names
        """
        return TierConfig.TIER_FIELDS.get(self.user_tier, {}).get(resource_type, set())

    def get_rate_limit(self, limit_type: str) -> int:
        """
        Get rate limit for user tier.

        Args:
            limit_type: Type of limit (requests_per_minute, max_batch_size, etc.)

        Returns:
            Rate limit value
        """
        return TierConfig.RATE_LIMITS[self.user_tier].get(limit_type, 0)

    def validate_batch_size(self, size: int) -> bool:
        """Validate batch operation size."""
        max_size = self.get_rate_limit("max_batch_size")
        if size > max_size:
            self.logger.warning(
                f"Batch size {size} exceeds limit {max_size} for tier {self.user_tier.value}"
            )
            return False
        return True

    def validate_page_size(self, size: int) -> bool:
        """Validate page size for pagination."""
        max_size = self.get_rate_limit("max_page_size")
        if size > max_size:
            self.logger.warning(
                f"Page size {size} exceeds limit {max_size} for tier {self.user_tier.value}"
            )
            return False
        return True


class FieldFilter:
    """Filter response fields based on user tier."""

    def __init__(self, user_tier: UserTier):
        """
        Initialize field filter.

        Args:
            user_tier: User's tier level
        """
        self.user_tier = user_tier
        self.tier_validator = TierValidator(user_tier)

    def filter_response(self, data: Dict, resource_type: str) -> Dict:
        """
        Filter response data to include only accessible fields.

        Args:
            data: Response data dictionary
            resource_type: Type of resource (trade, portfolio, etc.)

        Returns:
            Filtered dictionary with only accessible fields
        """
        accessible_fields = self.tier_validator.get_accessible_fields(resource_type)

        if not accessible_fields:
            return {}

        # Filter fields
        filtered = {k: v for k, v in data.items() if k in accessible_fields}

        logger.info(
            f"Filtered {resource_type} response for {self.user_tier.value} user: "
            f"{len(data)} fields → {len(filtered)} fields"
        )

        return filtered

    def filter_list_response(self, items: List[Dict], resource_type: str) -> List[Dict]:
        """
        Filter list of response items.

        Args:
            items: List of response dictionaries
            resource_type: Type of resource

        Returns:
            List of filtered dictionaries
        """
        return [self.filter_response(item, resource_type) for item in items]

    def add_tier_info_to_response(self, response: Dict) -> Dict:
        """
        Add tier information to response metadata.

        Args:
            response: Response dictionary

        Returns:
            Response with tier info added
        """
        if "_meta" not in response:
            response["_meta"] = {}

        response["_meta"]["user_tier"] = self.user_tier.value
        response["_meta"]["accessible_features"] = list(
            TierConfig.TIER_FEATURES[self.user_tier].keys()
        )

        return response


class TierContext:
    """Context for tier-based operations."""

    def __init__(self, token: str, secret_key: str = None):
        """
        Initialize tier context from token.

        Args:
            token: JWT token
            secret_key: Secret key for verification
        """
        self.token = token
        self.user_tier = TokenAnalyzer.extract_tier_from_token(token, secret_key)
        self.user_id = TokenAnalyzer.extract_user_id_from_token(token)
        self.is_expired = TokenAnalyzer.is_token_expired(token)

        self.tier_validator = TierValidator(self.user_tier)
        self.field_filter = FieldFilter(self.user_tier)

        logger.info(
            f"Tier context initialized: user_id={self.user_id}, "
            f"tier={self.user_tier.value}, expired={self.is_expired}"
        )

    def get_accessible_fields(self, resource_type: str) -> Set[str]:
        """Get accessible fields for resource."""
        return self.tier_validator.get_accessible_fields(resource_type)

    def filter_response(self, data: Dict, resource_type: str) -> Dict:
        """Filter response based on tier."""
        return self.field_filter.filter_response(data, resource_type)

    def filter_list(self, items: List[Dict], resource_type: str) -> List[Dict]:
        """Filter list response based on tier."""
        return self.field_filter.filter_list_response(items, resource_type)

    def is_feature_available(self, feature: str) -> bool:
        """Check if feature is available for tier."""
        return self.tier_validator.has_feature(feature)

    def validate_operation(self, operation: str) -> bool:
        """Validate if operation is allowed for tier."""
        feature_map = {
            "batch_create": "batch_operations",
            "batch_delete": "batch_operations",
            "export": "export_import",
            "import": "export_import",
            "advanced_filter": "advanced_analytics"
        }
        required_feature = feature_map.get(operation)
        if required_feature:
            return self.is_feature_available(required_feature)
        return True

    def get_rate_limit(self, limit_type: str) -> int:
        """Get rate limit for operation."""
        return self.tier_validator.get_rate_limit(limit_type)
