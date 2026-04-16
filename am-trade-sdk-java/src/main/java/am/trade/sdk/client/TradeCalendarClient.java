package am.trade.sdk.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import am.trade.models.shared.TradeDetails;

@FeignClient(name = "am-trade-management-service", url = "${am.trade.management.url:http://am-trade-management-service:8080}", path = "/api/v1/trades")
public interface TradeCalendarClient {

    @GetMapping("/calendar/day")
    Map<String, List<TradeDetails>> getTradeDetailsByDay(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "portfolioId", required = false) String portfolioId);

    @GetMapping("/calendar/month")
    Map<String, List<TradeDetails>> getTradeDetailsByMonth(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam(value = "portfolioId", required = false) String portfolioId);

    @GetMapping("/calendar/quarter")
    Map<String, List<TradeDetails>> getTradeDetailsByQuarter(
            @RequestParam("year") int year,
            @RequestParam("quarter") int quarter,
            @RequestParam(value = "portfolioId", required = false) String portfolioId);

    @GetMapping("/calendar/financial-year")
    Map<String, List<TradeDetails>> getTradeDetailsByFinancialYear(
            @RequestParam("financialYear") int financialYear,
            @RequestParam(value = "portfolioId", required = false) String portfolioId);

    @GetMapping("/calendar/custom")
    Map<String, List<TradeDetails>> getTradeDetailsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "portfolioId", required = false) String portfolioId);

    @GetMapping("/portfolio-details/{portfolioId}")
    Page<TradeDetails> getTradeDetailsByPortfolio(
            @PathVariable("portfolioId") String portfolioId,
            Pageable pageable);
}
