package am.trade.api.dto.summary;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyPerformance {
    private LocalDate date;
    private PerformanceMetrics metrics;
    private String bestTradeSymbol;
    private BigDecimal bestTradePnL;
    private BigDecimal averageProfitPerTrade;
    private BigDecimal averageWinAmount;
    private BigDecimal averageLossAmount;
    private double averageHoldingTimeWin;
    private double averageHoldingTimeLoss;
    private BigDecimal totalProfitLoss;
    private int tradeCount;
    private int winCount;
    private int lossCount;
    private double winRate;

    public DailyPerformance() {
    }

    public DailyPerformance(LocalDate date, PerformanceMetrics metrics, String bestTradeSymbol, BigDecimal bestTradePnL,
            BigDecimal averageProfitPerTrade, BigDecimal averageWinAmount, BigDecimal averageLossAmount,
            double averageHoldingTimeWin, double averageHoldingTimeLoss, BigDecimal totalProfitLoss, int tradeCount,
            int winCount, int lossCount, double winRate) {
        this.date = date;
        this.metrics = metrics;
        this.bestTradeSymbol = bestTradeSymbol;
        this.bestTradePnL = bestTradePnL;
        this.averageProfitPerTrade = averageProfitPerTrade;
        this.averageWinAmount = averageWinAmount;
        this.averageLossAmount = averageLossAmount;
        this.averageHoldingTimeWin = averageHoldingTimeWin;
        this.averageHoldingTimeLoss = averageHoldingTimeLoss;
        this.totalProfitLoss = totalProfitLoss;
        this.tradeCount = tradeCount;
        this.winCount = winCount;
        this.lossCount = lossCount;
        this.winRate = winRate;
    }

    public static DailyPerformanceBuilder builder() {
        return new DailyPerformanceBuilder();
    }

    // Getters
    public LocalDate getDate() {
        return date;
    }

    public PerformanceMetrics getMetrics() {
        return metrics;
    }

    public String getBestTradeSymbol() {
        return bestTradeSymbol;
    }

    public BigDecimal getBestTradePnL() {
        return bestTradePnL;
    }

    public BigDecimal getAverageProfitPerTrade() {
        return averageProfitPerTrade;
    }

    public BigDecimal getAverageWinAmount() {
        return averageWinAmount;
    }

    public BigDecimal getAverageLossAmount() {
        return averageLossAmount;
    }

    public double getAverageHoldingTimeWin() {
        return averageHoldingTimeWin;
    }

    public double getAverageHoldingTimeLoss() {
        return averageHoldingTimeLoss;
    }

    public BigDecimal getTotalProfitLoss() {
        return totalProfitLoss;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public int getWinCount() {
        return winCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public double getWinRate() {
        return winRate;
    }

    // Setters
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setMetrics(PerformanceMetrics metrics) {
        this.metrics = metrics;
    }

    public void setBestTradeSymbol(String bestTradeSymbol) {
        this.bestTradeSymbol = bestTradeSymbol;
    }

    public void setBestTradePnL(BigDecimal bestTradePnL) {
        this.bestTradePnL = bestTradePnL;
    }

    public void setAverageProfitPerTrade(BigDecimal averageProfitPerTrade) {
        this.averageProfitPerTrade = averageProfitPerTrade;
    }

    public void setAverageWinAmount(BigDecimal averageWinAmount) {
        this.averageWinAmount = averageWinAmount;
    }

    public void setAverageLossAmount(BigDecimal averageLossAmount) {
        this.averageLossAmount = averageLossAmount;
    }

    public void setAverageHoldingTimeWin(double averageHoldingTimeWin) {
        this.averageHoldingTimeWin = averageHoldingTimeWin;
    }

    public void setAverageHoldingTimeLoss(double averageHoldingTimeLoss) {
        this.averageHoldingTimeLoss = averageHoldingTimeLoss;
    }

    public void setTotalProfitLoss(BigDecimal totalProfitLoss) {
        this.totalProfitLoss = totalProfitLoss;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    public void setWinRate(double winRate) {
        this.winRate = winRate;
    }

    public static class DailyPerformanceBuilder {
        private LocalDate date;
        private PerformanceMetrics metrics;
        private String bestTradeSymbol;
        private BigDecimal bestTradePnL;
        private BigDecimal averageProfitPerTrade;
        private BigDecimal averageWinAmount;
        private BigDecimal averageLossAmount;
        private double averageHoldingTimeWin;
        private double averageHoldingTimeLoss;
        private BigDecimal totalProfitLoss;
        private int tradeCount;
        private int winCount;
        private int lossCount;
        private double winRate;

        DailyPerformanceBuilder() {
        }

        public DailyPerformanceBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public DailyPerformanceBuilder metrics(PerformanceMetrics metrics) {
            this.metrics = metrics;
            return this;
        }

        public DailyPerformanceBuilder bestTradeSymbol(String bestTradeSymbol) {
            this.bestTradeSymbol = bestTradeSymbol;
            return this;
        }

        public DailyPerformanceBuilder bestTradePnL(BigDecimal bestTradePnL) {
            this.bestTradePnL = bestTradePnL;
            return this;
        }

        public DailyPerformanceBuilder averageProfitPerTrade(BigDecimal averageProfitPerTrade) {
            this.averageProfitPerTrade = averageProfitPerTrade;
            return this;
        }

        public DailyPerformanceBuilder averageWinAmount(BigDecimal averageWinAmount) {
            this.averageWinAmount = averageWinAmount;
            return this;
        }

        public DailyPerformanceBuilder averageLossAmount(BigDecimal averageLossAmount) {
            this.averageLossAmount = averageLossAmount;
            return this;
        }

        public DailyPerformanceBuilder averageHoldingTimeWin(double averageHoldingTimeWin) {
            this.averageHoldingTimeWin = averageHoldingTimeWin;
            return this;
        }

        public DailyPerformanceBuilder averageHoldingTimeLoss(double averageHoldingTimeLoss) {
            this.averageHoldingTimeLoss = averageHoldingTimeLoss;
            return this;
        }

        public DailyPerformanceBuilder totalProfitLoss(BigDecimal totalProfitLoss) {
            this.totalProfitLoss = totalProfitLoss;
            return this;
        }

        public DailyPerformanceBuilder tradeCount(int tradeCount) {
            this.tradeCount = tradeCount;
            return this;
        }

        public DailyPerformanceBuilder winCount(int winCount) {
            this.winCount = winCount;
            return this;
        }

        public DailyPerformanceBuilder lossCount(int lossCount) {
            this.lossCount = lossCount;
            return this;
        }

        public DailyPerformanceBuilder winRate(double winRate) {
            this.winRate = winRate;
            return this;
        }

        public DailyPerformance build() {
            return new DailyPerformance(date, metrics, bestTradeSymbol, bestTradePnL, averageProfitPerTrade,
                    averageWinAmount, averageLossAmount, averageHoldingTimeWin, averageHoldingTimeLoss, totalProfitLoss,
                    tradeCount, winCount, lossCount, winRate);
        }
    }
}
