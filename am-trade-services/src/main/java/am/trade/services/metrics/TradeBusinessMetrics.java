package am.trade.services.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Domain meters for the Grafana "Functional / Services" dashboard (trade domain path).
 *
 * <p>Two meters are registered here:
 * <ul>
 *   <li>{@code trade.processed} — Counter, tagged by {@code status} (success/failure)
 *       and {@code position_type} (LONG/SHORT). Prometheus exports this as
 *       {@code trade_processed_total}.</li>
 *   <li>{@code trade.processing.duration} — Timer for the core processing pipeline.
 *       Prometheus exports this as {@code trade_processing_duration_seconds}
 *       (with {@code _bucket}, {@code _count}, {@code _sum} suffixes).</li>
 * </ul>
 *
 * <p><b>Why a separate class instead of calling MeterRegistry directly?</b><br>
 * Centralising metric definitions here means:
 * <ul>
 *   <li>Tag names and metric names are defined in exactly one place — easy to rename.</li>
 *   <li>Business logic classes stay clean; they call a simple method, not a builder chain.</li>
 *   <li>Unit-testable with Micrometer's {@code SimpleMeterRegistry} without starting Spring.</li>
 * </ul>
 *
 * <p>Mirrors the pattern in {@code McpBusinessMetrics} (am-core-services).
 */
@Component
public class TradeBusinessMetrics {

    private final MeterRegistry registry;

    public TradeBusinessMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Increments the trade processed counter.
     *
     * @param status        outcome of processing: "success" or "failure"
     * @param positionType  trade direction: "LONG", "SHORT", or "unknown"
     */
    public void recordTradeProcessed(String status, String positionType) {
        Counter.builder("trade.processed")
                .description("Total trades processed by the trade processing pipeline")
                .tag("status", status == null ? "unknown" : status)
                .tag("position_type", positionType == null ? "unknown" : positionType)
                .register(registry)
                .increment();
    }

    /**
     * Records the wall-clock time spent inside the trade processing pipeline.
     *
     * <p>Usage pattern (in the caller):
     * <pre>{@code
     *   long start = System.nanoTime();
     *   try {
     *       // ... business logic ...
     *   } finally {
     *       tradeBusinessMetrics.recordProcessingDuration(System.nanoTime() - start);
     *   }
     * }</pre>
     *
     * @param durationNanos elapsed time measured with {@link System#nanoTime()}
     */
    public void recordProcessingDuration(long durationNanos) {
        Timer.builder("trade.processing.duration")
                .description("Wall-clock time of the trade processing pipeline")
                .register(registry)
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
