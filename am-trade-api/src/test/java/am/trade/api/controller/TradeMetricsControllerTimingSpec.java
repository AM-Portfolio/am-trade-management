package am.trade.api.controller;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.service.TradeMetricsService;
import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDistributionMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TradeMetricsController.class)
@ContextConfiguration(classes = TradeMetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
class TradeMetricsControllerTimingSpec {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeMetricsService tradeMetricsService;

    @Test
    void postMetrics_returnsPerformanceAndDistributionFields() throws Exception {
        MetricsResponse response = sampleResponse();
        when(tradeMetricsService.getMetrics(any(MetricsFilterRequest.class))).thenReturn(response);

        String body = """
                {
                  "portfolioIds": ["p1"],
                  "dateRange": { "startDate": "2015-01-01", "endDate": "2026-09-15" },
                  "metricTypes": ["PERFORMANCE", "DISTRIBUTION"],
                  "tradeCharacteristics": { "holdingStyle": "SCALPER" }
                }
                """;

        mockMvc.perform(post("/v1/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTradesCount").value(2))
                .andExpect(jsonPath("$.performanceMetrics.totalProfitLoss").value(50))
                .andExpect(jsonPath("$.performanceMetrics.winningTradesCount").value(1))
                .andExpect(jsonPath("$.performanceMetrics.losingTradesCount").value(1))
                .andExpect(jsonPath("$.performanceMetrics.averageHoldingTimeMinutes").value(15))
                .andExpect(jsonPath("$.distributionMetrics.bestSessionKey")
                        .value("SESSION_0915_1100"))
                .andExpect(jsonPath("$.distributionMetrics.avgHoldMinutesBySession.SESSION_0915_1100")
                        .value(15))
                .andExpect(jsonPath("$.distributionMetrics.riskRewardBySession.SESSION_0915_1100")
                        .value(2));

        verify(tradeMetricsService).getMetrics(any(MetricsFilterRequest.class));
    }

    @Test
    void postMetrics_illegalArgument_returns400() throws Exception {
        when(tradeMetricsService.getMetrics(any(MetricsFilterRequest.class)))
                .thenThrow(new IllegalArgumentException("portfolioIds required"));

        mockMvc.perform(post("/v1/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"portfolioIds\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMetricTypes_returnsOk() throws Exception {
        when(tradeMetricsService.getAvailableMetricTypes())
                .thenReturn(List.of("PERFORMANCE", "DISTRIBUTION", "RISK"));

        mockMvc.perform(get("/v1/metrics/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("PERFORMANCE"))
                .andExpect(jsonPath("$[1]").value("DISTRIBUTION"));
    }

    private static MetricsResponse sampleResponse() {
        Map<String, BigDecimal> hold = new LinkedHashMap<>();
        hold.put("SESSION_0915_1100", new BigDecimal("15"));
        Map<String, BigDecimal> rr = new LinkedHashMap<>();
        rr.put("SESSION_0915_1100", new BigDecimal("2"));

        TradeDistributionMetrics dist = TradeDistributionMetrics.builder()
                .avgHoldMinutesBySession(hold)
                .riskRewardBySession(rr)
                .bestSessionKey("SESSION_0915_1100")
                .bestSessionAvgPnl(new BigDecimal("50"))
                .timezoneNote("entry_local_as_stored")
                .build();

        PerformanceMetrics perf = PerformanceMetrics.builder()
                .totalProfitLoss(new BigDecimal("50"))
                .winRate(new BigDecimal("50"))
                .winningTradesCount(1)
                .losingTradesCount(1)
                .averageHoldingTimeMinutes(new BigDecimal("15"))
                .build();

        return MetricsResponse.builder()
                .portfolioIds(List.of("p1"))
                .startDate(LocalDate.of(2015, 1, 1))
                .endDate(LocalDate.of(2026, 9, 15))
                .totalTradesCount(2)
                .performanceMetrics(perf)
                .distributionMetrics(dist)
                .build();
    }
}
