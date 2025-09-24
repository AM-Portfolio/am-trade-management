package am.trade.analytics.mapper;

import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayResponse;
import am.trade.analytics.model.dto.TradeReplayRequest;
import am.trade.common.models.PriceDataPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper class for converting between TradeReplay entity and DTOs
 */
@Component
public class TradeReplayMapper {

    /**
     * Creates a TradeReplay entity from a TradeReplayRequest
     * 
     * @param request The trade replay request
     * @param replayId The generated replay ID
     * @param profitLoss The calculated profit/loss
     * @param profitLossPercentage The calculated profit/loss percentage
     * @param maxDrawdown The calculated maximum drawdown
     * @param maxDrawdownPercentage The calculated maximum drawdown percentage
     * @param maxProfit The calculated maximum profit
     * @param maxProfitPercentage The calculated maximum profit percentage
     * @param holdingPeriodDays The calculated holding period in days
     * @param volatility The calculated volatility
     * @param averageDailyMovement The calculated average daily movement
     * @param priceDataPoints The price data points
     * @return A new TradeReplay entity
     */
    public TradeReplay createTradeReplayEntity(
            TradeReplayRequest request,
            String replayId,
            BigDecimal profitLoss,
            BigDecimal profitLossPercentage,
            BigDecimal maxDrawdown,
            BigDecimal maxDrawdownPercentage,
            BigDecimal maxProfit,
            BigDecimal maxProfitPercentage,
            Integer holdingPeriodDays,
            BigDecimal volatility,
            BigDecimal averageDailyMovement,
            List<PriceDataPoint> priceDataPoints) {
        
        return TradeReplay.builder()
                .replayId(replayId)
                .symbol(request.getSymbol())
                .entryDate(request.getEntryDate())
                .exitDate(request.getExitDate())
                .entryPrice(request.getEntryPrice())
                .exitPrice(request.getExitPrice())
                .side(request.getSide())
                .positionSize(request.getPositionSize())
                .profitLoss(profitLoss)
                .profitLossPercentage(profitLossPercentage)
                .maxDrawdown(maxDrawdown)
                .maxDrawdownPercentage(maxDrawdownPercentage)
                .maxProfit(maxProfit)
                .maxProfitPercentage(maxProfitPercentage)
                .holdingPeriodDays(holdingPeriodDays)
                .volatility(volatility)
                .averageDailyMovement(averageDailyMovement)
                .originalTradeId(request.getOriginalTradeId())
                .strategyId(request.getStrategyId())
                .portfolioId(request.getPortfolioId())
                .priceDataPoints(priceDataPoints)
                .replayNotes(new ArrayList<>())
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
    }
    
    /**
     * Converts a TradeReplay entity to a TradeReplayResponse DTO
     * 
     * @param tradeReplay The trade replay entity
     * @return A TradeReplayResponse DTO
     */
    public TradeReplayResponse toTradeReplayResponse(TradeReplay tradeReplay) {
        return TradeReplayResponse.builder()
                .replayId(tradeReplay.getReplayId())
                .symbol(tradeReplay.getSymbol())
                .entryDate(tradeReplay.getEntryDate())
                .exitDate(tradeReplay.getExitDate())
                .entryPrice(tradeReplay.getEntryPrice())
                .exitPrice(tradeReplay.getExitPrice())
                .side(tradeReplay.getSide())
                .positionSize(tradeReplay.getPositionSize())
                .profitLoss(tradeReplay.getProfitLoss())
                .profitLossPercentage(tradeReplay.getProfitLossPercentage())
                .maxDrawdown(tradeReplay.getMaxDrawdown())
                .maxDrawdownPercentage(tradeReplay.getMaxDrawdownPercentage())
                .maxProfit(tradeReplay.getMaxProfit())
                .maxProfitPercentage(tradeReplay.getMaxProfitPercentage())
                .holdingPeriodDays(tradeReplay.getHoldingPeriodDays())
                .volatility(tradeReplay.getVolatility())
                .averageDailyMovement(tradeReplay.getAverageDailyMovement())
                .originalTradeId(tradeReplay.getOriginalTradeId())
                .strategyId(tradeReplay.getStrategyId())
                .portfolioId(tradeReplay.getPortfolioId())
                .priceDataPoints(tradeReplay.getPriceDataPoints())
                .replayNotes(tradeReplay.getReplayNotes())
                .createdDate(tradeReplay.getCreatedDate())
                .build();
    }
}
