package am.trade.api.service;

import am.trade.api.client.PortfolioServiceClient;
import am.trade.api.client.PortfolioServiceClient.RemoteEquity;
import am.trade.api.client.PortfolioServiceClient.RemotePortfolio;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * When trade Mongo has no portfolios for the JWT user (Demo would be injected),
 * pull broker portfolios from am-portfolio over HTTP with the same Bearer token
 * and upsert them. Covers missed Kafka {@code am-portfolio-update} fan-out.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioFanoutCatchupService {

    private final PortfolioServiceClient portfolioServiceClient;
    private final PortfolioService portfolioService;
    private final TradeDetailsService tradeDetailsService;
    private final TradeProcessingService tradeProcessingService;

    /**
     * @return number of portfolios created/updated in trade DB
     */
    public int catchUpFromPortfolioService(String ownerId) {
        String bearer = currentAuthorizationHeader();
        if (bearer == null) {
            log.debug("No Authorization header — skip portfolio catch-up for {}", ownerId);
            return 0;
        }

        List<RemotePortfolio> remote = portfolioServiceClient.listPortfoliosForUser(bearer);
        if (remote.isEmpty()) {
            return 0;
        }

        int touched = 0;
        for (RemotePortfolio rp : remote) {
            if (!ownerId.equals(rp.ownerId())) {
                log.warn("Skipping remote portfolio {} owned by {} (request owner {})",
                        rp.portfolioId(), rp.ownerId(), ownerId);
                continue;
            }
            if (upsertPortfolio(rp)) {
                touched++;
            }
            upsertBaselineTrades(rp);
        }
        if (touched > 0) {
            log.info("Caught up {} portfolio(s) from am-portfolio for ownerId={}", touched, ownerId);
        }
        return touched;
    }

    private boolean upsertPortfolio(RemotePortfolio rp) {
        Optional<PortfolioModel> existing = portfolioService.findByPortfolioId(rp.portfolioId());
        if (existing.isPresent()) {
            return false;
        }
        String name = (rp.name() != null && !rp.name().isBlank())
                ? rp.name()
                : (rp.brokerType() != null ? rp.brokerType() : "Imported Portfolio");
        PortfolioModel portfolio = PortfolioModel.builder()
                .portfolioId(rp.portfolioId())
                .ownerId(rp.ownerId())
                .name(name)
                .active(true)
                .build();
        portfolioService.savePortfolio(portfolio);
        log.info("Catch-up created trade portfolio {} ({})", rp.portfolioId(), name);
        return true;
    }

    private void upsertBaselineTrades(RemotePortfolio rp) {
        if (rp.equities() == null || rp.equities().isEmpty()) {
            return;
        }
        List<TradeDetails> existing = tradeDetailsService.findModelsByPortfolioId(rp.portfolioId());
        List<TradeDetails> neu = new ArrayList<>();
        for (RemoteEquity equity : rp.equities()) {
            String symbol = equity.symbol().toUpperCase();
            boolean exists = existing.stream().anyMatch(t -> symbol.equalsIgnoreCase(t.getSymbol()));
            if (exists) {
                continue;
            }
            TradeDetails trade = new TradeDetails();
            trade.setTradeId(UUID.randomUUID().toString());
            trade.setPortfolioId(rp.portfolioId());
            trade.setUserId(rp.ownerId());
            trade.setSymbol(symbol);
            trade.setStatus(TradeStatus.OPEN);
            trade.setTradePositionType(TradePositionType.LONG);
            trade.setStrategy("Imported Holding");

            InstrumentInfo instrumentInfo = new InstrumentInfo();
            instrumentInfo.setSymbol(symbol);
            instrumentInfo.setIsin(equity.isin());
            trade.setInstrumentInfo(instrumentInfo);

            EntryExitInfo entryInfo = new EntryExitInfo();
            entryInfo.setQuantity(equity.quantity().intValue());
            entryInfo.setPrice(equity.avgBuyingPrice() != null
                    ? BigDecimal.valueOf(equity.avgBuyingPrice())
                    : BigDecimal.ZERO);
            entryInfo.setTotalValue(equity.investmentValue() != null
                    ? BigDecimal.valueOf(equity.investmentValue())
                    : entryInfo.getPrice().multiply(BigDecimal.valueOf(entryInfo.getQuantity())));
            entryInfo.setTimestamp(LocalDateTime.now());
            trade.setEntryInfo(entryInfo);
            if (equity.currentPrice() != null) {
                trade.setCurrentPrice(BigDecimal.valueOf(equity.currentPrice()));
            }
            neu.add(trade);
        }
        if (neu.isEmpty()) {
            return;
        }
        try {
            List<TradeDetails> saved = tradeDetailsService.saveAllTradeDetails(neu);
            existing = new ArrayList<>(existing);
            existing.addAll(saved);
            tradeProcessingService.processTradeDetailsWithObjects(existing, rp.portfolioId(), rp.ownerId());
            log.info("Catch-up created {} baseline trades for portfolio {}", saved.size(), rp.portfolioId());
        } catch (Exception e) {
            log.error("Catch-up trade upsert failed for {}: {}", rp.portfolioId(), e.getMessage(), e);
        }
    }

    private static String currentAuthorizationHeader() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            return request != null ? request.getHeader("Authorization") : null;
        } catch (Exception e) {
            return null;
        }
    }
}
