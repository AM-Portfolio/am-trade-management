package am.trade.api.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.am.security.context.UserContext;
import am.trade.api.dto.PortfolioCreateRequest;
import am.trade.api.dto.PortfolioUpdateRequest;
import am.trade.api.service.PortfolioApiService;
import am.trade.api.service.TradeApiService;
import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.TradeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioApiServiceImpl implements PortfolioApiService {

    private final PortfolioPersistenceService portfolioPersistenceService;
    private final TradeApiService tradeApiService;
    private final TradeDetailsService tradeDetailsService;

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"portfolioSummary", "tradeSummaryCache"}, allEntries = true)
    public PortfolioModel createPortfolio(PortfolioCreateRequest request) {
        String userId = UserContext.getUserIdOrThrow();
        log.info("Creating new portfolio for user: {}", userId);

        PortfolioModel portfolioModel = PortfolioModel.builder()
                .portfolioId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(userId)
                .active(true)
                .currency(request.getCurrency())
                .initialCapital(request.getInitialCapital())
                .currentCapital(request.getInitialCapital()) // Initially same as initial
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .metrics(new PortfolioMetrics())
                .tradeIds(new ArrayList<>())
                .winningTradeIds(new ArrayList<>())
                .losingTradeIds(new ArrayList<>())
                .assetAllocations(new ArrayList<>())
                .build();

        PortfolioModel saved = portfolioPersistenceService.savePortfolio(portfolioModel);
        tradeApiService.publishBulkPortfolioSyncEvent(saved.getPortfolioId(), saved.getName(), saved.getOwnerId(), new ArrayList<>(), "CREATE");
        return saved;
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"portfolioSummary", "tradeSummaryCache"}, allEntries = true)
    public PortfolioModel updatePortfolio(String portfolioId, PortfolioUpdateRequest request) {
        String userId = UserContext.getUserIdOrThrow();
        log.info("Updating portfolio: {} for user: {}", portfolioId, userId);

        PortfolioModel existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found with ID: " + portfolioId));

        if (!existingPortfolio.getOwnerId().equals(userId)) {
            log.error("User {} attempted to update portfolio {} owned by {}", userId, portfolioId, existingPortfolio.getOwnerId());
            throw new IllegalArgumentException("You are not authorized to update this portfolio");
        }

        // Update only user-editable fields
        existingPortfolio.setName(request.getName());
        existingPortfolio.setDescription(request.getDescription());
        existingPortfolio.setCurrency(request.getCurrency());
        existingPortfolio.setInitialCapital(request.getInitialCapital());
        existingPortfolio.setLastUpdatedDate(LocalDateTime.now());
        
        // Save and return
        PortfolioModel saved = portfolioPersistenceService.savePortfolio(existingPortfolio);
        List<TradeDetails> trades = tradeDetailsService.findModelsByPortfolioId(saved.getPortfolioId());
        tradeApiService.publishBulkPortfolioSyncEvent(saved.getPortfolioId(), saved.getName(), saved.getOwnerId(), trades, "REPLACE_ALL");
        return saved;
    }

    @Override
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = {"portfolioSummary", "tradeSummaryCache"}, allEntries = true)
    public void deletePortfolio(String portfolioId, boolean deleteTrades) {
        String userId = UserContext.getUserIdOrThrow();
        log.info("Deleting portfolio: {} for user: {}. deleteTrades: {}", portfolioId, userId, deleteTrades);

        PortfolioModel existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found with ID: " + portfolioId));

        if (!existingPortfolio.getOwnerId().equals(userId)) {
            log.error("User {} attempted to delete portfolio {} owned by {}", userId, portfolioId, existingPortfolio.getOwnerId());
            throw new IllegalArgumentException("You are not authorized to delete this portfolio");
        }

        if (deleteTrades) {
            log.info("Deleting all trades for portfolio: {}", portfolioId);
            List<TradeDetails> trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
            for (TradeDetails trade : trades) {
                // Delete trade from local DB directly
                tradeDetailsService.deleteByTradeId(trade.getTradeId());
            }
            // Send ONE Kafka message for portfolio and all trades
            tradeApiService.publishBulkPortfolioSyncEvent(portfolioId, existingPortfolio.getName(), userId, trades, "DELETE_PORTFOLIO");
        } else {
            log.info("Leaving trades orphaned for portfolio: {}", portfolioId);
            // Send ONE Kafka message for the empty portfolio
            tradeApiService.publishBulkPortfolioSyncEvent(portfolioId, existingPortfolio.getName(), userId, new ArrayList<>(), "DELETE_PORTFOLIO");
        }

        portfolioPersistenceService.deleteByPortfolioId(portfolioId);
        log.info("Successfully deleted portfolio: {}", portfolioId);
    }
}
