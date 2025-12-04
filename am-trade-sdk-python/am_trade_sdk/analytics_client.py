"""Analytics API client for AM Trade SDK."""

from typing import Any, Dict

from am_trade_sdk.base_client import BaseApiClient


class AnalyticsClient(BaseApiClient):
    """Client for analytics operations."""

    def get_trade_metrics(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get trade metrics.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Trade metrics

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/metrics/{portfolio_id}"
        )

    def get_trade_summary(
        self,
        portfolio_id: str,
        period: str = "ALL"
    ) -> Dict[str, Any]:
        """
        Get trade summary by period.

        Args:
            portfolio_id: Portfolio ID
            period: Time period (DAY, WEEK, MONTH, YEAR, ALL)

        Returns:
            dict: Trade summary

        Raises:
            ValidationException: If invalid period
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/trade-summary/{portfolio_id}",
            params={"period": period}
        )

    def get_analytics_data(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get analytics data.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Analytics data

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/analytics/trade-replays/{portfolio_id}"
        )

    def get_trade_replay(self, trade_id: str) -> Dict[str, Any]:
        """
        Get trade replay data.

        Args:
            trade_id: Trade ID

        Returns:
            dict: Trade replay data

        Raises:
            ResourceNotFoundException: If trade not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/analytics/trade-replays/{trade_id}"
        )

    def get_pnl_heatmap(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get P&L heatmap data.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Heatmap data

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/heatmap/{portfolio_id}"
        )

    def get_performance_chart(
        self,
        portfolio_id: str,
        period: str = "1M"
    ) -> Dict[str, Any]:
        """
        Get performance chart data.

        Args:
            portfolio_id: Portfolio ID
            period: Time period (1D, 1W, 1M, 3M, 6M, 1Y, ALL)

        Returns:
            dict: Chart data

        Raises:
            ValidationException: If invalid period
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/analytics/{portfolio_id}/performance",
            params={"period": period}
        )

    def get_risk_analysis(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get risk analysis data.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Risk analysis data

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/analytics/{portfolio_id}/risk"
        )

    def get_correlation_matrix(self, portfolio_id: str) -> Dict[str, Any]:
        """
        Get correlation matrix.

        Args:
            portfolio_id: Portfolio ID

        Returns:
            dict: Correlation matrix data

        Raises:
            ResourceNotFoundException: If portfolio not found
            ApiException: If API returns error
        """
        return self.get(
            f"/api/v1/analytics/{portfolio_id}/correlation"
        )

    def compare_performance(
        self,
        portfolio_ids: list,
        period: str = "1M"
    ) -> Dict[str, Any]:
        """
        Compare performance across portfolios.

        Args:
            portfolio_ids: List of portfolio IDs
            period: Time period

        Returns:
            dict: Comparison data

        Raises:
            ApiException: If API returns error
        """
        return self.post(
            "/api/v1/analytics/compare",
            data={
                "portfolio_ids": portfolio_ids,
                "period": period
            }
        )
