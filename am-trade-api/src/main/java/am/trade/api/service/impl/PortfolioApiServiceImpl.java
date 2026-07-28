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

        return portfolioPersistenceService.savePortfolio(portfolioModel);
    }

    @Override
    @Transactional
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
        return portfolioPersistenceService.savePortfolio(existingPortfolio);
    }

    @Override
    @Transactional
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
                // We call the API service so that Kafka sync events (DELETE) are properly emitted
                tradeApiService.deleteTrade(trade.getTradeId());
            }
        } else {
            log.info("Leaving trades orphaned for portfolio: {}", portfolioId);
            // Optionally, we could find the trades and set their portfolioId to null,
            // but the domain model usually just leaves the ID pointing to nowhere (orphaned)
            // or the user re-assigns them. For now, they simply become orphaned.
        }

        portfolioPersistenceService.deleteByPortfolioId(portfolioId);
        log.info("Successfully deleted portfolio: {}", portfolioId);
    }
}
