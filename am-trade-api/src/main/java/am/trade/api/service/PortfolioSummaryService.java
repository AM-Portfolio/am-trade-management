package am.trade.api.service;

import am.trade.common.models.PortfolioModel;
import am.trade.common.models.PortfolioSummaryDTO;
import am.trade.common.models.AssetAllocation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service for portfolio summary operations
 */
public interface PortfolioSummaryService {

    /**
     * Get a comprehensive summary of a portfolio
     * 
     * @param portfolioId the ID of the portfolio
     * @return the portfolio summary
     */
    PortfolioModel getPortfolioSummary(String portfolioId);

    /**
     * Get asset allocation for a portfolio
     * 
     * @param portfolioId the ID of the portfolio
     * @return a list of asset allocations
     */
    List<AssetAllocation> getAssetAllocation(String portfolioId);

    /**
     * Get portfolio performance over a time period
     * 
     * @param portfolioId the ID of the portfolio
     * @param startDate start date
     * @param endDate end date
     * @return a map of dates to performance values
     */
    Map<LocalDate, Double> getPortfolioPerformance(String portfolioId, LocalDate startDate, LocalDate endDate);

    /**
     * Compare multiple portfolios
     * 
     * @param portfolioIds list of portfolio IDs to compare
     * @return a map of portfolio ID to portfolio model
     */
    Map<String, PortfolioModel> comparePortfolios(List<String> portfolioIds);
    
    /**
     * Get a list of portfolio summaries for a specific owner.
     * Returns the full PortfolioModel so the frontend receives metrics (winRate, netProfitLoss, etc.)
     *
     * @param ownerId the ID of the portfolio owner
     * @return a list of full portfolio models
     */
    List<PortfolioModel> getPortfolioSummariesByOwnerId(String ownerId);
}
