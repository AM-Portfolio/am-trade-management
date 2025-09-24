package am.trade.analytics.controller;

import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayRequest;
import am.trade.analytics.model.dto.TradeReplayResponse;
import am.trade.analytics.service.TradeReplayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for trade replay operations
 * Provides endpoints for creating and retrieving trade replay analyses
 */
@RestController
@RequestMapping("/api/v1/analytics/trade-replays")
@RequiredArgsConstructor
@Slf4j
public class TradeReplayController {

    private final TradeReplayService tradeReplayService;

    /**
     * Create a new trade replay analysis
     * 
     * @param request The trade replay request
     * @return The created trade replay response
     */
    @PostMapping
    public ResponseEntity<TradeReplayResponse> createTradeReplay(@Valid @RequestBody TradeReplayRequest request) {
        log.info("Received request to create trade replay for symbol: {}", request.getSymbol());
        TradeReplayResponse response = tradeReplayService.createTradeReplay(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a trade replay by its ID
     * 
     * @param replayId The ID of the trade replay
     * @return The trade replay if found
     */
    @GetMapping("/{replayId}")
    public ResponseEntity<TradeReplay> getTradeReplayById(@PathVariable String replayId) {
        log.info("Fetching trade replay with ID: {}", replayId);
        return tradeReplayService.getTradeReplayById(replayId)
                .map(replay -> new ResponseEntity<>(replay, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Find trade replays by symbol
     * 
     * @param symbol The stock symbol
     * @return List of trade replays for the given symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<TradeReplay>> findTradeReplaysBySymbol(@PathVariable String symbol) {
        log.info("Finding trade replays for symbol: {}", symbol);
        List<TradeReplay> replays = tradeReplayService.findTradeReplaysBySymbol(symbol);
        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    /**
     * Find trade replays by date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of trade replays within the date range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<TradeReplay>> findTradeReplaysByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Finding trade replays between {} and {}", startDate, endDate);
        List<TradeReplay> replays = tradeReplayService.findTradeReplaysByDateRange(startDate, endDate);
        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    /**
     * Find trade replays by portfolio ID
     * 
     * @param portfolioId The portfolio ID
     * @return List of trade replays for the given portfolio
     */
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<List<TradeReplay>> findTradeReplaysByPortfolioId(@PathVariable String portfolioId) {
        log.info("Finding trade replays for portfolio ID: {}", portfolioId);
        List<TradeReplay> replays = tradeReplayService.findTradeReplaysByPortfolioId(portfolioId);
        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    /**
     * Find trade replays by strategy ID
     * 
     * @param strategyId The strategy ID
     * @return List of trade replays for the given strategy
     */
    @GetMapping("/strategy/{strategyId}")
    public ResponseEntity<List<TradeReplay>> findTradeReplaysByStrategyId(@PathVariable String strategyId) {
        log.info("Finding trade replays for strategy ID: {}", strategyId);
        List<TradeReplay> replays = tradeReplayService.findTradeReplaysByStrategyId(strategyId);
        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    /**
     * Find trade replays by original trade ID
     * 
     * @param tradeId The original trade ID
     * @return List of trade replays for the given trade
     */
    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<TradeReplay>> findTradeReplaysByOriginalTradeId(@PathVariable String tradeId) {
        log.info("Finding trade replays for original trade ID: {}", tradeId);
        List<TradeReplay> replays = tradeReplayService.findTradeReplaysByOriginalTradeId(tradeId);
        return new ResponseEntity<>(replays, HttpStatus.OK);
    }

    /**
     * Delete a trade replay by its ID
     * 
     * @param replayId The ID of the trade replay to delete
     * @return No content if deleted successfully
     */
    @DeleteMapping("/{replayId}")
    public ResponseEntity<Void> deleteTradeReplay(@PathVariable String replayId) {
        log.info("Deleting trade replay with ID: {}", replayId);
        boolean deleted = tradeReplayService.deleteTradeReplay(replayId);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
