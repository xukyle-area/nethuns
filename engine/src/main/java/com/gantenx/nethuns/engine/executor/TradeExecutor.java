package com.gantenx.nethuns.engine.executor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jfree.chart.JFreeChart;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.engine.TradeEngine;
import com.gantenx.nethuns.engine.config.ExecutorConfig;
import com.gantenx.nethuns.engine.exception.ExecutionException;
import com.gantenx.nethuns.engine.exception.ExportException;
import com.gantenx.nethuns.engine.executor.strategy.TradingDecision;
import com.gantenx.nethuns.engine.executor.strategy.TradingStrategy;
import com.gantenx.nethuns.engine.export.ChartExporter;
import com.gantenx.nethuns.engine.export.ReportGenerator;
import com.gantenx.nethuns.engine.model.TradeRecord;
import com.gantenx.nethuns.engine.result.ExecutionResult;
import com.gantenx.nethuns.engine.result.ExecutionStatistics;
import lombok.extern.slf4j.Slf4j;

/**
 * 交易执行器（重构版）
 * 采用组件化架构，分离交易逻辑、报告生成和图表导出
 */
@Slf4j
public class TradeExecutor {

    // 核心组件
    private final TradeEngine tradeEngine;
    private final TradingStrategy strategy;
    private final ExecutorConfig config;

    // 导出组件
    private final ReportGenerator reportGenerator;
    private final ChartExporter chartExporter;

    // 交易数据
    private final Symbol symbol;
    private final Map<Long, Candle> klineMap;
    private final List<Long> timeList;

    // 状态管理
    private TradeRecord tradeRecord;
    private ExecutionStatistics statistics;
    private boolean executed = false;

    /**
     * 主要构造器
     *
     * @param klineMap  K线数据
     * @param symbol    交易标的
     * @param strategy  交易策略
     * @param config    执行器配置
     */
    public TradeExecutor(Map<Long, Candle> klineMap, Symbol symbol, TradingStrategy strategy, ExecutorConfig config) {
        this.symbol = symbol;
        this.klineMap = klineMap;
        this.strategy = strategy;
        this.config = config;

        // 初始化时间序列
        this.timeList = klineMap.keySet().stream().sorted().collect(Collectors.toList());

        // 初始化交易引擎
        this.tradeEngine = new TradeEngine(timeList, Collections.singletonMap(symbol, klineMap));

        // 初始化导出组件
        this.reportGenerator = new ReportGenerator(config.getExportFilePrefix(), config.isGenerateChart());
        this.chartExporter = new ChartExporter(config.getExportFilePrefix() + "_chart", config.isGenerateChart());

        // 初始化统计信息
        this.statistics = new ExecutionStatistics();

        log.info("TradeExecutor initialized: strategy={}, symbol={}, config={}", strategy.getName(), symbol, config);
    }

    /**
     * 简化构造器（使用默认配置）
     */
    public TradeExecutor(Map<Long, Candle> klineMap, Symbol symbol, TradingStrategy strategy) {
        this(klineMap, symbol, strategy, ExecutorConfig.defaultConfig());
    }

    /**
     * 执行交易策略
     *
     * @return 执行结果
     */
    public ExecutionResult execute() {
        if (executed) {
            log.warn("Trade executor has already been executed, returning cached result");
            return createExecutionResult();
        }

        log.info("Starting strategy execution: {}", strategy.getName());
        statistics.startExecution();

        try {
            // 策略初始化
            strategy.initialize(tradeEngine, symbol);

            // 执行交易循环
            executeTradeLoop();

            // 策略清理
            strategy.cleanup(tradeEngine, symbol);

            // 结束交易并获取记录
            this.tradeRecord = tradeEngine.exit();
            this.executed = true;

            statistics.endExecution();
            log.info("Strategy execution completed successfully: {}", getExecutionSummary());

            return createExecutionResult();

        } catch (Exception e) {
            statistics.recordError(e);
            log.error("Strategy execution failed: {}", strategy.getName(), e);
            throw new ExecutionException("Strategy execution failed", e);
        }
    }

