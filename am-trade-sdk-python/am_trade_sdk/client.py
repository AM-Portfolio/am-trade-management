"""
Main SDK client class providing unified access to all AM Trade APIs.

Example:
    >>> from am_trade_sdk import AmTradeSdk
    >>> sdk = AmTradeSdk(
    ...     api_url="http://localhost:8073",
    ...     api_key="your-api-key",
    ...     timeout=30
    ... )
    >>> 
    >>> # Access individual clients
    >>> trades = sdk.trade_client.get_all_trades()
    >>> portfolio = sdk.portfolio_client.get_portfolio_summary()
    >>> analytics = sdk.analytics_client.get_trade_analytics()
    >>> 
    >>> # Create a new trade
    >>> from am_trade_sdk.models import Trade
    >>> trade = Trade(
    ...     symbol="NIFTY50",
    ...     quantity=1,
    ...     entry_price=18500.0
    ... )
    >>> response = sdk.trade_client.create_trade(trade)
"""

import logging
from typing import Optional

from am_trade_sdk.config import SdkConfig
from am_trade_sdk.analytics_client import AnalyticsClient
from am_trade_sdk.filter_client import FilterClient
from am_trade_sdk.journal_client import JournalClient
from am_trade_sdk.portfolio_client import PortfolioClient
from am_trade_sdk.trade_client import TradeClient

logger = logging.getLogger(__name__)


class AmTradeSdk:
    """
    Main AM Trade Management SDK client.
    
    Provides unified access to all trade management APIs including:
    - Trade CRUD operations
    - Portfolio analysis
    - Trade analytics
    - Journal management
    - Filter operations
    
    Attributes:
        trade_client: Client for trade operations
        portfolio_client: Client for portfolio operations
        analytics_client: Client for analytics operations
        journal_client: Client for journal operations
        filter_client: Client for filter operations
    """

    def __init__(
        self,
        api_url: str = "http://localhost:8073",
        api_key: Optional[str] = None,
        timeout: int = 30,
        max_retries: int = 3,
        enable_logging: bool = True
    ):
        """
        Initialize AM Trade SDK.
        
        Args:
            api_url: Base URL of the API server
            api_key: API key for authentication
            timeout: Request timeout in seconds
            max_retries: Maximum number of retry attempts
            enable_logging: Enable debug logging
        """
        self._config = SdkConfig(
            api_url=api_url,
            api_key=api_key,
            timeout=timeout,
            max_retries=max_retries,
            enable_logging=enable_logging
        )
        
        logger.info(f"Initializing AM Trade SDK v{self.version}")
        
        # Initialize API clients
        self.trade_client = TradeClient(self._config)
        self.portfolio_client = PortfolioClient(self._config)
        self.analytics_client = AnalyticsClient(self._config)
        self.journal_client = JournalClient(self._config)
        self.filter_client = FilterClient(self._config)
        
        logger.info("AM Trade SDK initialized successfully")

    @property
    def version(self) -> str:
        """Get SDK version"""
        return "1.0.0"

    @property
    def config(self) -> SdkConfig:
        """Get SDK configuration"""
        return self._config

    def close(self) -> None:
        """
        Close SDK and cleanup resources.
        
        Example:
            >>> sdk = AmTradeSdk()
            >>> try:
            ...     # Use SDK
            ...     trades = sdk.trade_client.get_all_trades()
            ... finally:
            ...     sdk.close()
        """
        logger.info("Shutting down AM Trade SDK")
        self.trade_client.close()
        self.portfolio_client.close()
        self.analytics_client.close()
        self.journal_client.close()
        self.filter_client.close()

    def __enter__(self):
        """Context manager entry"""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit"""
        self.close()

    def __repr__(self) -> str:
        """String representation"""
        return f"AmTradeSdk(api_url={self._config.api_url}, version={self.version})"
