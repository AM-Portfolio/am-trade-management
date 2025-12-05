"""SDK request/response wrapper with versioning and metadata."""

from datetime import datetime
from typing import Any, Dict, Optional
from uuid import uuid4

from pydantic import BaseModel, Field

from sdk.version import SdkIdentifier, VersionMetadata


class SdkRequestMetadata(BaseModel):
    """Metadata attached to every SDK request."""

    request_id: str = Field(default_factory=lambda: str(uuid4()), description="Unique request ID")
    sdk_name: str = Field(default=SdkIdentifier.SDK_NAME, description="SDK name")
    sdk_version: str = Field(default_factory=VersionMetadata.get_current_version, description="SDK version")
    sdk_type: str = Field(default=SdkIdentifier.SDK_TYPE, description="SDK type")
    language: str = Field(default="python", description="Programming language")
    timestamp: datetime = Field(default_factory=datetime.utcnow, description="Request timestamp")
    client_identifier: Optional[str] = Field(None, description="Optional client identifier")

    class Config:
        """Pydantic config."""

        use_enum_values = True
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class SdkRequest(BaseModel):
    """SDK request wrapper that includes metadata."""

    metadata: SdkRequestMetadata = Field(default_factory=SdkRequestMetadata, description="SDK metadata")
    operation: str = Field(..., description="Operation type (CREATE, READ, UPDATE, DELETE, etc.)")
    resource: str = Field(..., description="Resource type (TRADE, PORTFOLIO, etc.)")
    payload: Dict[str, Any] = Field(default_factory=dict, description="Request payload")
    api_version: str = Field(default="v1", description="API version")

    class Config:
        """Pydantic config."""

        use_enum_values = True
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }

    def to_api_payload(self) -> Dict[str, Any]:
        """
        Convert SDK request to API payload.
        
        Returns:
            Dictionary with SDK wrapper and payload
        """
        return {
            "_sdk_meta": self.metadata.dict(),
            "_operation": self.operation,
            "_resource": self.resource,
            "_api_version": self.api_version,
            "data": self.payload
        }

    def to_headers(self) -> Dict[str, str]:
        """
        Get headers to include in HTTP request.
        
        Returns:
            Dictionary with SDK identification headers
        """
        return {
            "X-Request-ID": self.metadata.request_id,
            "X-SDK-Name": self.metadata.sdk_name,
            "X-SDK-Version": self.metadata.sdk_version,
            "X-SDK-Type": self.metadata.sdk_type,
            "X-SDK-Language": self.metadata.language,
            "X-Client-Timestamp": self.metadata.timestamp.isoformat()
        }


class SdkResponseMetadata(BaseModel):
    """Metadata in SDK response from backend."""

    request_id: str = Field(..., description="Request ID from request")
    server_timestamp: datetime = Field(..., description="Server timestamp")
    processing_time_ms: int = Field(..., description="Time taken to process (ms)")
    sdk_version_used: str = Field(..., description="SDK version that made the request")
    api_version: str = Field(..., description="API version used")
    
    class Config:
        """Pydantic config."""

        use_enum_values = True
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }


class SdkResponse(BaseModel):
    """SDK response wrapper that includes metadata."""

    metadata: SdkResponseMetadata = Field(..., description="Response metadata")
    success: bool = Field(..., description="Operation success status")
    status_code: int = Field(..., description="HTTP status code")
    data: Optional[Dict[str, Any]] = Field(None, description="Response data")
    message: Optional[str] = Field(None, description="Response message")
    errors: Optional[Dict[str, Any]] = Field(None, description="Error details if failed")

    class Config:
        """Pydantic config."""

        use_enum_values = True
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }

    @classmethod
    def create_success(
        cls,
        request_id: str,
        data: Dict[str, Any],
        status_code: int = 200,
        message: str = "Success",
        processing_time_ms: int = 0
    ) -> "SdkResponse":
        """
        Create a success response.
        
        Args:
            request_id: Request ID
            data: Response data
            status_code: HTTP status code
            message: Response message
            processing_time_ms: Processing time
            
        Returns:
            SdkResponse instance
        """
        return cls(
            metadata=SdkResponseMetadata(
                request_id=request_id,
                server_timestamp=datetime.utcnow(),
                processing_time_ms=processing_time_ms,
                sdk_version_used=VersionMetadata.get_current_version(),
                api_version="v1"
            ),
            success=True,
            status_code=status_code,
            data=data,
            message=message
        )

    @classmethod
    def create_error(
        cls,
        request_id: str,
        status_code: int,
        message: str,
        errors: Optional[Dict[str, Any]] = None,
        processing_time_ms: int = 0
    ) -> "SdkResponse":
        """
        Create an error response.
        
        Args:
            request_id: Request ID
            status_code: HTTP status code
            message: Error message
            errors: Error details
            processing_time_ms: Processing time
            
        Returns:
            SdkResponse instance
        """
        return cls(
            metadata=SdkResponseMetadata(
                request_id=request_id,
                server_timestamp=datetime.utcnow(),
                processing_time_ms=processing_time_ms,
                sdk_version_used=VersionMetadata.get_current_version(),
                api_version="v1"
            ),
            success=False,
            status_code=status_code,
            message=message,
            errors=errors
        )


class VersionedDataTransformer:
    """Transform user data based on SDK version."""

    @staticmethod
    def transform_for_api(
        data: Dict[str, Any],
        operation: str = "CREATE",
        resource: str = "TRADE",
        version: str = None
    ) -> SdkRequest:
        """
        Transform user data into versioned SDK request.
        
        Args:
            data: User-provided data
            operation: Operation type
            resource: Resource type
            version: SDK version (uses current if None)
            
        Returns:
            SdkRequest with metadata
        """
        if version is None:
            version = VersionMetadata.get_current_version()

        # Add version-specific transformations
        capabilities = VersionMetadata.get_version_capabilities(version)
        
        # Validate against version limits
        if resource == "TRADE" and "quantity" in data:
            # Example: Version-specific validation
            pass

        return SdkRequest(
            operation=operation,
            resource=resource,
            payload=data,
            api_version="v1"
        )

    @staticmethod
    def transform_from_api(
        api_response: Dict[str, Any],
        version: str = None
    ) -> SdkResponse:
        """
        Transform API response into versioned SDK response.
        
        Args:
            api_response: Raw API response
            version: SDK version
            
        Returns:
            SdkResponse with metadata
        """
        if version is None:
            version = VersionMetadata.get_current_version()

        # Handle response transformation based on version
        success = api_response.get("success", False)
        
        if success:
            return SdkResponse.create_success(
                request_id=api_response.get("_meta", {}).get("request_id", "unknown"),
                data=api_response.get("data", {}),
                status_code=api_response.get("status_code", 200),
                message=api_response.get("message", "Success"),
                processing_time_ms=api_response.get("_meta", {}).get("processing_time_ms", 0)
            )
        else:
            return SdkResponse.create_error(
                request_id=api_response.get("_meta", {}).get("request_id", "unknown"),
                status_code=api_response.get("status_code", 500),
                message=api_response.get("message", "Error"),
                errors=api_response.get("errors"),
                processing_time_ms=api_response.get("_meta", {}).get("processing_time_ms", 0)
            )
