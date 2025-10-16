package com.gantenx.nethuns.engine.export;

import static com.gantenx.nethuns.commons.constant.Constants.ORDER_LIST;
import static com.gantenx.nethuns.commons.constant.Constants.RECORD_LIST;
import static com.gantenx.nethuns.commons.constant.Constants.TRADE_DETAIL;
import java.util.Collections;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import com.gantenx.nethuns.commons.utils.ExcelUtils;
import com.gantenx.nethuns.engine.model.TradeRecord;
import lombok.extern.slf4j.Slf4j;

/**
 * 交易报告生成器
 * 负责生成Excel格式的交易报告
 */
@Slf4j
public class ReportGenerator {

    private final String filePrefix;
    private final boolean includeCharts;

    public ReportGenerator() {
        this("trade_report", false);
    }

    public ReportGenerator(String filePrefix) {
        this(filePrefix, false);
    }

    public ReportGenerator(String filePrefix, boolean includeCharts) {
        this.filePrefix = filePrefix;
        this.includeCharts = includeCharts;
    }

    /**
     * 生成交易报告
     *
     * @param tradeRecord 交易记录
     * @return 生成的文件ID
     */
    public String generateReport(TradeRecord tradeRecord) {
        return generateReport(tradeRecord, generateUniqueId());
    }

    /**
     * 生成交易报告（指定文件ID）
     *
     * @param tradeRecord 交易记录
     * @param fileId      文件标识符
     * @return 生成的文件ID
     */
    public String generateReport(TradeRecord tradeRecord, String fileId) {
        if (tradeRecord == null) {
            log.warn("Trade record is null, skipping report generation");
            return null;
        }

        try {
            log.info("Generating trade report with ID: {}", fileId);

            // 创建工作簿并添加交易汇总
            Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(tradeRecord), TRADE_DETAIL);

            // 添加订单详情
            if (tradeRecord.getOrders() != null && !tradeRecord.getOrders().isEmpty()) {
                ExcelUtils.addDataToNewSheet(workbook, tradeRecord.getOrders(), ORDER_LIST);
                log.debug("Added {} orders to report", tradeRecord.getOrders().size());
            }

            // 添加交易记录
            if (tradeRecord.getRecords() != null && !tradeRecord.getRecords().isEmpty()) {
                ExcelUtils.addDataToNewSheet(workbook, tradeRecord.getRecords(), RECORD_LIST);
                log.debug("Added {} trade records to report", tradeRecord.getRecords().size());
            }

            // 导出Excel文件
            String fileName = String.format("%s_%s", filePrefix, fileId);
            ExcelUtils.exportWorkbook(workbook, fileName);

            log.info("Trade report generated successfully: {}.xlsx", fileName);
            return fileId;

        } catch (Exception e) {
            log.error("Failed to generate trade report for ID: {}", fileId, e);
            throw new ReportGenerationException("Failed to generate trade report", e);
        }
    }

    /**
     * 生成简化报告（仅汇总信息）
     *
     * @param tradeRecord 交易记录
     * @return 生成的文件ID
     */
    public String generateSummaryReport(TradeRecord tradeRecord) {
        return generateSummaryReport(tradeRecord, generateUniqueId());
    }

    /**
     * 生成简化报告（仅汇总信息，指定文件ID）
     *
     * @param tradeRecord 交易记录
     * @param fileId      文件标识符
     * @return 生成的文件ID
     */
    public String generateSummaryReport(TradeRecord tradeRecord, String fileId) {
        if (tradeRecord == null) {
            log.warn("Trade record is null, skipping summary report generation");
            return null;
        }

        try {
            log.info("Generating trade summary report with ID: {}", fileId);

            // 创建仅包含汇总信息的工作簿
            Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(tradeRecord), TRADE_DETAIL);

            // 导出Excel文件
            String fileName = String.format("%s_summary_%s", filePrefix, fileId);
            ExcelUtils.exportWorkbook(workbook, fileName);

            log.info("Trade summary report generated successfully: {}.xlsx", fileName);
            return fileId;

        } catch (Exception e) {
            log.error("Failed to generate trade summary report for ID: {}", fileId, e);
            throw new ReportGenerationException("Failed to generate trade summary report", e);
        }
    }

    /**
     * 获取报告统计信息
     *
     * @param tradeRecord 交易记录
     * @return 统计信息字符串
     */
    public String getReportStatistics(TradeRecord tradeRecord) {
        if (tradeRecord == null) {
            return "No trade record available";
        }

        StringBuilder stats = new StringBuilder();
        stats.append("Trade Report Statistics:\n");
        stats.append(String.format("  Initial Balance: %.2f\n", tradeRecord.getInitialBalance()));
        stats.append(String.format("  Final Balance: %.2f\n", tradeRecord.getBalance()));
        stats.append(String.format("  Total Profit/Loss: %.2f\n",
                tradeRecord.getBalance() - tradeRecord.getInitialBalance()));
        stats.append(String.format("  Return Rate: %.2f%%\n",
                ((tradeRecord.getBalance() - tradeRecord.getInitialBalance()) / tradeRecord.getInitialBalance())
                        * 100));
        stats.append(String.format("  Total Fees: %.2f\n", tradeRecord.getFeeCount()));
        stats.append(String.format("  Total Orders: %d\n",
                tradeRecord.getOrders() != null ? tradeRecord.getOrders().size() : 0));
        stats.append(String.format("  Total Trades: %d\n",
                tradeRecord.getRecords() != null ? tradeRecord.getRecords().size() : 0));

        return stats.toString();
    }

    /**
     * 生成唯一的文件ID
     */
    private String generateUniqueId() {
        return UUID.randomUUID().toString().substring(0, 16);
    }

    // Getters

    public String getFilePrefix() {
        return filePrefix;
    }

    public boolean isIncludeCharts() {
        return includeCharts;
    }

    /**
     * 报告生成异常
     */
    public static class ReportGenerationException extends RuntimeException {
        public ReportGenerationException(String message) {
            super(message);
        }

        public ReportGenerationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
