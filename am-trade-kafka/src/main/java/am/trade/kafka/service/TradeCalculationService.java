package am.trade.kafka.service;

import am.trade.common.models.TradeSummary;
import am.trade.dashboard.service.TradeMetricsCalculationService;
import am.trade.kafka.producer.TradeKafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeCalculationService {

    private final TradeMetricsCalculationService metricsCalculationService;
    private final TradeKafkaProducerService producerService;

    public void processCalculation(String userId, String portfolioId) {
        log.info("[TradeCalc] Starting calculation for User: {}, Portfolio: {}", userId, portfolioId);
        try {
            if (portfolioId == null || portfolioId.isEmpty()) {
                log.warn("[TradeCalc] No portfolioId provided for User: {}. Skipping.", userId);
                return;
            }

            // 1. Calculate all metrics using the existing dashboard service
            List<String> portfolioIds = Collections.singletonList(portfolioId);
            TradeSummary summary = metricsCalculationService.calculateAllMetrics(portfolioIds);

            // 2. Wrap the data into a Map so the Gateway can easily read it
            // Important: The Gateway expects a "userId" field at the top level
            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("portfolioId", portfolioId);
            payload.put("data", summary);

            // 3. Send to Kafka via our Producer
            producerService.sendTradeUpdate(userId, payload);

            log.info("[TradeCalc] ✅ Successfully calculated and published for User: {}", userId);
        } catch (Exception e) {
            log.error("[TradeCalc] ❌ Error calculating metrics for User: {}", userId, e);
        }
    }
}
