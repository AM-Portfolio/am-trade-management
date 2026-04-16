package am.trade.sdk.client;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.PortfolioSummaryDTO;

@FeignClient(name = "am-trade-management-service", url = "${am.trade.management.url:http://am-trade-management-service:8080}", path = "/api/v1/portfolio-summary")
public interface PortfolioSummaryClient {

    @GetMapping("/{portfolioId}")
    PortfolioModel getPortfolioSummary(@PathVariable("portfolioId") String portfolioId);

    @GetMapping("/{portfolioId}/asset-allocation")
    List<AssetAllocation> getAssetAllocation(@PathVariable("portfolioId") String portfolioId);

    @GetMapping("/{portfolioId}/performance")
    Map<LocalDate, Double> getPortfolioPerformance(
            @PathVariable("portfolioId") String portfolioId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @GetMapping("/compare")
    Map<String, PortfolioModel> comparePortfolios(@RequestParam("portfolioIds") List<String> portfolioIds);
    
    @GetMapping("/by-owner/{ownerId}")
    List<PortfolioSummaryDTO> getPortfolioSummariesByOwnerId(@PathVariable("ownerId") String ownerId);
}
