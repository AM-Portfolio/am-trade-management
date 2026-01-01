"""Journal API client for AM Trade SDK."""

from typing import Any, Dict, List

from sdk.http.base_client import BaseApiClient


class JournalClient(BaseApiClient):
    """Client for journal operations."""

    def get_journal_entry(self, entry_id: str) -> Dict[str, Any]:
        """
        Get journal entry by ID.

        Args:
            entry_id: Journal entry ID

        Returns:
            dict: Journal entry

        Raises:
            ResourceNotFoundException: If entry not found
            ApiException: If API returns error
        """
        response = self.get(f"/api/v1/journal/{entry_id}")
        return response.get('data', response)

    def get_all_entries(
        self,
        portfolio_id: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get all journal entries for portfolio.

        Args:
            portfolio_id: Portfolio ID
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged journal entries

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/journal/portfolio/{portfolio_id}",
            params={"page": page, "page_size": page_size}
        )

    def create_entry(self, entry_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create new journal entry.

        Args:
            entry_data: Journal entry data

        Returns:
            dict: Created entry

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post("/api/v1/journal", data=entry_data)
        return response.get('data', response)

    def update_entry(
        self,
        entry_id: str,
        entry_data: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Update journal entry.

        Args:
            entry_id: Entry ID
            entry_data: Updated entry data

        Returns:
            dict: Updated entry

        Raises:
            ValidationException: If validation fails
            ResourceNotFoundException: If entry not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/journal/{entry_id}",
            data=entry_data
        )
        return response.get('data', response)

    def delete_entry(self, entry_id: str) -> bool:
        """
        Delete journal entry.

        Args:
            entry_id: Entry ID

        Returns:
            bool: True if deleted

        Raises:
            ResourceNotFoundException: If entry not found
            ApiException: If API returns error
        """
        self.delete(f"/api/v1/journal/{entry_id}")
        return True

    def get_entries_by_tag(
        self,
        portfolio_id: str,
        tag: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get journal entries by tag.

        Args:
            portfolio_id: Portfolio ID
            tag: Tag name
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged entries with tag

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/journal/portfolio/{portfolio_id}/tag/{tag}",
            params={"page": page, "page_size": page_size}
        )

    def get_entries_by_trade(
        self,
        trade_id: str
    ) -> Dict[str, Any]:
        """
        Get journal entries for trade.

        Args:
            trade_id: Trade ID

        Returns:
            dict: Journal entries for trade

        Raises:
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/journal/trade/{trade_id}"
        )

    def search_entries(
        self,
        portfolio_id: str,
        query: str,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Search journal entries.

        Args:
            portfolio_id: Portfolio ID
            query: Search query
            page: Page number
            page_size: Page size

        Returns:
            dict: Search results

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/journal/portfolio/{portfolio_id}/search",
            params={
                "q": query,
                "page": page,
                "page_size": page_size
            }
        )

    def bulk_create_entries(
        self,
        entries: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        """
        Create multiple journal entries.

        Args:
            entries: List of entry data

        Returns:
            List[dict]: Created entries

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post(
            "/api/v1/journal/bulk",
            data={"entries": entries}
        )
        return response.get('data', [])

    def export_entries(
        self,
        portfolio_id: str,
        format: str = "json"
    ) -> Dict[str, Any]:
        """
        Export journal entries.

        Args:
            portfolio_id: Portfolio ID
            format: Export format (json, csv, pdf)

        Returns:
            dict: Export data

        Raises:
            ValidationException: If invalid format
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/journal/portfolio/{portfolio_id}/export",
            params={"format": format}
        )
