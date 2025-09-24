package am.trade.services.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for retrieving trade details across multiple portfolios
 */
@Service
public class PortfolioTradeService {

    private final TradeDetailsRepository tradeDetailsRepository;
    private final TradeDetailsMapper tradeDetailsMapper;

    public PortfolioTradeService(TradeDetailsRepository tradeDetailsRepository, TradeDetailsMapper tradeDetailsMapper) {
        this.tradeDetailsRepository = tradeDetailsRepository;
        this.tradeDetailsMapper = tradeDetailsMapper;
    }

    /**
     * Get all trade details for multiple portfolios
     * 
     * @param portfolioIds List of portfolio IDs to retrieve trades for
     * @return List of trade details for all specified portfolios
     */
    public List<TradeDetails> getTradeDetailsForPortfolios(List<String> portfolioIds) {
        List<TradeDetailsEntity> tradeEntities = tradeDetailsRepository.findByPortfolioIdIn(portfolioIds);
        return tradeEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    /**
     * Get paginated trade details for multiple portfolios
     * 
     * @param portfolioIds List of portfolio IDs to retrieve trades for
     * @param pageable Pagination information
     * @return Page of trade details for all specified portfolios
     */
    public Page<TradeDetails> getTradeDetailsForPortfoliosPaginated(List<String> portfolioIds, Pageable pageable) {
        Page<TradeDetailsEntity> tradeEntitiesPage = tradeDetailsRepository.findByPortfolioIdIn(portfolioIds, pageable);
        return tradeEntitiesPage.map(tradeDetailsMapper::toTradeDetails);
    }
    
    /**
     * Count total trades across multiple portfolios
     * 
     * @param portfolioIds List of portfolio IDs to count trades for
     * @return Total number of trades across all specified portfolios
     */
    public long countTradesAcrossPortfolios(List<String> portfolioIds) {
        return tradeDetailsRepository.findByPortfolioIdIn(portfolioIds).size();
    }
    
    /**
     * Get all trade details for a single portfolio
     * 
     * @param portfolioId Portfolio ID to retrieve trades for
     * @return List of trade details for the specified portfolio
     */
    public List<TradeDetails> getTradeDetailsForPortfolio(String portfolioId) {
        List<TradeDetailsEntity> tradeEntities = tradeDetailsRepository.findByPortfolioId(portfolioId);
        return tradeEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
}
