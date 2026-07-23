package am.trade.persistence.config;

import am.trade.persistence.entity.TradeDetailsEntity;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoIndexConfig {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        IndexOperations tradeOps = mongoTemplate.indexOps(TradeDetailsEntity.class);

        // Index 0: Powers business key lookups (PUT/POST)
        tradeOps.ensureIndex(new Index()
                .on("tradeId", Sort.Direction.ASC)
                .named("idx_trade_trade_id")
                .unique()
                .background());

        // Index 1: Powers GET /v1/trades/details/portfolio/{portfolioId}
        // Satisfies queries on portfolioId alone (prefix rule)
        // AND queries on portfolioId + symbol (the filtered symbol search)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("symbol", Sort.Direction.ASC)
                .named("idx_trade_portfolio_symbol")
                .background());

        // Index 2: Powers pagination queries sorted by entry date (most common sort)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("entryInfo.timestamp", Sort.Direction.DESC)
                .named("idx_trade_portfolio_date")
                .background());

        // Index 3: Powers the Filter API (status is the primary discriminator)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("idx_trade_portfolio_status")
                .background());

        // Index 4: Powers Journal/Analytics by user id and date
        tradeOps.ensureIndex(new Index()
                .on("userId", Sort.Direction.ASC)
                .on("entryInfo.timestamp", Sort.Direction.DESC)
                .named("idx_trade_user_date")
                .background());

        // Index 5: Powers Journal/Analytics by user id and symbol
        tradeOps.ensureIndex(new Index()
                .on("userId", Sort.Direction.ASC)
                .on("symbol", Sort.Direction.ASC)
                .named("idx_trade_user_symbol")
                .background());
    }
}
