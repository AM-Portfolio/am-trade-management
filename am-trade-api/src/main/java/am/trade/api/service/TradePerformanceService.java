package am.trade.api.service;

import am.trade.api.dto.summary.DailyPerformance;
import am.trade.api.dto.summary.TimingAnalysis;
import am.trade.api.dto.summary.TradePerformanceSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for trade performance analysis operations
 */
public interface TradePerformanceService {

    /**
     * Get a general performance summary for a portfolio
     *
     * @param portfolioId The portfolio ID
     * @param startDate   Optional start date
     * @param endDate     Optional end date
     * @return TradePerformanceSummary
     */
    TradePerformanceSummary getPerformanceSummary(String portfolioId, LocalDate startDate, LocalDate endDate);

    /**
     * Get daily performance analysis, identifying best/worst days
     *
     * @param portfolioId The portfolio ID
     * @param limit       Number of days to return
     * @return List of DailyPerformance
     */
    List<DailyPerformance> getDailyPerformance(String portfolioId, int limit);

    /**
     * Get timing analysis based on trade entry/exit times
     *
     * @param portfolioId The portfolio ID
     * @return TimingAnalysis
     */
    TimingAnalysis getTimingAnalysis(String portfolioId);
}
