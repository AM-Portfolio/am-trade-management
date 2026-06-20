package am.trade.services.mapper;

import am.trade.models.kafka.TradeHoldingEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps a TradeDTO (created/updated trade) into a TradeHoldingEvent
 * to be published to Kafka for the Portfolio service.
 */
@Component
public class TradeHoldingEventMapper {


    public TradeHoldingEvent toHoldingEvent(am.trade.common.models.TradeDetails trade, String updateType) {
        if (trade == null || trade.getEntryInfo() == null) return null;

        BigDecimal quantity = trade.getEntryInfo().getQuantity() != null
            ? BigDecimal.valueOf(trade.getEntryInfo().getQuantity())
            : BigDecimal.ZERO;

        BigDecimal price = trade.getEntryInfo().getPrice() != null
            ? trade.getEntryInfo().getPrice()
            : BigDecimal.ZERO;

        BigDecimal investmentAmount = price.multiply(quantity);

        LocalDateTime timestamp = trade.getEntryInfo().getTimestamp() != null
            ? trade.getEntryInfo().getTimestamp()
            : LocalDateTime.now();

        return TradeHoldingEvent.builder()
            .id(trade.getTradeId())
            .userId(trade.getUserId())
            .portfolioId(trade.getPortfolioId())
            .symbol(trade.getSymbol())
            .quantity(quantity)
            .averagePrice(price)
            .investmentAmount(investmentAmount)
            .timestamp(timestamp)
            .updateType(updateType)
            .build();
    }
}
