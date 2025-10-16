package com.gantenx.nethuns.engine.executor.strategy;

import com.gantenx.nethuns.commons.enums.Proportion;

/**
 * 交易决策结果
 * 封装策略产生的交易决策和相关参数
 */
public class TradingDecision {

    /**
     * 交易动作类型
     */
    public enum Action {
        BUY, // 买入
        SELL, // 卖出
        HOLD // 持有（不操作）
    }

    private final Action action;
    private final Proportion proportion;
    private final String reason;
    private final double confidence; // 决策置信度 0.0-1.0

    private TradingDecision(Action action, Proportion proportion, String reason, double confidence) {
        this.action = action;
        this.proportion = proportion;
        this.reason = reason;
        this.confidence = Math.max(0.0, Math.min(1.0, confidence)); // 限制在 0-1 范围
    }

    /**
     * 创建买入决策
     */
    public static TradingDecision buy(Proportion proportion, String reason) {
        return new TradingDecision(Action.BUY, proportion, reason, 1.0);
    }

    /**
     * 创建买入决策（带置信度）
     */
    public static TradingDecision buy(Proportion proportion, String reason, double confidence) {
        return new TradingDecision(Action.BUY, proportion, reason, confidence);
    }

    /**
     * 创建卖出决策
     */
    public static TradingDecision sell(Proportion proportion, String reason) {
        return new TradingDecision(Action.SELL, proportion, reason, 1.0);
    }

    /**
     * 创建卖出决策（带置信度）
     */
    public static TradingDecision sell(Proportion proportion, String reason, double confidence) {
        return new TradingDecision(Action.SELL, proportion, reason, confidence);
    }

    /**
     * 创建持有决策
     */
    public static TradingDecision hold(String reason) {
        return new TradingDecision(Action.HOLD, null, reason, 1.0);
    }

    /**
     * 创建持有决策（带置信度）
     */
    public static TradingDecision hold(String reason, double confidence) {
        return new TradingDecision(Action.HOLD, null, reason, confidence);
    }

    // Getters

    public Action getAction() {
        return action;
    }

    public Proportion getProportion() {
        return proportion;
    }

    public String getReason() {
        return reason;
    }

    public double getConfidence() {
        return confidence;
    }

    // 便利方法

    public boolean isBuy() {
        return action == Action.BUY;
    }

    public boolean isSell() {
        return action == Action.SELL;
    }

    public boolean isHold() {
        return action == Action.HOLD;
    }

    public boolean isHighConfidence() {
        return confidence >= 0.8;
    }

    public boolean isLowConfidence() {
        return confidence <= 0.3;
    }

    @Override
    public String toString() {
        if (isHold()) {
            return String.format("TradingDecision{action=%s, reason='%s', confidence=%.2f}", action, reason,
                    confidence);
        } else {
            return String.format("TradingDecision{action=%s, proportion=%s, reason='%s', confidence=%.2f}", action,
                    proportion, reason, confidence);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        TradingDecision that = (TradingDecision) obj;
        return Double.compare(that.confidence, confidence) == 0 && action == that.action
                && (proportion != null ? proportion.equals(that.proportion) : that.proportion == null)
                && (reason != null ? reason.equals(that.reason) : that.reason == null);
    }

    @Override
    public int hashCode() {
        int result = action != null ? action.hashCode() : 0;
        result = 31 * result + (proportion != null ? proportion.hashCode() : 0);
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        long temp = Double.doubleToLongBits(confidence);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
