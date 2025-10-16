package com.gantenx.nethuns.engine.export;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import com.gantenx.nethuns.commons.enums.Series;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.engine.model.Order;
import com.gantenx.nethuns.engine.model.TradeRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 图表导出器
 * 负责生成和导出交易图表
 */
@Slf4j
public class ChartExporter {

    private final String filePrefix;
    private final boolean exportEnabled;

    public ChartExporter() {
        this("trade_chart", true);
    }

    public ChartExporter(String filePrefix) {
        this(filePrefix, true);
    }

    public ChartExporter(String filePrefix, boolean exportEnabled) {
        this.filePrefix = filePrefix;
        this.exportEnabled = exportEnabled;
    }

    /**
     * 导出交易图表
     *
     * @param symbol       交易标的
     * @param klineMap     K线数据
     * @param tradeRecord  交易记录
     * @return 文件ID，如果导出被禁用则返回null
     */
    public String exportChart(Symbol symbol, Map<Long, Candle> klineMap, TradeRecord tradeRecord) {
        return exportChart(symbol, klineMap, tradeRecord, generateUniqueId());
    }

    /**
     * 导出交易图表（指定文件ID）
     *
     * @param symbol       交易标的
     * @param klineMap     K线数据
     * @param tradeRecord  交易记录
     * @param fileId       文件标识符
     * @return 文件ID，如果导出被禁用则返回null
     */
    public String exportChart(Symbol symbol, Map<Long, Candle> klineMap, TradeRecord tradeRecord, String fileId) {
        if (!exportEnabled) {
            log.debug("Chart export is disabled, skipping chart generation");
            return null;
        }

        if (klineMap == null || klineMap.isEmpty()) {
            log.warn("K-line data is null or empty, skipping chart export");
            return null;
        }

        try {
            log.info("Generating trading chart for symbol {} with ID: {}", symbol, fileId);

            JFreeChart chart = createChart(symbol, klineMap, tradeRecord);
            String fileName = String.format("%s_%s_%s", filePrefix, symbol.name(), fileId);

            ExportUtils.saveJFreeChartAsImage(chart, fileName);

            log.info("Trading chart exported successfully: {}.png", fileName);
            return fileId;

        } catch (Exception e) {
            log.error("Failed to export trading chart for symbol {} with ID: {}", symbol, fileId, e);
            throw new ChartExportException("Failed to export trading chart", e);
        }
    }

    /**
     * 仅导出K线图表（不包含交易标记）
     *
     * @param symbol    交易标的
     * @param klineMap  K线数据
     * @return 文件ID，如果导出被禁用则返回null
     */
    public String exportKlineChart(Symbol symbol, Map<Long, Candle> klineMap) {
        return exportKlineChart(symbol, klineMap, generateUniqueId());
    }

    /**
     * 仅导出K线图表（不包含交易标记，指定文件ID）
     *
     * @param symbol    交易标的
     * @param klineMap  K线数据
     * @param fileId    文件标识符
     * @return 文件ID，如果导出被禁用则返回null
     */
    public String exportKlineChart(Symbol symbol, Map<Long, Candle> klineMap, String fileId) {
        if (!exportEnabled) {
            log.debug("Chart export is disabled, skipping K-line chart generation");
            return null;
        }

        if (klineMap == null || klineMap.isEmpty()) {
            log.warn("K-line data is null or empty, skipping K-line chart export");
            return null;
        }

        try {
            log.info("Generating K-line chart for symbol {} with ID: {}", symbol, fileId);

            XYPlot plot = CandlePlot.create(Series.getSeries(symbol), klineMap);
            JFreeChart chart = Chart.get(plot, null, null);
            String fileName = String.format("%s_kline_%s_%s", filePrefix, symbol.name(), fileId);

            ExportUtils.saveJFreeChartAsImage(chart, fileName);

            log.info("K-line chart exported successfully: {}.png", fileName);
            return fileId;

        } catch (Exception e) {
            log.error("Failed to export K-line chart for symbol {} with ID: {}", symbol, fileId, e);
            throw new ChartExportException("Failed to export K-line chart", e);
        }
    }

    /**
     * 创建包含交易标记的图表
     *
     * @param symbol       交易标的
     * @param klineMap     K线数据
     * @param tradeRecord  交易记录
     * @return JFreeChart对象
     */
    public JFreeChart createChart(Symbol symbol, Map<Long, Candle> klineMap, TradeRecord tradeRecord) {
        if (klineMap == null || klineMap.isEmpty()) {
            throw new IllegalArgumentException("K-line data cannot be null or empty");
        }

        XYPlot mainPlot = CandlePlot.create(Series.getSeries(symbol), klineMap);

        List<Order> orders = null;
        if (tradeRecord != null && tradeRecord.getOrders() != null) {
            orders = tradeRecord.getOrders();
            log.debug("Including {} orders in chart", orders.size());
        }

        return Chart.get(mainPlot, null, orders);
    }

    /**
     * 批量导出图表
     *
     * @param charts 图表映射（文件名 -> 图表对象）
     * @return 成功导出的数量
     */
    public int exportCharts(Map<String, JFreeChart> charts) {
        if (!exportEnabled) {
            log.debug("Chart export is disabled, skipping batch chart export");
            return 0;
        }

        if (charts == null || charts.isEmpty()) {
            log.warn("Charts map is null or empty, nothing to export");
            return 0;
        }

        int successCount = 0;
        for (Map.Entry<String, JFreeChart> entry : charts.entrySet()) {
            try {
                String fileName = String.format("%s_%s", filePrefix, entry.getKey());
                ExportUtils.saveJFreeChartAsImage(entry.getValue(), fileName);
                successCount++;
                log.debug("Chart exported successfully: {}.png", fileName);
            } catch (Exception e) {
                log.error("Failed to export chart: {}", entry.getKey(), e);
            }
        }

        log.info("Batch chart export completed: {}/{} successful", successCount, charts.size());
        return successCount;
    }

    /**
     * 检查导出功能是否启用
     */
    public boolean isExportEnabled() {
        return exportEnabled;
    }

    /**
     * 获取文件前缀
     */
    public String getFilePrefix() {
        return filePrefix;
    }

    /**
     * 生成唯一的文件ID
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    /**
     * 图表导出异常
     */
    public static class ChartExportException extends RuntimeException {
        public ChartExportException(String message) {
            super(message);
        }

        public ChartExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
