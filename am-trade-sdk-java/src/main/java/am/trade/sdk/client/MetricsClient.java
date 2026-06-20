package am.trade.sdk.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import am.trade.models.dto.MetricsFilterRequest;
import am.trade.models.dto.MetricsResponse;

@FeignClient(name = "am-trade-management-service", url = "${am.trade.management.url:http://am-trade-management-service:8080}", path = "/api/v1/metrics")
public interface MetricsClient {

    @PostMapping
    MetricsResponse getTradeMetrics(@RequestBody MetricsFilterRequest filterRequest);

    @GetMapping("/types")
    List<String> getAvailableMetricTypes();

    @GetMapping("/compare")
    List<MetricsResponse> compareMetrics(
            @RequestParam("portfolioIds") List<String> portfolioIds,
            @RequestParam("metricTypes") Set<String> metricTypes,
            @RequestParam("firstPeriodStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstPeriodStart,
            @RequestParam("firstPeriodEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstPeriodEnd,
            @RequestParam("secondPeriodStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate secondPeriodStart,
            @RequestParam("secondPeriodEnd") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate secondPeriodEnd);
    
    @GetMapping("/trends")
    Map<String, List<MetricsResponse>> getMetricsTrends(
            @RequestParam("portfolioIds") List<String> portfolioIds,
            @RequestParam("metricTypes") Set<String> metricTypes,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam("interval") String interval);
}
