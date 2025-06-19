package am.trade.services.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import am.trade.models.document.Trade;
import am.trade.models.enums.OrderStatus;
import am.trade.models.repository.TradeRepository;
import am.trade.services.dto.TradeDTO;
import am.trade.services.mapper.TradeMapper;
import am.trade.services.service.TradeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of TradeService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;
    
    @Override
    @Transactional
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public TradeDTO createTrade(TradeDTO tradeDTO) {
        log.info("Creating new trade for symbol: {}", tradeDTO.getSymbol());
        Trade trade = tradeMapper.toEntity(tradeDTO);
        Trade savedTrade = tradeRepository.save(trade);
        return tradeMapper.toDto(savedTrade);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public TradeDTO updateTrade(String id, TradeDTO tradeDTO) {
        log.info("Updating trade with ID: {}", id);
        return tradeRepository.findById(id)
                .map(existingTrade -> {
                    Trade tradeToUpdate = tradeMapper.toEntity(tradeDTO);
                    tradeToUpdate.setId(id);
                    tradeToUpdate.setCreatedDate(existingTrade.getCreatedDate());
                    Trade updatedTrade = tradeRepository.save(tradeToUpdate);
                    return tradeMapper.toDto(updatedTrade);
                })
                .orElseThrow(() -> new RuntimeException("Trade not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        log.debug("Checking if trade exists with ID: {}", id);
        return tradeRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeDTO> findAllTrades(Pageable pageable) {
        log.debug("Fetching all trades with pagination");
        return tradeRepository.findAll(pageable)
                .map(tradeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeDTO> findByPortfolioId(String portfolioId, Pageable pageable) {
        log.debug("Fetching trades for portfolio ID: {}", portfolioId);
        return tradeRepository.findByPortfolioId(portfolioId, pageable)
                .map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Optional<TradeDTO> findById(String id) {
        log.debug("Finding trade by ID: {}", id);
        return tradeRepository.findById(id).map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Optional<TradeDTO> findByTradeId(String tradeId) {
        log.debug("Finding trade by trade ID: {}", tradeId);
        return tradeRepository.findByTradeId(tradeId).map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Optional<TradeDTO> findByOrderId(String orderId) {
        log.debug("Finding trade by order ID: {}", orderId);
        return tradeRepository.findByOrderId(orderId).map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findBySymbol(String symbol) {
        log.debug("Finding trades by symbol: {}", symbol);
        return tradeRepository.findBySymbol(symbol).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findByTradeDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trades between dates: {} and {}", startDate, endDate);
        return tradeRepository.findByTradeDateBetween(startDate, endDate).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trades by settlement date between: {} and {}", startDate, endDate);
        return tradeRepository.findBySettlementDateBetween(startDate, endDate).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findByStatus(OrderStatus status) {
        log.debug("Finding trades by status: {}", status);
        return tradeRepository.findByStatus(status).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findByPortfolioId(String portfolioId) {
        log.debug("Finding trades by portfolio ID: {}", portfolioId);
        return tradeRepository.findByPortfolioId(portfolioId).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findByTraderId(String traderId) {
        log.debug("Finding trades by trader ID: {}", traderId);
        return tradeRepository.findByTraderId(traderId).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public List<TradeDTO> findBySymbolAndTradeDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trades by symbol: {} between dates: {} and {}", symbol, startDate, endDate);
        return tradeRepository.findBySymbolAndTradeDateBetween(symbol, startDate, endDate).stream()
                .map(tradeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Page<TradeDTO> findBySymbol(String symbol, Pageable pageable) {
        log.debug("Finding trades by symbol: {} with pagination", symbol);
        return tradeRepository.findBySymbol(symbol, pageable).map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Page<TradeDTO> findByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Finding trades by status: {} with pagination", status);
        return tradeRepository.findByStatus(status, pageable).map(tradeMapper::toDto);
    }

    @Override
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public Page<TradeDTO> findByPortfolioIdAndTradeDateBetween(String portfolioId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Finding trades by portfolio ID: {} between dates: {} and {} with pagination", portfolioId, startDate, endDate);
        return tradeRepository.findByPortfolioIdAndTradeDateBetween(portfolioId, startDate, endDate, pageable)
                .map(tradeMapper::toDto);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public void deleteTrade(String id) {
        log.info("Deleting trade with ID: {}", id);
        tradeRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "tradeService")
    @Retry(name = "tradeService")
    public TradeDTO updateTradeStatus(String id, OrderStatus status) {
        log.info("Updating trade status to {} for trade with ID: {}", status, id);
        return tradeRepository.findById(id)
                .map(trade -> {
                    trade.setStatus(status);
                    return tradeMapper.toDto(tradeRepository.save(trade));
                })
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + id));
    }
}
