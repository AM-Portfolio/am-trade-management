package am.trade.api.service.impl;

import am.trade.api.dto.summary.DailyPerformance;
import am.trade.api.dto.summary.TimingAnalysis;
import am.trade.api.dto.summary.TradePerformanceSummary;
import am.trade.api.service.TradeApiService;
import am.trade.api.service.TradePerformanceService;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradePerformanceServiceImplTest {

        @Mock
        private TradeApiService tradeApiService;

        private TradePerformanceService tradePerformanceService;

        @BeforeEach
        void setUp() {
                tradePerformanceService = new TradePerformanceServiceImpl(tradeApiService);
        }

        @Test
        void getPerformanceSummary_ShouldCalculateMetricsCorrectly() {
                // Arrange
                String portfolioId = "p1";
                LocalDate start = LocalDate.now().minusDays(10);
                LocalDate end = LocalDate.now();

                TradeDetails win = createTrade(TradeStatus.WIN, new BigDecimal("100.00"), 100, 110,
                                TradePositionType.LONG); // +1000
                TradeDetails loss = createTrade(TradeStatus.LOSS, new BigDecimal("100.00"), 50, 90,
                                TradePositionType.LONG); // -500
                TradeDetails be = createTrade(TradeStatus.BREAK_EVEN, new BigDecimal("100.00"), 10, 100,
                                TradePositionType.LONG); // 0

                List<TradeDetails> trades = Arrays.asList(win, loss, be);
                Page<TradeDetails> page = new PageImpl<>(trades);

                when(tradeApiService.getTradesByFilters(
                                anyList(), isNull(), anyList(), eq(start), eq(end), isNull(), any(Pageable.class)))
                                .thenReturn(page);

                // Act
                TradePerformanceSummary summary = tradePerformanceService.getPerformanceSummary(portfolioId, start,
                                end);

                // Assert
                assertEquals(3, summary.getTotalTrades());
                assertEquals(1, summary.getWinningTrades());
                assertEquals(1, summary.getLosingTrades());
                assertEquals(1, summary.getBreakEvenTrades());

                // Win: 1000, Loss: -500, BE: 0 -> Total: 500
                assertEquals(0, new BigDecimal("500").compareTo(summary.getTotalProfitLoss()));

                // Avg: 500 / 3 = 166.67
                assertEquals(0, new BigDecimal("166.67").compareTo(summary.getAverageProfitPerTrade()));

                // Win Rate: 1/3 = 33.33%
                // Note: Floating point comparison delta
                assertEquals(33.33, summary.getWinPercentage(), 0.01);

                // Profit Factor: 1000 / 500 = 2.0
                assertEquals(2.0, summary.getProfitFactor(), 0.01);

                // New Metrics Assertions
                // Win Trade: (110-100)*100 = 1000 profit. 1 trade. Avg Win = 1000.
                assertEquals(0, new BigDecimal("1000").compareTo(summary.getAverageWinAmount()));

                // Loss Trade: (90-100)*50 = -500 loss. 1 trade. Avg Loss = -500.
                assertEquals(0, new BigDecimal("-500").compareTo(summary.getAverageLossAmount()));

                // Holding Time: entry 10:00, exit 11:00 -> 1 hour for both
                assertEquals(1.0, summary.getAverageHoldingTimeWin(), 0.01);
                assertEquals(1.0, summary.getAverageHoldingTimeLoss(), 0.01);
        }

        @Test
        void getDailyPerformance_ShouldGroupByDateAndSort() {
                // Arrange
                String portfolioId = "p1";
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                TradeDetails t1 = createTrade(TradeStatus.WIN, new BigDecimal("100"), 1, 150, TradePositionType.LONG,
                                today); // +50
                TradeDetails t2 = createTrade(TradeStatus.LOSS, new BigDecimal("100"), 1, 80, TradePositionType.LONG,
                                today); // -20
                TradeDetails t3 = createTrade(TradeStatus.WIN, new BigDecimal("100"), 1, 200, TradePositionType.LONG,
                                yesterday); // +100

                List<TradeDetails> trades = Arrays.asList(t1, t2, t3);
                Page<TradeDetails> page = new PageImpl<>(trades);

                when(tradeApiService.getTradesByFilters(
                                anyList(), isNull(), anyList(), isNull(), isNull(), isNull(), any(Pageable.class)))
                                .thenReturn(page);

                // Act
                List<DailyPerformance> result = tradePerformanceService.getDailyPerformance(portfolioId, 5);

                // Assert
                assertEquals(2, result.size());

                // Yesterday (+100) should be first
                assertEquals(yesterday, result.get(0).getDate());
                assertEquals(0, new BigDecimal("100").compareTo(result.get(0).getTotalProfitLoss()));

                // Today (+50 - 20 = +30) should be second
                assertEquals(today, result.get(1).getDate());
                assertEquals(0, new BigDecimal("30").compareTo(result.get(1).getTotalProfitLoss()));
        }

        @Test
        void getTimingAnalysis_ShouldCalculateAllPatterns() {
                // Arrange
                String portfolioId = "p1";
                LocalDate monday = LocalDate.of(2024, 1, 1); // Monday, Jan 1st 2024
                LocalDate tuesday = LocalDate.of(2024, 2, 6); // Tuesday, Feb 6th 2024 (Different month)

                // Trade 1: Monday 10am, Win +100
                TradeDetails t1 = createTrade(TradeStatus.WIN, new BigDecimal("100"), 1, 200, TradePositionType.LONG,
                                monday);
                t1.getEntryInfo().setTimestamp(monday.atTime(10, 0));

                // Trade 2: Tuesday 2pm (14:00), Loss -50
                TradeDetails t2 = createTrade(TradeStatus.LOSS, new BigDecimal("100"), 1, 50, TradePositionType.LONG,
                                tuesday);
                t2.getEntryInfo().setTimestamp(tuesday.atTime(14, 0));
                t2.getExitInfo().setTimestamp(tuesday.atTime(15, 0));

                List<TradeDetails> trades = Arrays.asList(t1, t2);
                Page<TradeDetails> page = new PageImpl<>(trades);

                when(tradeApiService.getTradesByFilters(
                                anyList(), isNull(), anyList(), isNull(), isNull(), isNull(), any(Pageable.class)))
                                .thenReturn(page);

                // Act
                TimingAnalysis analysis = tradePerformanceService.getTimingAnalysis(portfolioId);

                // Assert - Hourly
                // 10am: 1 trade, +100
                // 14pm: 1 trade, -50
                assertEquals(2, analysis.getHourlyPerformance().size());
                assertEquals(10, analysis.getHourlyPerformance().get(0).getHour());
                assertEquals(0, new BigDecimal("100")
                                .compareTo(analysis.getHourlyPerformance().get(0).getTotalProfitLoss()));
                assertEquals(0, new BigDecimal("100")
                                .compareTo(analysis.getHourlyPerformance().get(0).getAverageWinAmount())); // Avg Win
                assertEquals(1.0, analysis.getHourlyPerformance().get(0).getAverageHoldingTime(), 0.01);

                assertEquals(14, analysis.getHourlyPerformance().get(1).getHour());
                assertEquals(0, new BigDecimal("-50")
                                .compareTo(analysis.getHourlyPerformance().get(1).getTotalProfitLoss()));
                assertEquals(0, new BigDecimal("-50")
                                .compareTo(analysis.getHourlyPerformance().get(1).getAverageLossAmount())); // Avg Loss
                assertEquals(1.0, analysis.getHourlyPerformance().get(1).getAverageHoldingTime(), 0.01);

                // Assert - Day of Week
                assertEquals(2, analysis.getDayOfWeekPerformance().size());

                // Assert - Monthly
                assertEquals(2, analysis.getMonthlyPerformance().size());

                // Assert - Yearly
                assertEquals(1, analysis.getYearlyPerformance().size());
                assertEquals(2024, analysis.getYearlyPerformance().get(0).getYear());
                assertEquals(0, new BigDecimal("50")
                                .compareTo(analysis.getYearlyPerformance().get(0).getTotalProfitLoss()));
                assertEquals(0, new BigDecimal("100")
                                .compareTo(analysis.getYearlyPerformance().get(0).getAverageWinAmount()));
                assertEquals(0, new BigDecimal("-50")
                                .compareTo(analysis.getYearlyPerformance().get(0).getAverageLossAmount()));
                assertEquals(1.0, analysis.getYearlyPerformance().get(0).getAverageHoldingTime(), 0.01);
        }

        private TradeDetails createTrade(TradeStatus status, BigDecimal entryPrice, int qty, double exitPriceVal,
                        TradePositionType type) {
                return createTrade(status, entryPrice, qty, exitPriceVal, type, LocalDate.now());
        }

        private TradeDetails createTrade(TradeStatus status, BigDecimal entryPrice, int qty, double exitPriceVal,
                        TradePositionType type, LocalDate date) {
                LocalDateTime entryTime = date.atTime(10, 0);
                LocalDateTime exitTime = date.atTime(11, 0);

                return TradeDetails.builder()
                                .status(status)
                                .symbol("TEST")
                                .instrumentInfo(InstrumentInfo.builder().symbol("TEST").build())
                                .tradePositionType(type)
                                .entryInfo(EntryExitInfo.builder()
                                                .price(entryPrice)
                                                .quantity(qty)
                                                .timestamp(entryTime)
                                                .build())
                                .exitInfo(EntryExitInfo.builder()
                                                .price(new BigDecimal(exitPriceVal))
                                                .quantity(qty)
                                                .timestamp(exitTime)
                                                .build())
                                .build();
        }
}
