package am.trade.kafka.mapper;

import am.trade.common.models.TradeModel;
import am.trade.common.models.enums.BrokerType;
import am.trade.common.models.enums.TradeType;
import am.trade.models.dto.TradeDTO;
import am.trade.models.enums.OrderSide;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between TradeDTO and TradeModel
 */
@Mapper(componentModel = "spring")
public interface TradeModelMapper {

    TradeModelMapper INSTANCE = Mappers.getMapper(TradeModelMapper.class);

    /**
     * Convert TradeDTO to TradeModel
     */
    @Mapping(target = "basicInfo", expression = "java(createBasicInfo(dto))")
    @Mapping(target = "instrumentInfo", expression = "java(createInstrumentInfo(dto))")
    @Mapping(target = "executionInfo", expression = "java(createExecutionInfo(dto))")
    @Mapping(target = "fnoInfo", ignore = true) // Not applicable for standard trades
    @Mapping(target = "charges", expression = "java(createCharges(dto))")
    @Mapping(target = "financials", expression = "java(createFinancials(dto))")
    TradeModel toTradeModel(TradeDTO dto);

    /**
     * Convert a list of TradeDTOs to a list of TradeModels
     */
    default List<TradeModel> toTradeModels(List<TradeDTO> tradeDTOs) {
        if (tradeDTOs == null) {
            return null;
        }
        return tradeDTOs.stream()
                .map(this::toTradeModel)
                .collect(Collectors.toList());
    }

    /**
     * Convert TradeModel to TradeDTO
     */
    // id field will be set by the service layer
    @Mapping(target = "tradeId", source = "basicInfo.tradeId")
    @Mapping(target = "orderId", source = "basicInfo.orderId")
    @Mapping(target = "symbol", source = "instrumentInfo.symbol")
    @Mapping(target = "tradeDate", source = "basicInfo.orderExecutionTime")
    @Mapping(target = "settlementDate", ignore = true) // Will be set by service layer if needed
    @Mapping(target = "side", source = "basicInfo.tradeType", qualifiedByName = "mapTradeTypeToOrderSide")
    @Mapping(target = "type", constant = "MARKET") // Default to MARKET, can be overridden
    @Mapping(target = "status", constant = "FILLED") // Default to FILLED, can be overridden
    @Mapping(target = "quantity", expression = "java(convertToDecimal(tradeModel.getExecutionInfo().getQuantity()))")
    @Mapping(target = "price", source = "executionInfo.price")
    @Mapping(target = "totalValue", source = "financials.turnover")
    @Mapping(target = "executionVenue", source = "instrumentInfo.exchange")
    @Mapping(target = "counterpartyId", constant = "BROKER") // Default value
    @Mapping(target = "portfolioId", ignore = true) // Will be set by service layer
    @Mapping(target = "strategyId", ignore = true) // Will be set by service layer
    @Mapping(target = "traderId", ignore = true) // Will be set by service layer
    @Mapping(target = "commissionFee", source = "charges.brokerage")
    @Mapping(target = "otherFees", source = "charges.totalTaxes")
    @Mapping(target = "notes", expression = "java(createTradeNotes(tradeModel))")
    // BaseDocument fields will be handled by Spring Data auditing
    TradeDTO toTradeDTO(TradeModel tradeModel);

    /**
     * Convert a list of TradeModels to a list of TradeDTOs
     */
    default List<TradeDTO> toTradeDTOs(List<TradeModel> tradeModels) {
        if (tradeModels == null) {
            return null;
        }
        return tradeModels.stream()
                .map(this::toTradeDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to create BasicInfo
     */
    default TradeModel.BasicInfo createBasicInfo(TradeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return TradeModel.BasicInfo.builder()
                .tradeId(dto.getTradeId())
                .orderId(dto.getOrderId())
                .tradeDate(dto.getTradeDate() != null ? dto.getTradeDate().toLocalDate() : null)
                .orderExecutionTime(dto.getTradeDate())
                .brokerType(BrokerType.valueOf("SYSTEM")) // Default value
                .tradeType(mapOrderSideToTradeType(dto.getSide()))
                .build();
    }

    /**
     * Helper method to create InstrumentInfo
     */
    default TradeModel.InstrumentInfo createInstrumentInfo(TradeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return TradeModel.InstrumentInfo.builder()
                .symbol(dto.getSymbol())
                .exchange(dto.getExecutionVenue())
                .build();
    }

    /**
     * Helper method to create ExecutionInfo
     */
    default TradeModel.ExecutionInfo createExecutionInfo(TradeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return TradeModel.ExecutionInfo.builder()
                .quantity(dto.getQuantity() != null ? dto.getQuantity().intValue() : null)
                .price(dto.getPrice())
                .build();
    }

    /**
     * Helper method to create Charges
     */
    default TradeModel.Charges createCharges(TradeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return TradeModel.Charges.builder()
                .brokerage(dto.getCommissionFee())
                .totalTaxes(dto.getOtherFees())
                .build();
    }

    /**
     * Helper method to create Financials
     */
    default TradeModel.Financials createFinancials(TradeDTO dto) {
        if (dto == null) {
            return null;
        }
        
        BigDecimal netAmount = null;
        if (dto.getTotalValue() != null && dto.getCommissionFee() != null && dto.getOtherFees() != null) {
            netAmount = dto.getTotalValue().subtract(dto.getCommissionFee()).subtract(dto.getOtherFees());
        }
        
        return TradeModel.Financials.builder()
                .turnover(dto.getTotalValue())
                .netAmount(netAmount)
                .build();
    }

    /**
     * Helper method to create trade notes
     */
    default List<String> createTradeNotes(TradeModel tradeModel) {
        String note = String.format("Processed from %s trade", 
            tradeModel.getBasicInfo() != null ? tradeModel.getBasicInfo().getBrokerType() : "Unknown");
        return List.of(note);
    }

    /**
     * Helper method to convert Integer to BigDecimal
     */
    default BigDecimal convertToDecimal(Integer value) {
        return value != null ? new BigDecimal(value) : null;
    }

    /**
     * Helper method to map OrderSide to TradeType
     */
    default TradeType mapOrderSideToTradeType(OrderSide side) {
        if (side == null) {
            return TradeType.BUY; // Default to BUY if not specified
        }
        return side == OrderSide.BUY ? TradeType.BUY : TradeType.SELL;
    }

    /**
     * Helper method to map TradeType to OrderSide
     */
    @Named("mapTradeTypeToOrderSide")
    default OrderSide mapTradeTypeToOrderSide(TradeType tradeType) {
        if (tradeType == null) {
            return OrderSide.BUY; // Default to BUY if not specified
        }
        return tradeType == TradeType.BUY ? OrderSide.BUY : OrderSide.SELL;
    }
}
