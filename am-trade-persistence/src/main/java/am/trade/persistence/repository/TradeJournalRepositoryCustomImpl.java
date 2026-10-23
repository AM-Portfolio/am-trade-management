package am.trade.persistence.repository;

import am.trade.common.models.TradeJournalEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

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
            String portfolioId,
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
        if (StringUtils.hasText(portfolioId)) {
            criteriaList.add(Criteria.where("portfolioId").is(portfolioId));
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
                    Criteria.where("contentPlain").regex(pattern),
                    Criteria.where("symbol").regex(pattern),
                    Criteria.where("setup").regex(pattern),
                    Criteria.where("postTradeReview.mistakeCategory").regex(pattern),
                    Criteria.where("postTradeReview.lessonLearned").regex(pattern),
                    Criteria.where("tagIds").regex(pattern)
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

    private static final List<String> TRADE_LIKE_ENTRY_TYPES = List.of(
            "TRADE_JOURNAL",
            "PRE_PLAN",
            "POST_REVIEW",
            "TRADE_NOTE",
            "MISSED");

    @Override
    public Optional<JournalAnalyticsAggregation.Summary> aggregateSummary(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String portfolioId) {
        Criteria range = rangeCriteria(userId, startDate, endDate, portfolioId);
        MatchOperation matchRange = match(range);

        long planned = 0;
        long open = 0;
        long completed = 0;
        long archived = 0;

        Aggregation statusAgg = newAggregation(
                matchRange,
                group("journalStatus").count().as("count"));
        AggregationResults<Document> statusResults =
                mongoTemplate.aggregate(statusAgg, TradeJournalEntry.class, Document.class);
        for (Document doc : statusResults.getMappedResults()) {
            String status = doc.getString("_id");
            Number countNum = doc.get("count", Number.class);
            long count = countNum != null ? countNum.longValue() : 0L;
            if (status == null) {
                continue;
            }
            switch (status.toUpperCase()) {
                case "PLANNED" -> planned = count;
                case "OPEN" -> open = count;
                case "COMPLETED" -> completed = count;
                case "ARCHIVED" -> archived = count;
                default -> { /* ignore */ }
            }
        }

        double totalPnl = 0;
        int pnlCount = 0;
        int wins = 0;
        Aggregation pnlAgg = newAggregation(
                matchRange,
                match(Criteria.where("postTradeReview.actualPnl").exists(true).ne(null)),
                group()
                        .sum("postTradeReview.actualPnl").as("totalPnl")
                        .count().as("pnlCount")
                        .sum(ConditionalOperators.when(
                                ComparisonOperators.Gt.valueOf("postTradeReview.actualPnl")
                                        .greaterThanValue(0))
                                .then(1)
                                .otherwise(0))
                        .as("wins"));
        Document pnlDoc = mongoTemplate.aggregate(pnlAgg, TradeJournalEntry.class, Document.class)
                .getUniqueMappedResult();
        if (pnlDoc != null) {
            Number total = pnlDoc.get("totalPnl", Number.class);
            Number count = pnlDoc.get("pnlCount", Number.class);
            Number winCount = pnlDoc.get("wins", Number.class);
            totalPnl = total != null ? total.doubleValue() : 0;
            pnlCount = count != null ? count.intValue() : 0;
            wins = winCount != null ? winCount.intValue() : 0;
        }

        Criteria tradeLike = new Criteria().orOperator(
                Criteria.where("tradeId").exists(true).ne(null).ne(""),
                Criteria.where("entryType").in(TRADE_LIKE_ENTRY_TYPES));
        Aggregation tradeLikeAgg = newAggregation(
                match(new Criteria().andOperator(range, tradeLike)),
                group().count().as("count"));
        Document tradeLikeDoc = mongoTemplate.aggregate(tradeLikeAgg, TradeJournalEntry.class, Document.class)
                .getUniqueMappedResult();
        long totalTrades = 0;
        if (tradeLikeDoc != null) {
            Number n = tradeLikeDoc.get("count", Number.class);
            totalTrades = n != null ? n.longValue() : 0L;
        }

        double rrSum = 0;
        int rrCount = 0;
        ProjectionOperation rrProject = project()
                .andExpression(
                        "ifNull(postTradeReview.actualRMultiple, preTradePlan.plannedRRRatio)")
                .as("rr");
        Aggregation rrAgg = newAggregation(
                matchRange,
                rrProject,
                match(Criteria.where("rr").exists(true).ne(null)),
                group().avg("rr").as("avgRr").count().as("rrCount"));
        Document rrDoc = mongoTemplate.aggregate(rrAgg, TradeJournalEntry.class, Document.class)
                .getUniqueMappedResult();
        if (rrDoc != null) {
            Number avg = rrDoc.get("avgRr", Number.class);
            Number count = rrDoc.get("rrCount", Number.class);
            rrCount = count != null ? count.intValue() : 0;
            if (avg != null && rrCount > 0) {
                rrSum = avg.doubleValue() * rrCount;
            }
        }

        return Optional.of(JournalAnalyticsAggregation.Summary.builder()
                .plannedCount(planned)
                .openCount(open)
                .completedCount(completed)
                .archivedCount(archived)
                .totalTrades(totalTrades)
                .totalPnl(totalPnl)
                .pnlCount(pnlCount)
                .wins(wins)
                .rrSum(rrSum)
                .rrCount(rrCount)
                .build());
    }

    @Override
    public JournalAnalyticsAggregation.MistakesResult aggregateMistakes(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String portfolioId) {
        Criteria range = rangeCriteria(userId, startDate, endDate, portfolioId);
        Criteria hasMistake = new Criteria().andOperator(
                Criteria.where("postTradeReview.mistakeCategory").exists(true),
                Criteria.where("postTradeReview.mistakeCategory").ne(null),
                Criteria.where("postTradeReview.mistakeCategory").ne(""),
                Criteria.where("postTradeReview.mistakeCategory").not().regex("^NONE$", "i"));

        MatchOperation matchRangeWithMistake = match(new Criteria().andOperator(range, hasMistake));

        Aggregation groupAgg = newAggregation(
                matchRangeWithMistake,
                group("postTradeReview.mistakeCategory")
                        .count().as("occurrenceCount")
                        .sum("postTradeReview.actualPnl").as("totalPnlImpact")
                        .sum(ConditionalOperators.when(
                                ComparisonOperators.Lt.valueOf("postTradeReview.actualPnl")
                                        .lessThanValue(0))
                                .then(1)
                                .otherwise(0))
                        .as("lossOccurrences"));

        List<JournalAnalyticsAggregation.MistakeGroup> groups = new ArrayList<>();
        for (Document doc : mongoTemplate
                .aggregate(groupAgg, TradeJournalEntry.class, Document.class)
                .getMappedResults()) {
            String category = doc.getString("_id");
            if (!StringUtils.hasText(category)) {
                continue;
            }
            groups.add(JournalAnalyticsAggregation.MistakeGroup.builder()
                    .mistakeCategory(category.toUpperCase())
                    .occurrenceCount(readLong(doc, "occurrenceCount"))
                    .totalPnlImpact(readDouble(doc, "totalPnlImpact"))
                    .lossOccurrences(readLong(doc, "lossOccurrences"))
                    .build());
        }

        Aggregation totalLossesAgg = newAggregation(
                matchRangeWithMistake,
                match(Criteria.where("postTradeReview.actualPnl").exists(true).lt(0)),
                group().count().as("totalLosses"));
        Document lossDoc = mongoTemplate.aggregate(totalLossesAgg, TradeJournalEntry.class, Document.class)
                .getUniqueMappedResult();
        long totalLosses = lossDoc != null ? readLong(lossDoc, "totalLosses") : 0L;

        return JournalAnalyticsAggregation.MistakesResult.builder()
                .groups(groups)
                .totalLosses(totalLosses)
                .build();
    }

    @Override
    public Optional<JournalAnalyticsAggregation.Adherence> aggregateAdherence(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String portfolioId) {
        Criteria range = rangeCriteria(userId, startDate, endDate, portfolioId);
        Aggregation agg = newAggregation(
                match(range),
                group()
                        .avg("planAdherenceScore").as("avgPlanAdherenceScore")
                        .avg("checklistCompletionPct").as("avgChecklistCompletionPct")
                        .avg("postTradeReview.executionScore").as("avgExecutionScore")
                        .sum(ConditionalOperators.when(
                                ComparisonOperators.Ne.valueOf("planAdherenceScore").notEqualToValue(null))
                                .then(1)
                                .otherwise(0))
                        .as("adherenceCount")
                        .sum(ConditionalOperators.when(
                                ComparisonOperators.Ne.valueOf("checklistCompletionPct").notEqualToValue(null))
                                .then(1)
                                .otherwise(0))
                        .as("checklistCount")
                        .sum(ConditionalOperators.when(
                                ComparisonOperators.Ne.valueOf("postTradeReview.executionScore")
                                        .notEqualToValue(null))
                                .then(1)
                                .otherwise(0))
                        .as("execCount"));

        Document doc = mongoTemplate.aggregate(agg, TradeJournalEntry.class, Document.class)
                .getUniqueMappedResult();
        if (doc == null) {
            return Optional.empty();
        }

        return Optional.of(JournalAnalyticsAggregation.Adherence.builder()
                .avgPlanAdherenceScore(readNullableDouble(doc, "avgPlanAdherenceScore"))
                .avgChecklistCompletionPct(readNullableDouble(doc, "avgChecklistCompletionPct"))
                .avgExecutionScore(readNullableDouble(doc, "avgExecutionScore"))
                .adherenceCount(readInt(doc, "adherenceCount"))
                .checklistCount(readInt(doc, "checklistCount"))
                .execCount(readInt(doc, "execCount"))
                .build());
    }

    @Override
    public List<TradeJournalEntry> findLessonsInRange(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String portfolioId,
            int limit) {
        Criteria range = rangeCriteria(userId, startDate, endDate, portfolioId);
        Criteria hasLesson = new Criteria().andOperator(
                Criteria.where("postTradeReview.lessonLearned").exists(true),
                Criteria.where("postTradeReview.lessonLearned").ne(null),
                Criteria.where("postTradeReview.lessonLearned").ne(""));

        Query query = new Query(new Criteria().andOperator(range, hasLesson))
                .with(Sort.by(Sort.Direction.DESC, "entryDate"))
                .limit(Math.max(limit, 1));

        return mongoTemplate.find(query, TradeJournalEntry.class);
    }

    private Criteria rangeCriteria(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String portfolioId) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));
        criteriaList.add(Criteria.where("entryDate").gte(startDate).lte(endDate));
        if (StringUtils.hasText(portfolioId)) {
            criteriaList.add(Criteria.where("portfolioId").is(portfolioId));
        }
        return new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    }

    private static long readLong(Document doc, String field) {
        Number n = doc.get(field, Number.class);
        return n != null ? n.longValue() : 0L;
    }

    private static int readInt(Document doc, String field) {
        Number n = doc.get(field, Number.class);
        return n != null ? n.intValue() : 0;
    }

    private static double readDouble(Document doc, String field) {
        Number n = doc.get(field, Number.class);
        return n != null ? n.doubleValue() : 0.0;
    }

    private static Double readNullableDouble(Document doc, String field) {
        Number n = doc.get(field, Number.class);
        return n != null ? n.doubleValue() : null;
    }
}
