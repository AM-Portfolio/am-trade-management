package am.trade.dashboard.service;

import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;
import am.trade.dashboard.service.TradeMetricsCalculationService;
import am.trade.services.service.impl.TradeSummaryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Implementation of the MetricsCalculationService interface
 * Handles caching and calculation of trade metrics with time-based invalidation
 */
@Service
@Slf4j
public class MetricsCalculationServiceImpl implements MetricsCalculationService {

    private final TradeSummaryServiceImpl tradeSummaryService;
    private final TradeMetricsCalculationService tradeMetricsCalculationService;

    @Value("${metrics.cache.invalidation.hours:24}")
    private long cacheInvalidationHours;

    public MetricsCalculationServiceImpl(TradeSummaryServiceImpl tradeSummaryService,
                                        TradeMetricsCalculationService tradeMetricsCalculationService) {
        this.tradeSummaryService = tradeSummaryService;
        this.tradeMetricsCalculationService = tradeMetricsCalculationService;
    }

    /**
     * Calculate detailed metrics for a basic trade summary
     * This method performs the actual calculation and updates the lastCalculatedTimestamp
     *
     * @param basicSummary The basic trade summary to calculate metrics for
     * @return The detailed trade metrics
     */
    @Override
    public TradeSummaryDetailed calculateDetailedMetrics(TradeSummaryBasic basicSummary) {
        log.info("Calculating detailed metrics for basic summary ID: {}", basicSummary.getId());
        
        // Get existing detailed summary or create a new one
        TradeSummaryDetailed detailedSummary = tradeSummaryService.findDetailedByBasicId(basicSummary.getId())
                .orElse(new TradeSummaryDetailed());
        
        // Set reference to basic summary
        detailedSummary.setTradeSummaryBasicId(basicSummary.getId());
        
        // Calculate metrics using the existing trade metrics calculation service
        // We'll use the portfolioIds from the basic summary to calculate metrics
        TradeSummary calculatedSummary = tradeMetricsCalculationService.calculateAllMetrics(basicSummary.getPortfolioIds());
        
        // Transfer calculated metrics to detailed summary
        detailedSummary.setPerformanceMetrics(calculatedSummary.getPerformanceMetrics());
        detailedSummary.setRiskMetrics(calculatedSummary.getRiskMetrics());
        detailedSummary.setDistributionMetrics(calculatedSummary.getDistributionMetrics());
        detailedSummary.setTimingMetrics(calculatedSummary.getTimingMetrics());
        detailedSummary.setPatternMetrics(calculatedSummary.getPatternMetrics());
        detailedSummary.setTradingFeedback(calculatedSummary.getTradingFeedback());
        
        // Update calculation timestamp
        detailedSummary.setLastCalculatedTimestamp(LocalDateTime.now());
        
        // Save and return the updated detailed summary
        return tradeSummaryService.saveTradeSummaryDetailed(detailedSummary);
    }

    /**
     * Check if metrics need to be recalculated based on cache expiry or data changes
     *
     * @param basicSummary The basic trade summary
     * @param detailedSummary The detailed trade metrics
     * @return true if metrics need recalculation, false otherwise
     */
    @Override
    public boolean needsRecalculation(TradeSummaryBasic basicSummary, TradeSummaryDetailed detailedSummary) {
        if (detailedSummary == null || detailedSummary.getLastCalculatedTimestamp() == null) {
            log.info("Metrics recalculation needed: No detailed summary or calculation timestamp");
            return true;
        }
        
        // Check if cache has expired based on configured invalidation period
        LocalDateTime expiryTime = detailedSummary.getLastCalculatedTimestamp()
                .plus(Duration.ofHours(cacheInvalidationHours));
                
        if (LocalDateTime.now().isAfter(expiryTime)) {
            log.info("Metrics recalculation needed: Cache expired for basic summary ID: {}", basicSummary.getId());
            return true;
        }
        
        // Check if basic data has been updated since last calculation
        if (basicSummary.getLastUpdatedDate() != null && 
                basicSummary.getLastUpdatedDate().isAfter(detailedSummary.getLastCalculatedTimestamp())) {
            log.info("Metrics recalculation needed: Basic data updated since last calculation for ID: {}", 
                    basicSummary.getId());
            return true;
        }
        
        log.debug("No metrics recalculation needed for basic summary ID: {}", basicSummary.getId());
        return false;
    }

    /**
     * Get a composite trade summary with up-to-date metrics
     * This method will check if recalculation is needed and perform it if necessary
     *
     * @param basicSummaryId The ID of the basic trade summary
     * @return The composite trade summary with up-to-date metrics, or null if not found
     */
    @Override
    @Cacheable(value = "tradeSummaryCache", key = "#basicSummaryId")
    public TradeSummary getTradeSummaryWithMetrics(String basicSummaryId) {
        log.info("Getting trade summary with metrics for ID: {}", basicSummaryId);
        
        // Get basic summary
        Optional<TradeSummaryBasic> basicOpt = tradeSummaryService.findBasicById(basicSummaryId);
        if (basicOpt.isEmpty()) {
            log.warn("Basic trade summary not found for ID: {}", basicSummaryId);
            return null;
        }
        
        TradeSummaryBasic basicSummary = basicOpt.get();
        
        // Get detailed summary
        Optional<TradeSummaryDetailed> detailedOpt = tradeSummaryService.findDetailedByBasicId(basicSummaryId);
        
        // Check if we need to calculate metrics
        TradeSummaryDetailed detailedSummary;
        if (detailedOpt.isEmpty() || needsRecalculation(basicSummary, detailedOpt.get())) {
            // Calculate metrics
            detailedSummary = calculateDetailedMetrics(basicSummary);
        } else {
            detailedSummary = detailedOpt.get();
        }
        
        // Create and return composite summary
        return TradeSummary.fromBasicAndDetailed(basicSummary, detailedSummary);
    }

    /**
     * Scheduled task to recalculate expired metrics
     * Runs at 2 AM every day by default
     */
    @Scheduled(cron = "${metrics.recalculation.cron:0 0 2 * * ?}")
    @CacheEvict(value = "tradeSummaryCache", allEntries = true)
    public void recalculateExpiredMetrics() {
        log.info("Starting scheduled recalculation of expired metrics");
        
        // Get all active basic summaries
        tradeSummaryService.findAllActiveBasic().forEach(basicSummary -> {
            try {
                // Get detailed summary
                Optional<TradeSummaryDetailed> detailedOpt = 
                        tradeSummaryService.findDetailedByBasicId(basicSummary.getId());
                
                // Check if recalculation is needed
                if (detailedOpt.isEmpty() || needsRecalculation(basicSummary, detailedOpt.get())) {
                    log.info("Recalculating metrics for basic summary ID: {}", basicSummary.getId());
                    calculateDetailedMetrics(basicSummary);
                }
            } catch (Exception e) {
                log.error("Error recalculating metrics for basic summary ID: {}", 
                        basicSummary.getId(), e);
            }
        });
        
        log.info("Completed scheduled recalculation of expired metrics");
    }
}
