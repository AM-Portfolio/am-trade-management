package am.trade.sdk.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Transform between user DTOs and internal models.
 * Ensures users NEVER see internal fields.
 */
public class DTOTransformer {

    /**
     * Transform internal trade model to user DTO.
     *
     * @param internalTrade Internal trade map
     * @return TradeDTO.TradeResponse with only safe fields
     */
    public static TradeDTO.TradeResponse toTradeResponse(Map<String, Object> internalTrade) {
        if (internalTrade == null) {
            return null;
        }

        return TradeDTO.TradeResponse.builder()
                .id((String) internalTrade.get("id"))
                .portfolioId((String) internalTrade.get("portfolio_id"))
                .symbol((String) internalTrade.get("symbol"))
                .tradeType((String) internalTrade.get("trade_type"))
                .quantity(((Number) internalTrade.get("quantity")).doubleValue())
                .entryPrice(((Number) internalTrade.get("entry_price")).doubleValue())
                .entryDate((java.time.LocalDateTime) internalTrade.get("entry_date"))
                .exitPrice(internalTrade.get("exit_price") != null ?
                        ((Number) internalTrade.get("exit_price")).doubleValue() : null)
                .exitDate((java.time.LocalDateTime) internalTrade.get("exit_date"))
                .status((String) internalTrade.get("status"))
                .notes((String) internalTrade.get("notes"))
                .pnl(internalTrade.get("pnl") != null ?
                        ((Number) internalTrade.get("pnl")).doubleValue() : null)
                .pnlPercentage(internalTrade.get("pnl_percentage") != null ?
                        ((Number) internalTrade.get("pnl_percentage")).doubleValue() : null)
                .build();

        // NEVER expose these internal fields:
        // internal_id, transaction_cost, fee_paid, tax_impact, margin_used, metadata
    }

    /**
     * Transform internal portfolio model to user DTO.
     *
     * @param internalPortfolio Internal portfolio map
     * @return PortfolioDTO.PortfolioResponse with only safe fields
     */
    public static PortfolioDTO.PortfolioResponse toPortfolioResponse(Map<String, Object> internalPortfolio) {
        if (internalPortfolio == null) {
            return null;
        }

        return PortfolioDTO.PortfolioResponse.builder()
                .id((String) internalPortfolio.get("id"))
                .name((String) internalPortfolio.get("name"))
                .description((String) internalPortfolio.get("description"))
                .initialCapital(((Number) internalPortfolio.get("initial_capital")).doubleValue())
                .currentValue(((Number) internalPortfolio.get("current_value")).doubleValue())
                .totalPnl(((Number) internalPortfolio.get("total_pnl")).doubleValue())
                .totalPnlPercentage(((Number) internalPortfolio.get("total_pnl_percentage")).doubleValue())
                .totalTrades((Integer) internalPortfolio.getOrDefault("total_trades", 0))
                .activeTrades((Integer) internalPortfolio.getOrDefault("active_trades", 0))
                .build();
    }

    /**
     * Convert create request DTO to internal format.
     *
     * @param dto Trade create request
     * @return Map for backend processing
     */
    public static Map<String, Object> fromTradeCreateRequest(TradeDTO.TradeCreateRequest dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("portfolio_id", dto.getPortfolioId());
        map.put("symbol", dto.getSymbol());
        map.put("trade_type", dto.getTradeType());
        map.put("quantity", dto.getQuantity());
        map.put("entry_price", dto.getEntryPrice());
        map.put("entry_date", dto.getEntryDate());
        map.put("notes", dto.getNotes());
        return map;
    }

    /**
     * Convert update request DTO to internal format.
     *
     * @param dto Trade update request
     * @return Map for backend processing
     */
    public static Map<String, Object> fromTradeUpdateRequest(TradeDTO.TradeUpdateRequest dto) {
        Map<String, Object> map = new HashMap<>();
        if (dto.getExitPrice() != null) {
            map.put("exit_price", dto.getExitPrice());
        }
        if (dto.getExitDate() != null) {
            map.put("exit_date", dto.getExitDate());
        }
        if (dto.getStatus() != null) {
            map.put("status", dto.getStatus());
        }
        if (dto.getNotes() != null) {
            map.put("notes", dto.getNotes());
        }
        return map;
    }

    /**
     * Convert portfolio create request DTO to internal format.
     *
     * @param dto Portfolio create request
     * @return Map for backend processing
     */
    public static Map<String, Object> fromPortfolioCreateRequest(PortfolioDTO.PortfolioCreateRequest dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", dto.getName());
        map.put("description", dto.getDescription());
        map.put("initial_capital", dto.getInitialCapital());
        return map;
    }

    /**
     * Convert journal entry create request DTO to internal format.
     *
     * @param dto Journal entry create request
     * @return Map for backend processing
     */
    public static Map<String, Object> fromJournalCreateRequest(ApiDTO.JournalEntryCreateRequest dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("trade_id", dto.getTradeId());
        map.put("title", dto.getTitle());
        map.put("content", dto.getContent());
        map.put("tags", dto.getTags());
        return map;
    }

    /**
     * Convert filter create request DTO to internal format.
     *
     * @param dto Filter create request
     * @return Map for backend processing
     */
    public static Map<String, Object> fromFilterCreateRequest(ApiDTO.FilterCreateRequest dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", dto.getName());
        map.put("criteria", dto.getCriteria());
        map.put("description", dto.getDescription());
        return map;
    }
}
