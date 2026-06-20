package am.trade.sdk.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import am.trade.models.shared.TradeDetails;
import am.trade.models.shared.enums.TradeStatus;

@FeignClient(name = "am-trade-management-service", url = "${am.trade.management.url:http://am-trade-management-service:8080}", path = "/api/v1/trades")
public interface TradeClient {
    
    @GetMapping("/details/portfolio/{portfolioId}")
    List<TradeDetails> getTradeDetailsByPortfolioAndSymbols(
            @PathVariable("portfolioId") String portfolioId,
            @RequestParam(value = "symbols", required = false) List<String> symbols);
    
    @PostMapping("/details")
    TradeDetails addTrade(@RequestBody TradeDetails tradeDetails);
    
    @PutMapping("/details/{tradeId}")
    TradeDetails updateTrade(
            @PathVariable("tradeId") String tradeId,
            @RequestBody TradeDetails tradeDetails);
    
    @GetMapping("/filter")
    Page<TradeDetails> getTradesByFilters(
            @RequestParam(value = "portfolioIds", required = false) List<String> portfolioIds,
            @RequestParam(value = "symbols", required = false) List<String> symbols,
            @RequestParam(value = "statuses", required = false) List<TradeStatus> statuses,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "strategies", required = false) List<String> strategies,
            Pageable pageable);
    
    @PostMapping("/details/batch")
    List<TradeDetails> addOrUpdateTrades(@RequestBody List<TradeDetails> tradeDetailsList);
    
    @PostMapping("/details/by-ids")
    List<TradeDetails> getTradeDetailsByTradeIds(@RequestBody List<String> tradeIds);
}
