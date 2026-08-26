package am.trade.api.service;

import am.trade.api.dto.PortfolioCreateRequest;
import am.trade.api.dto.PortfolioUpdateRequest;
import am.trade.common.models.PortfolioModel;

public interface PortfolioApiService {
    
    /**
     * Create a new portfolio
     * @param request The portfolio creation request
     * @return The created portfolio
     */
    PortfolioModel createPortfolio(PortfolioCreateRequest request);
    
    /**
     * Update an existing portfolio
     * @param portfolioId The ID of the portfolio to update
     * @param request The portfolio update request
     * @return The updated portfolio
     */
    PortfolioModel updatePortfolio(String portfolioId, PortfolioUpdateRequest request);
    
    /**
     * Delete a portfolio and optionally all its associated trades
     * @param portfolioId The ID of the portfolio to delete
     * @param deleteTrades Whether to also delete all trades in this portfolio
     */
    void deletePortfolio(String portfolioId, boolean deleteTrades);
}
