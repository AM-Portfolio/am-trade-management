package am.trade.persistence.repository;

import am.trade.models.enums.TradeStatus;
import am.trade.persistence.entity.TradeDetailsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeDetailsRepositoryCustom {
    Page<TradeDetailsEntity> findByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            List<String> strategies,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
}
