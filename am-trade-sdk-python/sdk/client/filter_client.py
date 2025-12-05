"""Filter API client for AM Trade SDK."""

from typing import Any, Dict, List

from sdk.http.base_client import BaseApiClient


class FilterClient(BaseApiClient):
    """Client for filter operations."""

    def get_filter_by_id(self, filter_id: str) -> Dict[str, Any]:
        """
        Get filter by ID.

        Args:
            filter_id: Filter ID

        Returns:
            dict: Filter data

        Raises:
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        response = self.get(f"/api/v1/filters/{filter_id}")
        return response.get('data', response)

    def get_all_filters(
        self,
        portfolio_id: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get all filters for portfolio.

        Args:
            portfolio_id: Portfolio ID
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged filters

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/filters/portfolio/{portfolio_id}",
            params={"page": page, "page_size": page_size}
        )

    def create_filter(self, filter_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create new filter.

        Args:
            filter_data: Filter data

        Returns:
            dict: Created filter

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post("/api/v1/filters", data=filter_data)
        return response.get('data', response)

    def update_filter(
        self,
        filter_id: str,
        filter_data: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Update filter.

        Args:
            filter_id: Filter ID
            filter_data: Updated filter data

        Returns:
            dict: Updated filter

        Raises:
            ValidationException: If validation fails
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/filters/{filter_id}",
            data=filter_data
        )
        return response.get('data', response)

    def delete_filter(self, filter_id: str) -> bool:
        """
        Delete filter.

        Args:
            filter_id: Filter ID

        Returns:
            bool: True if deleted

        Raises:
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        self.delete(f"/api/v1/filters/{filter_id}")
        return True

    def get_shared_filters(
        self,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get shared filters.

        Args:
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged shared filters

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            "/api/v1/filters/shared",
            params={"page": page, "page_size": page_size}
        )

    def share_filter(self, filter_id: str) -> Dict[str, Any]:
        """
        Share filter with other users.

        Args:
            filter_id: Filter ID

        Returns:
            dict: Updated filter

        Raises:
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/filters/{filter_id}/share"
        )
        return response.get('data', response)

    def unshare_filter(self, filter_id: str) -> Dict[str, Any]:
        """
        Unshare filter from other users.

        Args:
            filter_id: Filter ID

        Returns:
            dict: Updated filter

        Raises:
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/filters/{filter_id}/unshare"
        )
        return response.get('data', response)

    def duplicate_filter(
        self,
        filter_id: str,
        new_name: str
    ) -> Dict[str, Any]:
        """
        Duplicate filter with new name.

        Args:
            filter_id: Filter ID to duplicate
            new_name: New filter name

        Returns:
            dict: Duplicated filter

        Raises:
            ResourceNotFoundException: If filter not found
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post(
            f"/api/v1/filters/{filter_id}/duplicate",
            data={"name": new_name}
        )
        return response.get('data', response)

    def bulk_delete_filters(
        self,
        filter_ids: List[str]
    ) -> Dict[str, Any]:
        """
        Delete multiple filters.

        Args:
            filter_ids: List of filter IDs

        Returns:
            dict: Deletion result

        Raises:
            ApiException: If API returns error
        """
        return self.post(
            "/api/v1/filters/bulk/delete",
            data={"ids": filter_ids}
        )

    def export_filter(
        self,
        filter_id: str,
        format: str = "json"
    ) -> Dict[str, Any]:
        """
        Export filter definition.

        Args:
            filter_id: Filter ID
            format: Export format (json, yaml)

        Returns:
            dict: Export data

        Raises:
            ValidationException: If invalid format
            ResourceNotFoundException: If filter not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/filters/{filter_id}/export",
            params={"format": format}
        )

    def import_filter(
        self,
        import_data: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Import filter from exported data.

        Args:
            import_data: Export data to import

        Returns:
            dict: Imported filter

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post(
            "/api/v1/filters/import",
            data=import_data
        )
        return response.get('data', response)
