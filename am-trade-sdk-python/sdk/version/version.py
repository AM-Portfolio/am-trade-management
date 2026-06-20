"""SDK version management and metadata."""

from enum import Enum
from typing import Dict, Any


class SdkVersion(Enum):
    """SDK version enumeration."""

    V1_0_0 = "1.0.0"
    V1_1_0 = "1.1.0"
    V2_0_0 = "2.0.0"


class VersionMetadata:
    """SDK version metadata and capabilities."""

    CURRENT_VERSION = SdkVersion.V1_0_0
    
    # Version-specific features and API changes
    VERSION_FEATURES = {
        "1.0.0": {
            "released": "2025-12-05",
            "features": [
                "Basic CRUD operations",
                "Trade management",
                "Portfolio tracking",
                "Analytics support",
                "Journal entries",
                "Filter management"
            ],
            "breaking_changes": [],
            "deprecated_endpoints": [],
            "max_retries": 3,
            "timeout": 30,
            "pagination_max_size": 100
        },
        "1.1.0": {
            "released": "2026-01-15",
            "features": [
                "Advanced filtering",
                "Batch operations",
                "Export/import",
                "Performance metrics"
            ],
            "breaking_changes": [],
            "deprecated_endpoints": [],
            "max_retries": 5,
            "timeout": 60,
            "pagination_max_size": 500
        },
        "2.0.0": {
            "released": "2026-03-01",
            "features": [
                "WebSocket support",
                "Real-time updates",
                "Advanced AI analytics"
            ],
            "breaking_changes": [
                "Removed legacy endpoints",
                "Changed response format"
            ],
            "deprecated_endpoints": [
                "/api/v1/old-endpoint"
            ],
            "max_retries": 5,
            "timeout": 120,
            "pagination_max_size": 1000
        }
    }

    @classmethod
    def get_current_version(cls) -> str:
        """Get current SDK version string."""
        return cls.CURRENT_VERSION.value

    @classmethod
    def get_version_info(cls, version: str = None) -> Dict[str, Any]:
        """
        Get version information.
        
        Args:
            version: Version string (uses current if None)
            
        Returns:
            Dictionary with version metadata
        """
        if version is None:
            version = cls.get_current_version()
        
        return cls.VERSION_FEATURES.get(version, {})

    @classmethod
    def get_version_capabilities(cls, version: str = None) -> Dict[str, Any]:
        """
        Get version-specific capabilities.
        
        Args:
            version: Version string (uses current if None)
            
        Returns:
            Dictionary with version capabilities
        """
        info = cls.get_version_info(version)
        return {
            "version": version or cls.get_current_version(),
            "max_retries": info.get("max_retries", 3),
            "timeout": info.get("timeout", 30),
            "pagination_max_size": info.get("pagination_max_size", 100),
            "features": info.get("features", []),
            "breaking_changes": info.get("breaking_changes", []),
            "deprecated_endpoints": info.get("deprecated_endpoints", [])
        }

    @classmethod
    def is_compatible(cls, version: str) -> bool:
        """Check if version is supported."""
        return version in cls.VERSION_FEATURES

    @classmethod
    def get_deprecation_warnings(cls, version: str = None) -> list:
        """Get deprecation warnings for version."""
        if version is None:
            version = cls.get_current_version()
        
        info = cls.get_version_info(version)
        return info.get("deprecated_endpoints", [])


class SdkIdentifier:
    """SDK identifier for backend tracking."""

    SDK_NAME = "am-trade-sdk-python"
    SDK_TYPE = "SDK"
    
    @classmethod
    def get_sdk_header(cls, version: str = None) -> Dict[str, str]:
        """
        Get SDK identification headers for API requests.
        
        Args:
            version: SDK version
            
        Returns:
            Dictionary of headers to add to requests
        """
        if version is None:
            version = VersionMetadata.get_current_version()
        
        return {
            "X-SDK-Name": cls.SDK_NAME,
            "X-SDK-Version": version,
            "X-SDK-Type": cls.SDK_TYPE,
            "X-SDK-Language": "python"
        }

    @classmethod
    def get_sdk_metadata(cls, version: str = None) -> Dict[str, str]:
        """
        Get complete SDK metadata.
        
        Args:
            version: SDK version
            
        Returns:
            Dictionary with SDK metadata
        """
        if version is None:
            version = VersionMetadata.get_current_version()
        
        return {
            "sdk_name": cls.SDK_NAME,
            "sdk_version": version,
            "sdk_type": cls.SDK_TYPE,
            "language": "python",
            "timestamp": None  # Will be set at request time
        }
