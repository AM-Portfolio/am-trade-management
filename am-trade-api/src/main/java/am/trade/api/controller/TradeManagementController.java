package am.trade.api.controller;

import am.trade.common.models.TradeDetails;

import am.trade.api.service.TradeManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for trade management operations
 */
@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Slf4j
public class TradeManagementController {

    private final TradeManagementService tradeManagementService;

    /**
     * Get trade details for a specific day
     */
    @GetMapping("/calendar/day")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for date: {} and portfolio: {}", date, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByDay(date, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific month
     */
    @GetMapping("/calendar/month")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByMonth(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for year: {}, month: {} and portfolio: {}", year, month, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByMonth(year, month, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific quarter
     */
    @GetMapping("/calendar/quarter")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByQuarter(
            @RequestParam int year,
            @RequestParam int quarter,
            @RequestParam(required = false) String portfolioId) {
        
        if (quarter < 1 || quarter > 4) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Fetching trade details for year: {}, quarter: {} and portfolio: {}", year, quarter, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByQuarter(year, quarter, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific financial year
     */
    @GetMapping("/calendar/financial-year")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByFinancialYear(
            @RequestParam int financialYear,
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for financial year: {} and portfolio: {}", financialYear, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByFinancialYear(financialYear, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a custom date range
     */
    @GetMapping("/calendar/custom")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String portfolioId) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Fetching trade details from {} to {} for portfolio: {}", startDate, endDate, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByDateRange(startDate, endDate, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get paginated trade details for a specific portfolio
     */
    @GetMapping("/portfolio-details/{portfolioId}")
    public ResponseEntity<Page<TradeDetails>> getTradeDetailsByPortfolio(
            @PathVariable String portfolioId,
            Pageable pageable) {
        
        log.info("Fetching paginated trade details for portfolio: {}", portfolioId);
        Page<TradeDetails> tradeDetails = tradeManagementService.getTradeDetailsByPortfolio(portfolioId, pageable);
        return ResponseEntity.ok(tradeDetails);
    }
}
