package am.trade.persistence.repository;

import am.trade.models.enums.TradeStatus;
import am.trade.persistence.entity.TradeDetailsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TradeDetailsRepositoryCustomImpl implements TradeDetailsRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<TradeDetailsEntity> findByFilters(
            String userId,
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            List<String> strategies,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Query query = new Query();

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId must not be null or empty — UserContext may not be initialized for this request");
        }
        query.addCriteria(Criteria.where("userId").is(userId));

        if (portfolioIds != null && !portfolioIds.isEmpty()) {
            query.addCriteria(Criteria.where("portfolioId").in(portfolioIds));
        }

        if (symbols != null && !symbols.isEmpty()) {
            List<String> upperCaseSymbols = symbols.stream().map(String::toUpperCase).collect(Collectors.toList());
            // In MongoDB, you can use regex for case-insensitivity, but since the model stores uppercase
            // or we mapped it to uppercase, we will use in() directly on the list.
            query.addCriteria(Criteria.where("symbol").in(upperCaseSymbols));
        }

        if (statuses != null && !statuses.isEmpty()) {
            query.addCriteria(Criteria.where("status").in(statuses));
        }

        if (strategies != null && !strategies.isEmpty()) {
            // Some strategies might be case sensitive, let's use regex for case insensitivity
            // or just $in if they are exact matches. The previous java stream used equalsIgnoreCase
            List<java.util.regex.Pattern> regexPatterns = strategies.stream()
                    .map(s -> java.util.regex.Pattern.compile("^" + java.util.regex.Pattern.quote(s) + "$", java.util.regex.Pattern.CASE_INSENSITIVE))
                    .collect(Collectors.toList());
            query.addCriteria(Criteria.where("strategy").in(regexPatterns));
        }

        if (startDate != null && endDate != null) {
            query.addCriteria(Criteria.where("entryInfo.timestamp").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            query.addCriteria(Criteria.where("entryInfo.timestamp").gte(startDate));
        } else if (endDate != null) {
            query.addCriteria(Criteria.where("entryInfo.timestamp").lte(endDate));
        }

        long count = mongoTemplate.count(query, TradeDetailsEntity.class);

        query.with(pageable);
        List<TradeDetailsEntity> results = mongoTemplate.find(query, TradeDetailsEntity.class);

        return new PageImpl<>(results, pageable, count);
    }
}
