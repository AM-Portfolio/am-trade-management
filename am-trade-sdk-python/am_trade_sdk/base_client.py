"""Base API client for AM Trade SDK."""

import logging
from typing import Any, Dict, Optional

import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry

from am_trade_sdk.config import SdkConfig
from am_trade_sdk.exceptions import (
    ApiException,
    NetworkException,
    TimeoutException,
    ValidationException,
)
from am_trade_sdk.models import ApiResponse
from am_trade_sdk.version import SdkIdentifier, VersionMetadata
from am_trade_sdk.wrapper import SdkRequest, SdkResponse
from am_trade_sdk.tier import TierContext

logger = logging.getLogger(__name__)


class BaseApiClient:
    """Base class for API clients."""

    def __init__(self, config: SdkConfig):
        """
        Initialize base API client.

        Args:
            config: SDK configuration
        """
        self.config = config
        self.session = self._create_session()
        self.logger = logging.getLogger(self.__class__.__name__)
        self.sdk_version = VersionMetadata.get_current_version()
        
        # Initialize tier context from config API key (token)
        self.tier_context = None
        if config.api_key:
            try:
                self.tier_context = TierContext(config.api_key)
                self.logger.info(
                    f"Tier context initialized: {self.tier_context.user_tier.value}",
                    extra={"user_tier": self.tier_context.user_tier.value}
                )
            except Exception as e:
                self.logger.warning(f"Failed to initialize tier context: {str(e)}")
        
        self.logger.info(
            f"AM Trade SDK initialized - Version: {self.sdk_version}",
            extra={"sdk_version": self.sdk_version}
        )

    def _create_session(self) -> requests.Session:
        """
        Create requests session with retry strategy.

        Returns:
            requests.Session: Configured session
        """
        session = requests.Session()

        retry_strategy = Retry(
            total=self.config.max_retries,
            backoff_factor=1,
            status_forcelist=[429, 500, 502, 503, 504],
            allowed_methods=["HEAD", "GET", "OPTIONS", "POST", "PUT", "DELETE"]
        )

        adapter = HTTPAdapter(max_retries=retry_strategy)
        session.mount("http://", adapter)
        session.mount("https://", adapter)

        if self.config.api_key:
            session.headers.update({
                "Authorization": f"Bearer {self.config.api_key}"
            })

        # Add SDK identification headers
        session.headers.update({
            "Content-Type": "application/json",
            "User-Agent": f"am-trade-sdk-python/{VersionMetadata.get_current_version()}",
            **SdkIdentifier.get_sdk_header(VersionMetadata.get_current_version())
        })

        return session

    def request(
        self,
        method: str,
        endpoint: str,
        data: Optional[Dict[str, Any]] = None,
        params: Optional[Dict[str, Any]] = None,
        operation: str = "READ",
        resource: str = "UNKNOWN",
        **kwargs
    ) -> Dict[str, Any]:
        """
        Make HTTP request to API with SDK metadata.

        Args:
            method: HTTP method
            endpoint: API endpoint
            data: Request body
            params: Query parameters
            operation: Operation type (CREATE, READ, UPDATE, DELETE)
            resource: Resource type (TRADE, PORTFOLIO, etc.)
            **kwargs: Additional arguments

        Returns:
            dict: Response data

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
            NetworkException: If network error occurs
            TimeoutException: If request times out
        """
        # Create SDK request wrapper
        sdk_request = SdkRequest(
            operation=operation,
            resource=resource,
            payload=data or {}
        )

        url = f"{self.config.api_url}{endpoint}"

        try:
            self.logger.debug(
                f"Making {method} request to {endpoint}",
                extra={
                    "url": url,
                    "sdk_version": self.sdk_version,
                    "request_id": sdk_request.metadata.request_id
                }
            )

            # Merge SDK headers with request headers
            request_headers = sdk_request.to_headers()
            request_headers.update(kwargs.pop("headers", {}))

            response = self.session.request(
                method=method,
                url=url,
                json=sdk_request.to_api_payload(),
                params=params,
                timeout=(
                    self.config.connection_timeout,
                    self.config.timeout
                ),
                verify=self.config.verify_ssl,
                headers=request_headers,
                **kwargs
            )

            self.logger.debug(
                f"Response status: {response.status_code}",
                extra={
                    "status": response.status_code,
                    "request_id": sdk_request.metadata.request_id
                }
            )

            if response.status_code >= 400:
                self._handle_error_response(response, sdk_request.metadata.request_id)

            response_data = response.json() if response.content else {}
            return response_data

        except requests.exceptions.Timeout as e:
            self.logger.error(
                f"Request timeout: {str(e)}",
                exc_info=True,
                extra={"request_id": sdk_request.metadata.request_id}
            )
            raise TimeoutException(
                f"Request timed out after {self.config.timeout}s",
                timeout_seconds=self.config.timeout
            ) from e

        except requests.exceptions.ConnectionError as e:
            self.logger.error(
                f"Connection error: {str(e)}",
                exc_info=True,
                extra={"request_id": sdk_request.metadata.request_id}
            )
            raise NetworkException(
                f"Failed to connect to {self.config.api_url}"
            ) from e

        except requests.exceptions.RequestException as e:
            self.logger.error(
                f"Request error: {str(e)}",
                exc_info=True,
                extra={"request_id": sdk_request.metadata.request_id}
            )
            raise NetworkException(
                f"Request failed: {str(e)}"
            ) from e

    def _handle_error_response(self, response: requests.Response, request_id: str = None) -> None:
        """
        Handle error response.

        Args:
            response: Response object
            request_id: Request ID for tracking

        Raises:
            ApiException: Always
        """
        status = response.status_code
        try:
            error_data = response.json()
            message = error_data.get('message', response.text)
            error_code = error_data.get('error_code')
        except Exception:
            message = response.text or f"HTTP {status}"
            error_code = None

        self.logger.warning(
            f"API error: {status} - {message}",
            extra={
                "status": status,
                "message": message,
                "request_id": request_id
            }
        )

        raise ApiException(
            message=message,
            status_code=status,
            error_code=error_code,
            details={"response": response.text, "request_id": request_id}
        )

    def get(self, endpoint: str, **kwargs) -> Dict[str, Any]:
        """Make GET request."""
        return self.request("GET", endpoint, **kwargs)

    def post(
        self,
        endpoint: str,
        data: Dict[str, Any] = None,
        **kwargs
    ) -> Dict[str, Any]:
        """Make POST request."""
        return self.request("POST", endpoint, data=data, **kwargs)

    def put(
        self,
        endpoint: str,
        data: Dict[str, Any] = None,
        **kwargs
    ) -> Dict[str, Any]:
        """Make PUT request."""
        return self.request("PUT", endpoint, data=data, **kwargs)

    def delete(self, endpoint: str, **kwargs) -> Dict[str, Any]:
        """Make DELETE request."""
        return self.request("DELETE", endpoint, **kwargs)

    def filter_response_by_tier(self, response: Dict[str, Any], resource_type: str) -> Dict[str, Any]:
        """
        Filter response fields based on user tier.

        Args:
            response: Response data
            resource_type: Resource type (trade, portfolio, etc.)

        Returns:
            Filtered response with only accessible fields
        """
        if not self.tier_context:
            # No tier context, return response as-is
            return response

        if isinstance(response, dict):
            # Single item response
            if "data" in response:
                response["data"] = self.tier_context.filter_response(
                    response.get("data", {}),
                    resource_type
                )
            else:
                response = self.tier_context.filter_response(response, resource_type)

        return response

    def close(self) -> None:
        """Close session."""
        if self.session:
            self.session.close()
            self.logger.debug("Session closed")

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.close()
