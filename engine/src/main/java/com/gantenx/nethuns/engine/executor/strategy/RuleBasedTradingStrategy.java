package com.gantenx.nethuns.engine.executor.strategy;

import com.gantenx.nethuns.commons.enums.Proportion;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.engine.TradeEngine;
import com.gantenx.nethuns.engine.config.ExecutorConfig;
import com.gantenx.nethuns.engine.rule.Rule;


/**
 * 基于规则的交易策略实现
 * 使用买入规则和卖出规则来制定交易决策
 */
public class RuleBasedTradingStrategy implements TradingStrategy {

    private final String name;
    private final Rule entryRule;
    private final Rule exitRule;
    private final ExecutorConfig config;

    public RuleBasedTradingStrategy(String name, Rule entryRule, Rule exitRule, ExecutorConfig config) {
        this.name = name;
        this.entryRule = entryRule;
        this.exitRule = exitRule;
        this.config = config;
    }

    public RuleBasedTradingStrategy(Rule entryRule, Rule exitRule, ExecutorConfig config) {
        this("RuleBasedStrategy", entryRule, exitRule, config);
    }

    public RuleBasedTradingStrategy(Rule entryRule, Rule exitRule) {
        this("RuleBasedStrategy", entryRule, exitRule, ExecutorConfig.defaultConfig());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TradingDecision decide(TradeEngine engine, Symbol symbol, long timestamp) {
        // 检查买入条件
        if (entryRule.isSatisfied(timestamp)) {
            // 如果已有持仓且配置不允许加仓，则不买入
            if (engine.getQuantity(symbol) > 0) {
                return TradingDecision.hold("Already holding position");
            }

            return TradingDecision.buy(config.getDefaultBuyProportion(), "Entry rule satisfied", 0.8);
        }

        // 检查卖出条件
        if (exitRule.isSatisfied(timestamp)) {
            // 如果没有持仓，则不卖出
            if (engine.getQuantity(symbol) <= 0) {
                return TradingDecision.hold("No position to sell");
            }

            return TradingDecision.sell(config.getDefaultSellProportion(), "Exit rule satisfied", 0.8);
        }

        // 检查止损止盈
        if (engine.getQuantity(symbol) > 0) {
            TradingDecision stopDecision = checkStopLossAndTakeProfit(engine, symbol);
            if (!stopDecision.isHold()) {
                return stopDecision;
            }
        }

        return TradingDecision.hold("No trading signal");
    }

    /**
     * 检查止损止盈条件
     */
    private TradingDecision checkStopLossAndTakeProfit(TradeEngine engine, Symbol symbol) {
        double stopLoss = config.getStopLossPercentage();
        double takeProfit = config.getTakeProfitPercentage();

        if (stopLoss <= 0 && takeProfit <= 0) {
            return TradingDecision.hold("No stop loss/take profit configured");
        }

        // 这里需要计算当前持仓的盈亏百分比
        // 简化实现，实际应该计算持仓的平均成本和当前价格
        double currentBalance = engine.getBalance();
        double positionValue = engine.getPositionAsset();
        double totalValue = currentBalance + positionValue;

        // 假设初始资金来自配置或引擎
        double initialBalance = engine.getConfig().getInitialBalance();
        double profitLossPercentage = ((totalValue - initialBalance) / initialBalance) * 100;

        if (stopLoss > 0 && profitLossPercentage <= -stopLoss) {
            return TradingDecision.sell(Proportion.PROPORTION_OF_100,
                    String.format("Stop loss triggered: %.2f%%", profitLossPercentage), 1.0);
        }

        if (takeProfit > 0 && profitLossPercentage >= takeProfit) {
            return TradingDecision.sell(Proportion.PROPORTION_OF_100,
                    String.format("Take profit triggered: %.2f%%", profitLossPercentage), 1.0);
        }

        return TradingDecision.hold("Within stop loss/take profit range");
    }

    @Override
    public String getDescription() {
        return String.format("%s with entry rule and exit rule, stopLoss=%.2f%%, takeProfit=%.2f%%", name,
                config.getStopLossPercentage(), config.getTakeProfitPercentage());
    }

    // Getters for testing

    public Rule getEntryRule() {
        return entryRule;
    }

    public Rule getExitRule() {
        return exitRule;
    }

    public ExecutorConfig getConfig() {
        return config;
    }
}
