package am.trade.persistence.config;

import am.trade.persistence.entity.TradeDetailsEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        log.info("Creating MongoDB indexes for trade_details collection...");
        IndexOperations tradeOps = mongoTemplate.indexOps(TradeDetailsEntity.class);
        int created = 0;
        int failed = 0;

        // Index 0a: UNIQUE index on tradeId — powers all findByTradeId lookups.
        // Can fail if duplicate tradeIds exist in the collection (e.g. after load testing
        // without a unique constraint). The non-unique fallback below (0b) ensures queries
        // remain fast even in that degraded state.
        try {
            tradeOps.ensureIndex(new Index()
                    .on("tradeId", Sort.Direction.ASC)
                    .named("idx_trade_trade_id")
                    .unique()
                    .background());
            created++;
            log.info("[OK] idx_trade_trade_id (unique)");
        } catch (Exception e) {
            failed++;
            log.warn("[WARN] idx_trade_trade_id (unique) FAILED — duplicate tradeIds detected. " +
                     "Run the cleanup script (docs/ops/deduplicate-trade-ids.js) against am-apps-dev " +
                     "to resolve duplicates and re-enable the unique constraint. " +
                     "Falling back to non-unique index for query performance.", e);
        }

        // Index 0b: NON-UNIQUE fallback index on tradeId.
        // Always created regardless of whether the unique index above succeeded.
        // Guarantees that findByTradeId() is never a full collection scan (COLLSCAN)
        // even when duplicates exist.
        try {
            tradeOps.ensureIndex(new Index()
                    .on("tradeId", Sort.Direction.ASC)
                    .named("idx_trade_trade_id_lookup")
                    .background());
            created++;
            log.info("[OK] idx_trade_trade_id_lookup (non-unique fallback)");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_trade_id_lookup FAILED — trade update/read queries may be slow!", e);
        }

        // Index 1: Powers GET /v1/trades/details/portfolio/{portfolioId}
        // Satisfies queries on portfolioId alone (prefix rule)
        // AND queries on portfolioId + symbol (the filtered symbol search)
        try {
            tradeOps.ensureIndex(new Index()
                    .on("portfolioId", Sort.Direction.ASC)
                    .on("symbol", Sort.Direction.ASC)
                    .named("idx_trade_portfolio_symbol")
                    .background());
            created++;
            log.info("[OK] idx_trade_portfolio_symbol");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_portfolio_symbol FAILED", e);
        }

        // Index 1b: Powers GET /v2/trades/details/portfolio/{portfolioId}
        // with the new User ID pushdown optimization
        try {
            tradeOps.ensureIndex(new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("portfolioId", Sort.Direction.ASC)
                    .on("symbol", Sort.Direction.ASC)
                    .named("idx_trade_user_portfolio_symbol")
                    .background());
            created++;
            log.info("[OK] idx_trade_user_portfolio_symbol");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_user_portfolio_symbol FAILED", e);
        }

        // Index 2: Powers pagination queries sorted by entry date (most common sort)
        try {
            tradeOps.ensureIndex(new Index()
                    .on("portfolioId", Sort.Direction.ASC)
                    .on("entryInfo.timestamp", Sort.Direction.DESC)
                    .named("idx_trade_portfolio_date")
                    .background());
            created++;
            log.info("[OK] idx_trade_portfolio_date");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_portfolio_date FAILED", e);
        }

        // Index 3: Powers the Filter API (status is the primary discriminator)
        try {
            tradeOps.ensureIndex(new Index()
                    .on("portfolioId", Sort.Direction.ASC)
                    .on("status", Sort.Direction.ASC)
                    .named("idx_trade_portfolio_status")
                    .background());
            created++;
            log.info("[OK] idx_trade_portfolio_status");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_portfolio_status FAILED", e);
        }

        // Index 4: Powers Journal/Analytics by user id and date
        try {
            tradeOps.ensureIndex(new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("entryInfo.timestamp", Sort.Direction.DESC)
                    .named("idx_trade_user_date")
                    .background());
            created++;
            log.info("[OK] idx_trade_user_date");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_user_date FAILED", e);
        }

        // Index 5: Powers Journal/Analytics by user id and symbol
        try {
            tradeOps.ensureIndex(new Index()
                    .on("userId", Sort.Direction.ASC)
                    .on("symbol", Sort.Direction.ASC)
                    .named("idx_trade_user_symbol")
                    .background());
            created++;
            log.info("[OK] idx_trade_user_symbol");
        } catch (Exception e) {
            failed++;
            log.error("[ERROR] idx_trade_user_symbol FAILED", e);
        }

        if (failed == 0) {
            log.info("MongoDB index setup complete: {}/{} indexes OK.", created, created + failed);
        } else {
            log.warn("MongoDB index setup complete with issues: {}/{} indexes OK, {} FAILED. " +
                     "Check logs above for details.", created, created + failed, failed);
        }
    }
}

