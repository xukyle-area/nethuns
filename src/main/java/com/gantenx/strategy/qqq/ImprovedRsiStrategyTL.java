package com.gantenx.strategy.qqq;

public class ImprovedRsiStrategyTL {
    // RSI参数
    static final double EXTREME_OVERSOLD = 25.0;
    static final double EXTREME_OVERBOUGHT = 85.0;
    public static final double NEUTRAL_LEVEL = 60.0;
    static final int RSI_PERIOD = 6;

    // 趋势参数
    static final int TREND_PERIOD = 20;
    static final int FAST_EMA_PERIOD = 5;
    static final double TREND_THRESHOLD = 0.02;

    // 止损参数
    public static final double STOP_LOSS_THRESHOLD = 0.05;
    public static final double TRAILING_STOP = 0.08;

    // 持仓时间限制
    public static final int MAX_LEVERAGED_HOLDING_DAYS = 5 * 24 * 3600 * 1000;

    // 交易控制参数
    public static final long MIN_TRADE_INTERVAL = 24 * 60 * 60 * 1000; // 1天
    public static final double MAX_VOLATILITY_THRESHOLD = 0.03;

    // 风险控制参数
    static final double STOP_LOSS = 0.05;
    static final long MAX_HOLD_TIME = 5 * 24 * 3600 * 1000;
    static final double MAX_VOLATILITY = 0.03;

    // 交易确认参数
    static final int MIN_CONSECUTIVE_DAYS = 2;
    static final int VOLUME_LOOKBACK_DAYS = 20;
    static final double MIN_VOLUME_RATIO = 0.5;
    static final int MIN_CONFIRMATION_COUNT = 3;
}