    /**
     * 执行交易循环
     */
    private void executeTradeLoop() {
        int tradeCount = 0;
        int maxTrades = config.getMaxTradeCount();

        while (tradeEngine.hasNext()) {
            long currentTime = tradeEngine.next();
            statistics.incrementTimeStep();

            // 检查最大交易次数限制
            if (maxTrades > 0 && tradeCount >= maxTrades) {
                log.info("Reached maximum trade count limit: {}", maxTrades);
                break;
            }

            try {
                // 获取策略决策
                TradingDecision decision = strategy.decide(tradeEngine, symbol, currentTime);
                statistics.recordDecision(decision);

                // 执行交易决策
                if (executeDecision(decision)) {
                    tradeCount++;
                    statistics.incrementTradeCount();
                }

                // 详细日志
                if (config.isVerboseLogging() && !decision.isHold()) {
                    log.debug("Time: {}, Decision: {}, Balance: {:.2f}", currentTime, decision,
                            tradeEngine.getBalance());
                }

            } catch (Exception e) {
                statistics.recordDecisionError(e);
                log.warn("Error in trading decision at time {}: {}", currentTime, e.getMessage());
                // 继续执行，不中断整个流程
            }
        }

        log.info("Trade loop completed: {} time steps, {} trades executed", statistics.getTimeSteps(),
                statistics.getTradeCount());
    }

    /**
     * 执行交易决策
     *
     * @param decision 交易决策
     * @return 是否执行了实际交易
     */
    private boolean executeDecision(TradingDecision decision) {
        if (decision.isHold()) {
            return false;
        }

        try {
            if (decision.isBuy()) {
                tradeEngine.buy(symbol, decision.getProportion());
                log.debug("Executed BUY: {} - {}", decision.getProportion(), decision.getReason());
                return true;
            } else if (decision.isSell()) {
                tradeEngine.sell(symbol, decision.getProportion());
                log.debug("Executed SELL: {} - {}", decision.getProportion(), decision.getReason());
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to execute decision: {}", decision, e);
            throw e;
        }

        return false;
    }

    /**
     * 导出结果（Excel + 图表）
     *
     * @return 导出文件ID
     */
    public String export() {
        if (!executed) {
            log.warn("Cannot export before execution, running execute() first");
            execute();
        }

        if (!config.isAutoExport()) {
            log.info("Auto export is disabled, skipping export");
            return null;
        }

        try {
            log.info("Starting result export");

            // 生成报告
            String reportId = reportGenerator.generateReport(tradeRecord);

            // 导出图表
            chartExporter.exportChart(symbol, klineMap, tradeRecord, reportId);

            log.info("Export completed successfully with ID: {}", reportId);
            return reportId;

        } catch (Exception e) {
            log.error("Failed to export results", e);
            throw new ExportException("Failed to export results", e);
        }
    }

    /**
     * 仅导出报告
     */
    public String exportReport() {
        if (!executed) {
            execute();
        }
        return reportGenerator.generateReport(tradeRecord);
    }

    /**
     * 仅导出图表
     */
    public String exportChart() {
        if (!executed) {
            execute();
        }
        return chartExporter.exportChart(symbol, klineMap, tradeRecord);
    }

    /**
     * 获取交易图表对象
     */
    public JFreeChart getChart() {
        if (!executed) {
            execute();
        }
        return chartExporter.createChart(symbol, klineMap, tradeRecord);
    }

    /**
     * 获取交易记录
     */
    public TradeRecord getTradeRecord() {
        if (!executed) {
            execute();
        }
        return tradeRecord;
    }

    /**
     * 获取执行统计信息
     */
    public ExecutionStatistics getStatistics() {
        return statistics;
    }

    /**
     * 获取执行摘要
     */
    public String getExecutionSummary() {
        if (!executed) {
            return "Strategy not executed yet";
        }

        return String.format(
                "Strategy: %s | Symbol: %s | Duration: %dms | Trades: %d | "
                        + "Initial: %.2f | Final: %.2f | Return: %.2f%%",
                strategy.getName(), symbol, statistics.getExecutionDuration(), statistics.getTradeCount(),
                tradeRecord.getInitialBalance(), tradeRecord.getBalance(),
                ((tradeRecord.getBalance() - tradeRecord.getInitialBalance()) / tradeRecord.getInitialBalance()) * 100);
    }

    /**
     * 创建执行结果
     */
    private ExecutionResult createExecutionResult() {
        return new ExecutionResult(tradeRecord, statistics, getExecutionSummary());
    }

    // Getters

    public Symbol getSymbol() {
        return symbol;
    }

    public TradingStrategy getStrategy() {
        return strategy;
    }

    public ExecutorConfig getConfig() {
        return config;
    }

    public boolean isExecuted() {
        return executed;
    }

    /**
     * 静态便利方法：执行并导出
     */
    public static String processAndExport(TradeExecutor executor) {
        executor.execute();
        return executor.export();
    }

    /**
     * 静态便利方法：仅执行
     */
    public static ExecutionResult process(TradeExecutor executor) {
        return executor.execute();
    }
}
