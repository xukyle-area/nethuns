package com.gantenx.nethuns.engine.result;

import com.gantenx.nethuns.engine.model.TradeRecord;

/**
 * 执行结果
 * 封装交易执行的完整结果，包括交易记录、统计信息和摘要
 */
public class ExecutionResult {

    private final TradeRecord tradeRecord;
    private final ExecutionStatistics statistics;
    private final String summary;
    private final boolean successful;

    public ExecutionResult(TradeRecord tradeRecord, ExecutionStatistics statistics, String summary) {
        this.tradeRecord = tradeRecord;
        this.statistics = statistics;
        this.summary = summary;
        this.successful = (tradeRecord != null && statistics.getErrorCount() == 0);
    }

    public ExecutionResult(ExecutionStatistics statistics, String errorMessage) {
        this.tradeRecord = null;
        this.statistics = statistics;
        this.summary = errorMessage;
        this.successful = false;
    }

    // Getters

    public TradeRecord getTradeRecord() {
        return tradeRecord;
    }

    public ExecutionStatistics getStatistics() {
        return statistics;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isSuccessful() {
        return successful;
    }

    /**
     * 获取盈利情况
     */
    public double getProfitLoss() {
        if (tradeRecord == null) {
            return 0.0;
        }
        return tradeRecord.getBalance() - tradeRecord.getInitialBalance();
    }

    /**
     * 获取收益率
     */
    public double getReturnRate() {
        if (tradeRecord == null || tradeRecord.getInitialBalance() <= 0) {
            return 0.0;
        }
        return (getProfitLoss() / tradeRecord.getInitialBalance()) * 100;
    }

    /**
     * 是否盈利
     */
    public boolean isProfitable() {
        return getProfitLoss() > 0;
    }

    /**
     * 获取详细报告
     */
    public String getDetailedReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Execution Result ===\n");
        report.append(String.format("Status: %s\n", successful ? "SUCCESS" : "FAILED"));
        report.append(String.format("Summary: %s\n", summary));
        report.append("\n");

        if (tradeRecord != null) {
            report.append("=== Financial Results ===\n");
            report.append(String.format("Initial Balance: %.2f\n", tradeRecord.getInitialBalance()));
            report.append(String.format("Final Balance: %.2f\n", tradeRecord.getBalance()));
            report.append(String.format("Profit/Loss: %.2f\n", getProfitLoss()));
            report.append(String.format("Return Rate: %.2f%%\n", getReturnRate()));
            report.append(String.format("Total Fees: %.2f\n", tradeRecord.getFeeCount()));
            report.append("\n");

            report.append("=== Trading Activity ===\n");
            report.append(String.format("Total Orders: %d\n",
                    tradeRecord.getOrders() != null ? tradeRecord.getOrders().size() : 0));
            report.append(String.format("Total Trades: %d\n",
                    tradeRecord.getRecords() != null ? tradeRecord.getRecords().size() : 0));
        }

        if (statistics != null) {
            report.append("\n");
            report.append("=== Execution Statistics ===\n");
            report.append(String.format("Execution Duration: %dms\n", statistics.getExecutionDuration()));
            report.append(String.format("Time Steps: %d\n", statistics.getTimeSteps()));
            report.append(String.format("Decision Distribution: %s\n", statistics.getDecisionDistribution()));
            report.append(String.format("Trade Success Rate: %.2f%%\n", statistics.getTradeSuccessRate() * 100));

            if (statistics.getErrorCount() > 0) {
                report.append(String.format("Errors Encountered: %d\n", statistics.getErrorCount()));
            }
        }

        return report.toString();
    }

    @Override
    public String toString() {
        return String.format("ExecutionResult{successful=%s, profit=%.2f, return=%.2f%%, duration=%dms}", successful,
                getProfitLoss(), getReturnRate(), statistics != null ? statistics.getExecutionDuration() : 0);
    }
}
