"""
Configuration management for AM Trade SDK.

Provides configuration classes for SDK initialization with support for
environment variables, builder pattern, and validation.
"""

import os
from typing import Optional

from pydantic import BaseModel, Field, validator


class SdkConfig(BaseModel):
    """
    SDK Configuration with environment variable support.
    
    Supports loading configuration from environment variables with
    prefix AM_TRADE_SDK_ (e.g., AM_TRADE_SDK_API_URL).
    
    Example:
        >>> config = SdkConfig(api_url="http://localhost:8073")
        >>> config.api_url
        'http://localhost:8073'
    """
    
    api_url: str = Field(
        default="http://localhost:8073",
        description="Base URL for the Trade Management API"
    )
    api_key: Optional[str] = Field(
        default=None,
        description="API key for authentication (optional)"
    )
    timeout: int = Field(
        default=30,
        ge=1,
        le=300,
        description="HTTP request timeout in seconds"
    )
    max_retries: int = Field(
        default=3,
        ge=0,
        le=10,
        description="Maximum number of retry attempts for failed requests"
    )
    connection_timeout: int = Field(
        default=10,
        ge=1,
        le=60,
        description="Connection timeout in seconds"
    )
    verify_ssl: bool = Field(
        default=True,
        description="Enable SSL certificate verification"
    )
    enable_logging: bool = Field(
        default=True,
        description="Enable request/response logging"
    )
    
    class Config:
        """Pydantic configuration."""
        validate_assignment = True
        use_enum_values = True
        case_sensitive = False
    
    @validator('api_url')
    def validate_api_url(cls, v):
        """Validate API URL format."""
        if not v:
            raise ValueError("api_url cannot be empty")
        if not v.startswith(('http://', 'https://')):
            raise ValueError("api_url must start with http:// or https://")
        return v.rstrip('/')
    
    @classmethod
    def from_env(cls) -> 'SdkConfig':
        """
        Create configuration from environment variables.
        
        Looks for:
        - AM_TRADE_SDK_API_URL
        - AM_TRADE_SDK_API_KEY
        - AM_TRADE_SDK_TIMEOUT
        - AM_TRADE_SDK_MAX_RETRIES
        - AM_TRADE_SDK_CONNECTION_TIMEOUT
        - AM_TRADE_SDK_VERIFY_SSL (true/false)
        - AM_TRADE_SDK_ENABLE_LOGGING (true/false)
        
        Returns:
            SdkConfig: Configuration loaded from environment
        """
        return cls(
            api_url=os.getenv(
                'AM_TRADE_SDK_API_URL',
                'http://localhost:8073'
            ),
            api_key=os.getenv('AM_TRADE_SDK_API_KEY'),
            timeout=int(os.getenv('AM_TRADE_SDK_TIMEOUT', 30)),
            max_retries=int(os.getenv('AM_TRADE_SDK_MAX_RETRIES', 3)),
            connection_timeout=int(
                os.getenv('AM_TRADE_SDK_CONNECTION_TIMEOUT', 10)
            ),
            verify_ssl=os.getenv(
                'AM_TRADE_SDK_VERIFY_SSL', 'true'
            ).lower() == 'true',
            enable_logging=os.getenv(
                'AM_TRADE_SDK_ENABLE_LOGGING', 'true'
            ).lower() == 'true',
        )
    
    @classmethod
    def builder(cls) -> 'ConfigBuilder':
        """
        Create a builder for fluent configuration.
        
        Returns:
            ConfigBuilder: Builder instance for fluent API
            
        Example:
            >>> config = SdkConfig.builder() \\
            ...     .api_url("http://api.example.com") \\
            ...     .api_key("secret-key") \\
            ...     .timeout(60) \\
            ...     .build()
        """
        return ConfigBuilder()
    
    def to_dict(self) -> dict:
        """
        Convert configuration to dictionary.
        
        Returns:
            dict: Configuration as dictionary
        """
        return self.dict()
    
    def __repr__(self) -> str:
        """String representation with masked API key."""
        return (
            f"SdkConfig("
            f"api_url={self.api_url!r}, "
            f"api_key={'***' if self.api_key else None}, "
            f"timeout={self.timeout}, "
            f"max_retries={self.max_retries})"
        )


class ConfigBuilder:
    """
    Builder for fluent SdkConfig creation.
    
    Example:
        >>> config = SdkConfig.builder() \\
        ...     .api_url("http://api.example.com") \\
        ...     .api_key("my-key") \\
        ...     .timeout(60) \\
        ...     .max_retries(5) \\
        ...     .build()
    """
    
    def __init__(self):
        """Initialize builder with defaults."""
        self._api_url = "http://localhost:8073"
        self._api_key: Optional[str] = None
        self._timeout = 30
        self._max_retries = 3
        self._connection_timeout = 10
        self._verify_ssl = True
        self._enable_logging = True
    
    def api_url(self, url: str) -> 'ConfigBuilder':
        """Set API URL."""
        self._api_url = url
        return self
    
    def api_key(self, key: str) -> 'ConfigBuilder':
        """Set API key."""
        self._api_key = key
        return self
    
    def timeout(self, seconds: int) -> 'ConfigBuilder':
        """Set request timeout."""
        self._timeout = seconds
        return self
    
    def max_retries(self, retries: int) -> 'ConfigBuilder':
        """Set maximum retry attempts."""
        self._max_retries = retries
        return self
    
    def connection_timeout(self, seconds: int) -> 'ConfigBuilder':
        """Set connection timeout."""
        self._connection_timeout = seconds
        return self
    
    def verify_ssl(self, verify: bool) -> 'ConfigBuilder':
        """Set SSL verification."""
        self._verify_ssl = verify
        return self
    
    def enable_logging(self, enable: bool) -> 'ConfigBuilder':
        """Set logging enabled."""
        self._enable_logging = enable
        return self
    
    def build(self) -> SdkConfig:
        """
        Build SdkConfig instance.
        
        Returns:
            SdkConfig: Configured instance
            
        Raises:
            ValueError: If configuration is invalid
        """
        return SdkConfig(
            api_url=self._api_url,
            api_key=self._api_key,
            timeout=self._timeout,
            max_retries=self._max_retries,
            connection_timeout=self._connection_timeout,
            verify_ssl=self._verify_ssl,
            enable_logging=self._enable_logging,
        )
