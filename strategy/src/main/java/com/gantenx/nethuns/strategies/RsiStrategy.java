package com.gantenx.nethuns.strategies;

import java.util.Map;
import com.gantenx.nethuns.CandleService;
import com.gantenx.nethuns.commons.enums.Period;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.engine.config.ExecutorConfig;
import com.gantenx.nethuns.engine.executor.TradeExecutor;
import com.gantenx.nethuns.engine.executor.strategy.RuleBasedTradingStrategy;
import com.gantenx.nethuns.engine.indicator.RsiIndicator;
import com.gantenx.nethuns.rule.CrossedDownIndicatorRule;
import com.gantenx.nethuns.rule.CrossedUpIndicatorRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsiStrategy {

    public static void main(String[] args) {
        Symbol symbol = Symbol.QQQUSD;
        String startStr = "20240101";
        String endStr = "20241001";
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Map<Long, Candle> klineMap = CandleService.getKLineMap(symbol, Period.D_1, start, end);

        // 生成RSI指标图表
        RsiIndicator rsiIndicator = new RsiIndicator(klineMap);
        ExportUtils.saveJFreeChartAsImage(rsiIndicator.getChart(), "RSI");

        // 创建交易规则
        // 跌破 30 - 买入信号
        CrossedDownIndicatorRule buyRule = new CrossedDownIndicatorRule(rsiIndicator, 30.0d);
        // 涨破 70 - 卖出信号
        CrossedUpIndicatorRule sellRule = new CrossedUpIndicatorRule(rsiIndicator, 70.0d);

        // 配置交易参数
        ExecutorConfig config =
                ExecutorConfig.defaultConfig().setVerboseLogging(true).setExportFilePrefix("rsi_strategy_result");

        // 创建基于规则的交易策略
        RuleBasedTradingStrategy strategy = new RuleBasedTradingStrategy("RSI Strategy", buyRule, sellRule, config);

        // 创建并执行交易执行器
        TradeExecutor tradeExecutor = new TradeExecutor(klineMap, symbol, strategy, config);

        log.info("Starting RSI strategy execution...");
        String exportId = TradeExecutor.processAndExport(tradeExecutor);

        // 输出执行结果摘要
        log.info("RSI strategy execution completed:");
        log.info(tradeExecutor.getExecutionSummary());
        log.info("Results exported with ID: {}", exportId);
    }
}
