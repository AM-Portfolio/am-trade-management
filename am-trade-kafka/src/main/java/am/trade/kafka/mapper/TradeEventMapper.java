package am.trade.kafka.mapper;

import am.trade.common.models.TradeModel;
import am.trade.models.document.Trade;
import am.trade.models.enums.OrderSide;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between TradeModel (from Kafka events) and Trade entity
 */
@Mapper(componentModel = "spring")
public interface TradeEventMapper {

    TradeEventMapper INSTANCE = Mappers.getMapper(TradeEventMapper.class);

    /**
     * Convert a single TradeModel to Trade
     */
    // id field is inherited from BaseDocument and will be handled by Spring Data MongoDB
    @Mapping(target = "tradeId", source = "basicInfo.tradeId")
    @Mapping(target = "orderId", source = "basicInfo.orderId")
    @Mapping(target = "symbol", source = "instrumentInfo.symbol")
    @Mapping(target = "tradeDate", source = "basicInfo.tradeDate", qualifiedByName = "localDateToLocalDateTime")
    @Mapping(target = "settlementDate", ignore = true) // Will be set by service layer if needed
    @Mapping(target = "side", source = "basicInfo.tradeType", qualifiedByName = "mapTradeTypeToOrderSide")
    @Mapping(target = "type", constant = "MARKET") // Default to MARKET type
    @Mapping(target = "status", constant = "FILLED") // Default to FILLED status if coming from broker
    @Mapping(target = "quantity", source = "executionInfo.quantity")
    @Mapping(target = "price", source = "executionInfo.price")
    @Mapping(target = "totalValue", expression = "java(calculateTotalValue(tradeModel))")
    @Mapping(target = "executionVenue", source = "instrumentInfo.exchange")
    @Mapping(target = "counterpartyId", constant = "BROKER") // Default value, can be overridden
    @Mapping(target = "portfolioId", ignore = true) // Should be set by the service layer
    @Mapping(target = "strategyId", ignore = true) // Should be set by the service layer
    @Mapping(target = "traderId", ignore = true) // Should be set by the service layer
    @Mapping(target = "commissionFee", source = "charges.brokerage")
    @Mapping(target = "otherFees", source = "charges.totalTaxes")
    @Mapping(target = "notes", expression = "java(createTradeNotes(tradeModel))")
    // BaseDocument fields will be handled by Spring Data auditing
    Trade toTrade(TradeModel tradeModel);

    /**
     * Convert a list of TradeModels to a list of Trades
     */
    default List<Trade> toTrades(List<TradeModel> tradeModels) {
        if (tradeModels == null) {
            return null;
        }
        return tradeModels.stream()
                .map(this::toTrade)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to calculate total trade value
     */
    default BigDecimal calculateTotalValue(TradeModel tradeModel) {
        if (tradeModel.getExecutionInfo() == null || 
            tradeModel.getExecutionInfo().getPrice() == null || 
            tradeModel.getExecutionInfo().getQuantity() == null) {
            return null;
        }
        return tradeModel.getExecutionInfo().getPrice()
                .multiply(BigDecimal.valueOf(tradeModel.getExecutionInfo().getQuantity()));
    }

    /**
     * Helper method to create trade notes
     */
    default List<String> createTradeNotes(TradeModel tradeModel) {
        String note = String.format("Processed from %s trade", 
            tradeModel.getBasicInfo() != null ? tradeModel.getBasicInfo().getBrokerType() : "Unknown");
        return List.of(note);
    }

    @Named("localDateToLocalDateTime")
    default LocalDateTime localDateToLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    @Named("mapTradeTypeToOrderSide")
    default OrderSide mapTradeTypeToOrderSide(am.trade.common.models.enums.TradeType tradeType) {
        if (tradeType == null) {
            return OrderSide.BUY; // Default to BUY if not specified
        }
        return tradeType == am.trade.common.models.enums.TradeType.BUY ? OrderSide.BUY : OrderSide.SELL;
    }
}
