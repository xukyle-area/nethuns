package com.gantenx.nethuns.engine.config;

/**
 * 交易引擎配置类
 * 集中管理所有交易相关的配置参数
 */
public class TradingConfig {

    private final double initialBalance;
    private final double feeRate;
    private final double epsilon;

    /**
     * 默认配置构造器
     */
    public TradingConfig() {
        this(10000.0, 0.001, 1e-6);
    }

    /**
     * 自定义配置构造器
     *
     * @param initialBalance 初始余额
     * @param feeRate        手续费率 (0-1之间)
     * @param epsilon        精度值
     */
    public TradingConfig(double initialBalance, double feeRate, double epsilon) {
        if (initialBalance <= 0) {
            throw new IllegalArgumentException("Initial balance must be positive");
        }
        if (feeRate < 0 || feeRate > 1) {
            throw new IllegalArgumentException("Fee rate must be between 0 and 1");
        }
        if (epsilon <= 0) {
            throw new IllegalArgumentException("Epsilon must be positive");
        }

        this.initialBalance = initialBalance;
        this.feeRate = feeRate;
        this.epsilon = epsilon;
    }

    public double getInitialBalance() {
        return initialBalance;
    }

    public double getFeeRate() {
        return feeRate;
    }

    public double getEpsilon() {
        return epsilon;
    }

    @Override
    public String toString() {
        return String.format("TradingConfig{initialBalance=%.2f, feeRate=%.4f, epsilon=%.2e}", initialBalance, feeRate,
                epsilon);
    }
}
