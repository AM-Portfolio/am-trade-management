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

        // Index 1: Powers GET /v1/trades/details/portfolio/{portfolioId}
        // Satisfies queries on portfolioId alone (prefix rule)
        // AND queries on portfolioId + symbol (the filtered symbol search)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("symbol", Sort.Direction.ASC)
                .named("idx_trade_portfolio_symbol"));

        // Index 2: Powers pagination queries sorted by entry date (most common sort)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("entryInfo.timestamp", Sort.Direction.DESC)
                .named("idx_trade_portfolio_date"));

        // Index 3: Powers the Filter API (status is the primary discriminator)
        tradeOps.ensureIndex(new Index()
                .on("portfolioId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("idx_trade_portfolio_status"));
    }
}
