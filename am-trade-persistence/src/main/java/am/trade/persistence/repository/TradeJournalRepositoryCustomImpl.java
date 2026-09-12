package am.trade.persistence.repository;

import am.trade.common.models.TradeJournalEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MongoTemplate implementation of filtered journal search
 */
@Repository
public class TradeJournalRepositoryCustomImpl implements TradeJournalRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<TradeJournalEntry> searchEntries(
            String userId,
            String journalStatus,
            String entryType,
            String symbol,
            String setup,
            String folderId,
            String playbookId,
            List<String> tagIds,
            String tradeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String q,
            Pageable pageable) {

        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId must not be null or empty");
        }

        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));

        if (StringUtils.hasText(journalStatus)) {
            criteriaList.add(Criteria.where("journalStatus").is(journalStatus));
        }
        if (StringUtils.hasText(entryType)) {
            criteriaList.add(Criteria.where("entryType").is(entryType));
        }
        if (StringUtils.hasText(symbol)) {
            criteriaList.add(Criteria.where("symbol")
                    .regex(Pattern.compile(Pattern.quote(symbol), Pattern.CASE_INSENSITIVE)));
        }
        if (StringUtils.hasText(setup)) {
            criteriaList.add(Criteria.where("setup").is(setup));
        }
        if (StringUtils.hasText(folderId)) {
            criteriaList.add(Criteria.where("folderId").is(folderId));
        }
        if (StringUtils.hasText(playbookId)) {
            criteriaList.add(Criteria.where("playbookId").is(playbookId));
        }
        if (tagIds != null && !tagIds.isEmpty()) {
            criteriaList.add(Criteria.where("tagIds").in(tagIds));
        }
        if (StringUtils.hasText(tradeId)) {
            criteriaList.add(Criteria.where("tradeId").is(tradeId));
        }
        if (startDate != null && endDate != null) {
            criteriaList.add(Criteria.where("entryDate").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            criteriaList.add(Criteria.where("entryDate").gte(startDate));
        } else if (endDate != null) {
            criteriaList.add(Criteria.where("entryDate").lte(endDate));
        }
        if (StringUtils.hasText(q)) {
            String escaped = Pattern.quote(q.trim());
            Pattern pattern = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("title").regex(pattern),
                    Criteria.where("content").regex(pattern),
                    Criteria.where("symbol").regex(pattern),
                    Criteria.where("setup").regex(pattern)
            ));
        }

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        long count = mongoTemplate.count(query, TradeJournalEntry.class);

        if (pageable.getSort().isSorted()) {
            query.with(pageable);
        } else {
            query.with(pageable).with(Sort.by(Sort.Direction.DESC, "entryDate"));
        }

        List<TradeJournalEntry> results = mongoTemplate.find(query, TradeJournalEntry.class);
        return new PageImpl<>(results, pageable, count);
    }
}
