"""Portfolio API client for AM Trade SDK."""

from typing import Any, Dict, List

from sdk.http.base_client import BaseApiClient


class PortfolioClient(BaseApiClient):
    """Client for portfolio operations."""

    def get_portfolio_by_id(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get portfolio by ID.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Portfolio data

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        response = self.get(f"/api/v1/portfolio-summary/{portfolio_id}")
        return response.get('data', response)

    def get_all_portfolios(
        self,
        page: int = 0,
        page_size: int = 20
    ) -> Dict[str, Any]:
        """
        Get all portfolios.

        Args:
            page: Page number
            page_size: Page size

        Returns:
            dict: Paged portfolios response

        Raises:
            ApiException: If API returns error
        """
        return self.get(
            "/api/v1/portfolio-summary",
            params={"page": page, "page_size": page_size}
        )

    def create_portfolio(self, portfolio_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Create new portfolio.

        Args:
            portfolio_data: Portfolio data

        Returns:
            dict: Created portfolio

        Raises:
            ValidationException: If validation fails
            ApiException: If API returns error
        """
        response = self.post("/api/v1/portfolio-summary", data=portfolio_data)
        return response.get('data', response)

    def update_portfolio(
        self,
        portfolio_id: str,
        portfolio_data: Dict[str, Any]
    ) -> Dict[str, Any]:
        """
        Update portfolio.

        Args:
            portfolio_id: Portfolio ID
            portfolio_data: Updated portfolio data

        Returns:
            dict: Updated portfolio

        Raises:
            ValidationException: If validation fails
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        response = self.put(
            f"/api/v1/portfolio-summary/{portfolio_id}",
            data=portfolio_data
        )
        return response.get('data', response)

    def delete_portfolio(self, portfolio_id: str) -> bool:
        """
        Delete portfolio.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            bool: True if deleted

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        self.delete(f"/api/v1/portfolio-summary/{portfolio_id}")
        return True

    def get_portfolio_summary(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get detailed portfolio summary.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Portfolio summary with metrics

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/portfolio-summary/{portfolio_id}/summary"
        )

    def get_portfolio_performance(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get portfolio performance metrics.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Performance metrics

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/portfolio-summary/{portfolio_id}/performance"
        )

    def get_portfolio_stats(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get portfolio statistics.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Portfolio statistics

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/portfolio-summary/{portfolio_id}/stats"
        )

    def compare_portfolios(
        self,
        portfolio_ids: List[str]
    ) -> Dict[str, Any]:
        """
        Compare multiple portfolios.

        Args:
            portfolio_ids: List of portfolio IDs

        Returns:
            dict: Comparison data

        Raises:
            ApiException: If API returns error
        """
        return self.post(
            "/api/v1/portfolio-summary/compare",
            data={"portfolio_ids": portfolio_ids}
        )

    def export_portfolio(
        self,
        portfolio_id: str,
        format: str = "json"
    ) -> Dict[str, Any]:
        """
        Export portfolio data.

        Args:
            portfolio_id: Portfolio ID
            format: Export format (json, csv, excel)

        Returns:
            dict: Export data

        Raises:
            ValidationException: If invalid format
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/portfolio-summary/{portfolio_id}/export",
            params={"format": format}
        )
