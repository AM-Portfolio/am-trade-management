package am.trade.services.mapper;

import org.springframework.stereotype.Component;

import am.trade.models.document.Trade;
import am.trade.services.dto.TradeDTO;

/**
 * Mapper for converting between Trade entity and TradeDTO
 */
@Component
public class TradeMapper {
    
    /**
     * Convert Trade entity to TradeDTO
     */
    public TradeDTO toDto(Trade trade) {
        if (trade == null) {
            return null;
        }
        
        return TradeDTO.builder()
                .id(trade.getId())
                .tradeId(trade.getTradeId())
                .orderId(trade.getOrderId())
                .symbol(trade.getSymbol())
                .tradeDate(trade.getTradeDate())
                .settlementDate(trade.getSettlementDate())
                .side(trade.getSide())
                .type(trade.getType())
                .status(trade.getStatus())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .totalValue(trade.getTotalValue())
                .executionVenue(trade.getExecutionVenue())
                .counterpartyId(trade.getCounterpartyId())
                .portfolioId(trade.getPortfolioId())
                .strategyId(trade.getStrategyId())
                .traderId(trade.getTraderId())
                .commissionFee(trade.getCommissionFee())
                .otherFees(trade.getOtherFees())
                .notes(trade.getNotes())
                .createdDate(trade.getCreatedDate())
                .lastModifiedDate(trade.getLastModifiedDate())
                .createdBy(trade.getCreatedBy())
                .lastModifiedBy(trade.getLastModifiedBy())
                .build();
    }
    
    /**
     * Convert TradeDTO to Trade entity
     */
    public Trade toEntity(TradeDTO tradeDTO) {
        if (tradeDTO == null) {
            return null;
        }
        
        return Trade.builder()
                .tradeId(tradeDTO.getTradeId())
                .orderId(tradeDTO.getOrderId())
                .symbol(tradeDTO.getSymbol())
                .tradeDate(tradeDTO.getTradeDate())
                .settlementDate(tradeDTO.getSettlementDate())
                .side(tradeDTO.getSide())
                .type(tradeDTO.getType())
                .status(tradeDTO.getStatus())
                .quantity(tradeDTO.getQuantity())
                .price(tradeDTO.getPrice())
                .totalValue(tradeDTO.getTotalValue())
                .executionVenue(tradeDTO.getExecutionVenue())
                .counterpartyId(tradeDTO.getCounterpartyId())
                .portfolioId(tradeDTO.getPortfolioId())
                .strategyId(tradeDTO.getStrategyId())
                .traderId(tradeDTO.getTraderId())
                .commissionFee(tradeDTO.getCommissionFee())
                .otherFees(tradeDTO.getOtherFees())
                .notes(tradeDTO.getNotes())
                .build();
    }
    
    /**
     * Update Trade entity from TradeDTO
     */
    public Trade updateEntityFromDto(TradeDTO tradeDTO, Trade trade) {
        if (tradeDTO == null) {
            return trade;
        }
        
        if (tradeDTO.getTradeId() != null) {
            trade.setTradeId(tradeDTO.getTradeId());
        }
        if (tradeDTO.getOrderId() != null) {
            trade.setOrderId(tradeDTO.getOrderId());
        }
        if (tradeDTO.getSymbol() != null) {
            trade.setSymbol(tradeDTO.getSymbol());
        }
        if (tradeDTO.getTradeDate() != null) {
            trade.setTradeDate(tradeDTO.getTradeDate());
        }
        if (tradeDTO.getSettlementDate() != null) {
            trade.setSettlementDate(tradeDTO.getSettlementDate());
        }
        if (tradeDTO.getSide() != null) {
            trade.setSide(tradeDTO.getSide());
        }
        if (tradeDTO.getType() != null) {
            trade.setType(tradeDTO.getType());
        }
        if (tradeDTO.getStatus() != null) {
            trade.setStatus(tradeDTO.getStatus());
        }
        if (tradeDTO.getQuantity() != null) {
            trade.setQuantity(tradeDTO.getQuantity());
        }
        if (tradeDTO.getPrice() != null) {
            trade.setPrice(tradeDTO.getPrice());
        }
        if (tradeDTO.getTotalValue() != null) {
            trade.setTotalValue(tradeDTO.getTotalValue());
        }
        if (tradeDTO.getExecutionVenue() != null) {
            trade.setExecutionVenue(tradeDTO.getExecutionVenue());
        }
        if (tradeDTO.getCounterpartyId() != null) {
            trade.setCounterpartyId(tradeDTO.getCounterpartyId());
        }
        if (tradeDTO.getPortfolioId() != null) {
            trade.setPortfolioId(tradeDTO.getPortfolioId());
        }
        if (tradeDTO.getStrategyId() != null) {
            trade.setStrategyId(tradeDTO.getStrategyId());
        }
        if (tradeDTO.getTraderId() != null) {
            trade.setTraderId(tradeDTO.getTraderId());
        }
        if (tradeDTO.getCommissionFee() != null) {
            trade.setCommissionFee(tradeDTO.getCommissionFee());
        }
        if (tradeDTO.getOtherFees() != null) {
            trade.setOtherFees(tradeDTO.getOtherFees());
        }
        if (tradeDTO.getNotes() != null) {
            trade.setNotes(tradeDTO.getNotes());
        }
        
        return trade;
    }
}
