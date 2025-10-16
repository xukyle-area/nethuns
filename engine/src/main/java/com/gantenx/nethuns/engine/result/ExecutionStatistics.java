package com.gantenx.nethuns.engine.result;

import java.util.ArrayList;
import java.util.List;
import com.gantenx.nethuns.engine.executor.strategy.TradingDecision;

/**
 * 执行统计信息
 * 记录交易执行过程中的各种统计数据
 */
public class ExecutionStatistics {

    private long startTime = 0;
    private long endTime = 0;
    private int timeSteps = 0;
    private int tradeCount = 0;
    private int buyDecisions = 0;
    private int sellDecisions = 0;
    private int holdDecisions = 0;
    private int errorCount = 0;

    private final List<Exception> errors = new ArrayList<>();
    private final List<TradingDecision> decisions = new ArrayList<>();

    /**
     * 开始执行
     */
    public void startExecution() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 结束执行
     */
    public void endExecution() {
        this.endTime = System.currentTimeMillis();
    }

    /**
     * 记录决策
     */
    public void recordDecision(TradingDecision decision) {
        decisions.add(decision);

        if (decision.isBuy()) {
            buyDecisions++;
        } else if (decision.isSell()) {
            sellDecisions++;
        } else {
            holdDecisions++;
        }
    }

    /**
     * 记录错误
     */
    public void recordError(Exception error) {
        errors.add(error);
        errorCount++;
    }

    /**
     * 记录决策错误
     */
    public void recordDecisionError(Exception error) {
        recordError(error);
    }

    /**
     * 增加时间步数
     */
    public void incrementTimeStep() {
        timeSteps++;
    }

    /**
     * 增加交易次数
     */
    public void incrementTradeCount() {
        tradeCount++;
    }

    // Getters

    public long getExecutionDuration() {
        if (startTime == 0)
            return 0;
        return (endTime > 0 ? endTime : System.currentTimeMillis()) - startTime;
    }

    public int getTimeSteps() {
        return timeSteps;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public int getBuyDecisions() {
        return buyDecisions;
    }

    public int getSellDecisions() {
        return sellDecisions;
    }

    public int getHoldDecisions() {
        return holdDecisions;
    }

    public int getTotalDecisions() {
        return buyDecisions + sellDecisions + holdDecisions;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<Exception> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<TradingDecision> getDecisions() {
        return new ArrayList<>(decisions);
    }

    /**
     * 获取交易成功率
     */
    public double getTradeSuccessRate() {
        int totalActionDecisions = buyDecisions + sellDecisions;
        return totalActionDecisions > 0 ? (double) tradeCount / totalActionDecisions : 0.0;
    }

    /**
     * 获取决策分布
     */
    public String getDecisionDistribution() {
        int total = getTotalDecisions();
        if (total == 0) {
            return "No decisions recorded";
        }

        return String.format("Buy: %d (%.1f%%), Sell: %d (%.1f%%), Hold: %d (%.1f%%)", buyDecisions,
                (buyDecisions * 100.0 / total), sellDecisions, (sellDecisions * 100.0 / total), holdDecisions,
                (holdDecisions * 100.0 / total));
    }

    @Override
    public String toString() {
        return String.format(
                "ExecutionStatistics{duration=%dms, timeSteps=%d, trades=%d, "
                        + "decisions=[buy=%d, sell=%d, hold=%d], errors=%d, successRate=%.2f%%}",
                getExecutionDuration(), timeSteps, tradeCount, buyDecisions, sellDecisions, holdDecisions, errorCount,
                getTradeSuccessRate() * 100);
    }
}
