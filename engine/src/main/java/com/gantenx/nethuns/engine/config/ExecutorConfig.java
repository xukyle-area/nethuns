package com.gantenx.nethuns.engine.config;

import com.gantenx.nethuns.commons.enums.Proportion;

/**
 * 交易执行器配置
 * 定义交易执行的各种参数和策略配置
 */
public class ExecutorConfig {

    // 默认买入比例
    private Proportion defaultBuyProportion = Proportion.PROPORTION_OF_100;

    // 默认卖出比例
    private Proportion defaultSellProportion = Proportion.PROPORTION_OF_100;

    // 是否启用自动导出
    private boolean autoExport = true;

    // 导出文件前缀
    private String exportFilePrefix = "trade_result";

    // 是否生成图表
    private boolean generateChart = true;

    // 是否启用详细日志
    private boolean verboseLogging = false;

    // 最大交易次数限制（0表示无限制）
    private int maxTradeCount = 0;

    // 止损比例（0表示不启用止损）
    private double stopLossPercentage = 0.0;

    // 止盈比例（0表示不启用止盈）
    private double takeProfitPercentage = 0.0;

    /**
     * 默认配置构造器
     */
    public ExecutorConfig() {}

    /**
     * 自定义配置构造器
     */
    public ExecutorConfig(Proportion buyProportion, Proportion sellProportion) {
        this.defaultBuyProportion = buyProportion;
        this.defaultSellProportion = sellProportion;
    }

    // Getters and Setters

    public Proportion getDefaultBuyProportion() {
        return defaultBuyProportion;
    }

    public ExecutorConfig setDefaultBuyProportion(Proportion defaultBuyProportion) {
        this.defaultBuyProportion = defaultBuyProportion;
        return this;
    }

    public Proportion getDefaultSellProportion() {
        return defaultSellProportion;
    }

    public ExecutorConfig setDefaultSellProportion(Proportion defaultSellProportion) {
        this.defaultSellProportion = defaultSellProportion;
        return this;
    }

    public boolean isAutoExport() {
        return autoExport;
    }

    public ExecutorConfig setAutoExport(boolean autoExport) {
        this.autoExport = autoExport;
        return this;
    }

    public String getExportFilePrefix() {
        return exportFilePrefix;
    }

    public ExecutorConfig setExportFilePrefix(String exportFilePrefix) {
        this.exportFilePrefix = exportFilePrefix;
        return this;
    }

    public boolean isGenerateChart() {
        return generateChart;
    }

    public ExecutorConfig setGenerateChart(boolean generateChart) {
        this.generateChart = generateChart;
        return this;
    }

    public boolean isVerboseLogging() {
        return verboseLogging;
    }

    public ExecutorConfig setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
        return this;
    }

    public int getMaxTradeCount() {
        return maxTradeCount;
    }

    public ExecutorConfig setMaxTradeCount(int maxTradeCount) {
        this.maxTradeCount = maxTradeCount;
        return this;
    }

    public double getStopLossPercentage() {
        return stopLossPercentage;
    }

    public ExecutorConfig setStopLossPercentage(double stopLossPercentage) {
        this.stopLossPercentage = stopLossPercentage;
        return this;
    }

    public double getTakeProfitPercentage() {
        return takeProfitPercentage;
    }

    public ExecutorConfig setTakeProfitPercentage(double takeProfitPercentage) {
        this.takeProfitPercentage = takeProfitPercentage;
        return this;
    }

    /**
     * 创建默认配置
     */
    public static ExecutorConfig defaultConfig() {
        return new ExecutorConfig();
    }

    /**
     * 创建保守配置（小比例交易）
     */
    public static ExecutorConfig conservativeConfig() {
        return new ExecutorConfig().setDefaultBuyProportion(Proportion.PROPORTION_OF_25)
                .setDefaultSellProportion(Proportion.PROPORTION_OF_50).setStopLossPercentage(5.0)
                .setTakeProfitPercentage(10.0);
    }

    /**
     * 创建激进配置（全仓交易）
     */
    public static ExecutorConfig aggressiveConfig() {
        return new ExecutorConfig().setDefaultBuyProportion(Proportion.PROPORTION_OF_100)
                .setDefaultSellProportion(Proportion.PROPORTION_OF_100).setVerboseLogging(true);
    }

    /**
     * 创建测试配置（无导出）
     */
    public static ExecutorConfig testConfig() {
        return new ExecutorConfig().setAutoExport(false).setGenerateChart(false).setVerboseLogging(true);
    }

    @Override
    public String toString() {
        return String.format(
                "ExecutorConfig{buyProportion=%s, sellProportion=%s, autoExport=%s, "
                        + "generateChart=%s, maxTrades=%d, stopLoss=%.2f%%, takeProfit=%.2f%%}",
                defaultBuyProportion, defaultSellProportion, autoExport, generateChart, maxTradeCount,
                stopLossPercentage, takeProfitPercentage);
    }
}
