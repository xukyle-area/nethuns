package com.gantenx.nethuns.engine.executor.strategy;

import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.engine.TradeEngine;

/**
 * 交易策略接口
 * 定义了交易策略的核心方法和决策逻辑
 */
public interface TradingStrategy {

    /**
     * 策略名称
     */
    String getName();

    /**
     * 在指定时间戳执行交易决策
     *
     * @param engine    交易引擎
     * @param symbol    交易标的
     * @param timestamp 当前时间戳
     * @return 交易决策结果
     */
    TradingDecision decide(TradeEngine engine, Symbol symbol, long timestamp);

    /**
     * 策略初始化（可选）
     *
     * @param engine 交易引擎
     * @param symbol 交易标的
     */
    default void initialize(TradeEngine engine, Symbol symbol) {
        // 默认不需要初始化
    }

    /**
     * 策略清理（可选）
     *
     * @param engine 交易引擎
     * @param symbol 交易标的
     */
    default void cleanup(TradeEngine engine, Symbol symbol) {
        // 默认不需要清理
    }

    /**
     * 是否支持多标的交易
     */
    default boolean supportsMultipleSymbols() {
        return false;
    }

    /**
     * 获取策略描述
     */
    default String getDescription() {
        return "Trading strategy: " + getName();
    }
}
