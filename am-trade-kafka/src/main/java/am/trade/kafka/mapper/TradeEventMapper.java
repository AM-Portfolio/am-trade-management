package am.trade.kafka.mapper;

import am.trade.common.models.TradeModel;
import am.trade.models.document.Trade;
import am.trade.models.dto.TradeDTO;
import am.trade.models.enums.OrderSide;
import am.trade.models.enums.OrderStatus;
import am.trade.models.enums.OrderType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Component for converting between TradeModel (from Kafka events) and Trade entity
 */
@Component
public class TradeEventMapper {

    /**
     * Convert a single TradeModel to Trade
     */
    public Trade toTrade(TradeModel tradeModel) {
        if (tradeModel == null) {
            return null;
        }

        Trade trade = new Trade();
        
        // Map from nested properties
        if (tradeModel.getBasicInfo() != null) {
            trade.setTradeId(tradeModel.getBasicInfo().getTradeId());
            trade.setOrderId(tradeModel.getBasicInfo().getOrderId());
            trade.setTradeDate(localDateToLocalDateTime(tradeModel.getBasicInfo().getTradeDate()));
            trade.setSide(mapTradeTypeToOrderSide(tradeModel.getBasicInfo().getTradeType()));
        }
        
        if (tradeModel.getInstrumentInfo() != null) {
            trade.setSymbol(tradeModel.getInstrumentInfo().getSymbol());
            trade.setExecutionVenue(tradeModel.getInstrumentInfo().getExchange());
        }
        
        if (tradeModel.getExecutionInfo() != null) {
            trade.setQuantity(tradeModel.getExecutionInfo().getQuantity());
            trade.setPrice(tradeModel.getExecutionInfo().getPrice());
        }
        
        if (tradeModel.getCharges() != null) {
            trade.setCommissionFee(tradeModel.getCharges().getBrokerage());
            trade.setOtherFees(tradeModel.getCharges().getTotalTaxes());
        }
        
        // Set default values
        trade.setType(OrderType.MARKET);
        trade.setStatus(OrderStatus.FILLED);
        trade.setCounterpartyId("BROKER");
        
        // Set calculated values
        trade.setTotalValue(calculateTotalValue(tradeModel));
        trade.setNotes(createTradeNotes(tradeModel));
        
        return trade;
    }

    /**
     * Convert a list of TradeModels to a list of Trades
     */
    public List<Trade> toTrades(List<TradeModel> tradeModels) {
        if (tradeModels == null) {
            return null;
        }
        
        List<Trade> trades = new ArrayList<>(tradeModels.size());
        for (TradeModel tradeModel : tradeModels) {
            trades.add(toTrade(tradeModel));
        }
        return trades;
    }
    
    /**
     * Convert a TradeModel directly to TradeDTO without going through Trade entity
     */
    public TradeDTO toTradeDTO(TradeModel tradeModel) {
        if (tradeModel == null) {
            return null;
        }

        TradeDTO.TradeDTOBuilder tradeDTOBuilder = TradeDTO.builder();
        
        // Generate random UUID for id
        tradeDTOBuilder.id(UUID.randomUUID().toString());
        
        // Map from nested properties
        if (tradeModel.getBasicInfo() != null) {
            tradeDTOBuilder.tradeId(tradeModel.getBasicInfo().getTradeId());
            tradeDTOBuilder.orderId(tradeModel.getBasicInfo().getOrderId());
            tradeDTOBuilder.tradeDate(localDateToLocalDateTime(tradeModel.getBasicInfo().getTradeDate()));
            tradeDTOBuilder.orderExecutionTime(mapLocalDateToLocalDateTime(tradeModel.getBasicInfo().getOrderExecutionTime()));
            tradeDTOBuilder.side(mapTradeTypeToOrderSide(tradeModel.getBasicInfo().getTradeType()));
        }
        
        if (tradeModel.getInstrumentInfo() != null) {
            tradeDTOBuilder.symbol(tradeModel.getInstrumentInfo().getSymbol());
            tradeDTOBuilder.executionVenue(tradeModel.getInstrumentInfo().getExchange());
        }
        
        if (tradeModel.getExecutionInfo() != null) {
            if (tradeModel.getExecutionInfo().getQuantity() != null) {
                tradeDTOBuilder.quantity(tradeModel.getExecutionInfo().getQuantity());
            }
            tradeDTOBuilder.price(tradeModel.getExecutionInfo().getPrice());
        }
        
        if (tradeModel.getCharges() != null) {
            tradeDTOBuilder.commissionFee(tradeModel.getCharges().getBrokerage());
            tradeDTOBuilder.otherFees(tradeModel.getCharges().getTotalTaxes());
        }
        
        // Set default values
        tradeDTOBuilder.type(OrderType.MARKET);
        tradeDTOBuilder.status(OrderStatus.FILLED);
        tradeDTOBuilder.counterpartyId("BROKER");
        
        // Set calculated values
        tradeDTOBuilder.totalValue(calculateTotalValue(tradeModel));
        tradeDTOBuilder.notes(createTradeNotes(tradeModel));
        
        return tradeDTOBuilder.build();
    }
    
    /**
     * Convert a list of TradeModels directly to a list of TradeDTOs
     */
    public List<TradeDTO> toTradeDTOs(List<TradeModel> tradeModels) {
        if (tradeModels == null) {
            return null;
        }
        
        List<TradeDTO> tradeDTOs = new ArrayList<>(tradeModels.size());
        for (TradeModel tradeModel : tradeModels) {
            tradeDTOs.add(toTradeDTO(tradeModel));
        }
        return tradeDTOs;
    }

    /**
     * Helper method to calculate total trade value
     */
    public BigDecimal calculateTotalValue(TradeModel tradeModel) {
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
    public List<String> createTradeNotes(TradeModel tradeModel) {
        String note = String.format("Processed from %s trade", 
            tradeModel.getBasicInfo() != null ? tradeModel.getBasicInfo().getBrokerType() : "Unknown");
        return List.of(note);
    }

    public LocalDateTime localDateToLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public LocalDateTime mapLocalDateToLocalDateTime(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date;
    }

    public OrderSide mapTradeTypeToOrderSide(am.trade.common.models.enums.TradeType tradeType) {
        if (tradeType == null) {
            return OrderSide.BUY; // Default to BUY if not specified
        }
        return tradeType == am.trade.common.models.enums.TradeType.BUY ? OrderSide.BUY : OrderSide.SELL;
    }
}
