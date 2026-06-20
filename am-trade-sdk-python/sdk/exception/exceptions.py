"""Exception classes for AM Trade SDK."""

from typing import Any, Dict, Optional


class AmTradeSdkException(Exception):
    """Base exception for AM Trade SDK."""

    def __init__(
        self,
        message: str,
        status_code: Optional[int] = None,
        error_code: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize exception."""
        self.message = message
        self.status_code = status_code
        self.error_code = error_code
        self.details = details or {}
        super().__init__(self.message)

    def to_dict(self) -> Dict[str, Any]:
        """Convert exception to dictionary."""
        return {
            'exception_type': self.__class__.__name__,
            'message': self.message,
            'status_code': self.status_code,
            'error_code': self.error_code,
            'details': self.details,
        }


class ApiException(AmTradeSdkException):
    """Exception raised for API errors."""

    def __init__(
        self,
        message: str,
        status_code: int,
        error_code: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize API exception."""
        super().__init__(
            message,
            status_code=status_code,
            error_code=error_code or self._get_error_code(status_code),
            details=details
        )

    @staticmethod
    def _get_error_code(status_code: int) -> str:
        """Get error code from HTTP status."""
        status_map = {
            400: 'BAD_REQUEST',
            401: 'UNAUTHORIZED',
            403: 'FORBIDDEN',
            404: 'NOT_FOUND',
            409: 'CONFLICT',
            429: 'RATE_LIMITED',
            500: 'INTERNAL_SERVER_ERROR',
            502: 'BAD_GATEWAY',
            503: 'SERVICE_UNAVAILABLE',
        }
        return status_map.get(status_code, f'HTTP_{status_code}')


class ValidationException(AmTradeSdkException):
    """Exception raised for validation errors."""

    def __init__(
        self,
        message: str,
        field: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize validation exception."""
        details = details or {}
        if field:
            details['field'] = field
        super().__init__(
            message,
            status_code=400,
            error_code='VALIDATION_ERROR',
            details=details
        )


class NetworkException(AmTradeSdkException):
    """Exception raised for network errors."""

    def __init__(
        self,
        message: str,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize network exception."""
        super().__init__(
            message,
            error_code='NETWORK_ERROR',
            details=details
        )


class TimeoutException(AmTradeSdkException):
    """Exception raised for request timeouts."""

    def __init__(
        self,
        message: str,
        timeout_seconds: Optional[int] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize timeout exception."""
        details = details or {}
        if timeout_seconds:
            details['timeout_seconds'] = timeout_seconds
        super().__init__(
            message,
            error_code='REQUEST_TIMEOUT',
            details=details
        )


class ConfigurationException(AmTradeSdkException):
    """Exception raised for configuration errors."""

    def __init__(
        self,
        message: str,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize configuration exception."""
        super().__init__(
            message,
            error_code='CONFIGURATION_ERROR',
            details=details
        )


class AuthenticationException(AmTradeSdkException):
    """Exception raised for authentication failures."""

    def __init__(
        self,
        message: str,
        status_code: int = 401,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize authentication exception."""
        super().__init__(
            message,
            status_code=status_code,
            error_code='AUTHENTICATION_FAILED',
            details=details
        )


class RateLimitException(AmTradeSdkException):
    """Exception raised when rate limit is exceeded."""

    def __init__(
        self,
        message: str,
        retry_after: Optional[int] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize rate limit exception."""
        details = details or {}
        if retry_after:
            details['retry_after_seconds'] = retry_after
        super().__init__(
            message,
            status_code=429,
            error_code='RATE_LIMITED',
            details=details
        )


class ResourceNotFoundException(AmTradeSdkException):
    """Exception raised when resource is not found."""

    def __init__(
        self,
        message: str,
        resource_type: Optional[str] = None,
        resource_id: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize not found exception."""
        details = details or {}
        if resource_type:
            details['resource_type'] = resource_type
        if resource_id:
            details['resource_id'] = resource_id
        super().__init__(
            message,
            status_code=404,
            error_code='RESOURCE_NOT_FOUND',
            details=details
        )


class ConflictException(AmTradeSdkException):
    """Exception raised for resource conflicts."""

    def __init__(
        self,
        message: str,
        details: Optional[Dict[str, Any]] = None
    ):
        """Initialize conflict exception."""
        super().__init__(
            message,
            status_code=409,
            error_code='CONFLICT',
            details=details
        )
