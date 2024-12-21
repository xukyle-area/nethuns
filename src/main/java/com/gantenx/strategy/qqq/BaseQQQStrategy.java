package com.gantenx.strategy.qqq;

import com.gantenx.engine.*;
import com.gantenx.constant.QQQSymbol;
import com.gantenx.model.Kline;
import com.gantenx.calculator.Profit;
import com.gantenx.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.*;

import static com.gantenx.constant.QQQSymbol.*;

@Slf4j
public abstract class BaseQQQStrategy {
    protected final String strategyName;
    protected TradeDetail<QQQSymbol> tradeDetail;
    protected final Map<QQQSymbol, Map<Long, Kline>> klineMap;
    protected TradeEngine<QQQSymbol> tradeEngine;
    protected String startStr;
    protected String endStr;

    public BaseQQQStrategy(String strategyName, String startStr, String endStr) {
        this.strategyName = strategyName;
        this.startStr = startStr;
        this.endStr = endStr;
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Map<QQQSymbol, Map<Long, Kline>> klineMap = klineMap(Arrays.asList(TQQQ, QQQ, SQQQ), start, end);
        this.klineMap = klineMap;
        List<Long> timestamps = CollectionUtils.getTimestamps(klineMap.get(QQQ));
        tradeEngine = new TradeEngine<>(timestamps, klineMap);
    }

    public static Map<QQQSymbol, Map<Long, Kline>> klineMap(List<QQQSymbol> list,
                                                            long start,
                                                            long end) {
        HashMap<QQQSymbol, Map<Long, Kline>> map = new HashMap<>();
        for (QQQSymbol cryptoSymbol : list) {
            Map<Long, Kline> kLineMap = CsvUtils.getKLineMap(cryptoSymbol, start, end);
            map.put(cryptoSymbol, kLineMap);
        }
        return map;
    }

    private void printTradeDetail() {
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(tradeDetail), "trade-detail");
        ExcelUtils.addDataToNewSheet(workbook, tradeDetail.getOrders(), "order-list");
        ExcelUtils.addDataToNewSheet(workbook, tradeDetail.getRecords(), "record-list");
        List<Profit<QQQSymbol>> profitList = OrderCalculator.calculateProfitAndHoldingDays(tradeDetail.getOrders());
        ExcelUtils.addDataToNewSheet(workbook, profitList, "profit-list");
        ExportUtils.exportWorkbook(workbook, startStr, endStr, strategyName, "result");
        JFreeChart tradingChart = getTradingChart();
        if (Objects.nonNull(tradingChart)) {
            ExportUtils.saveJFreeChartAsImage(tradingChart, startStr, endStr, strategyName, "lines", 2400, 1200);
        }
    }

    protected JFreeChart getTradingChart() {
        return null;
    }

    public void process() {
        this.openTrade();
        tradeDetail = tradeEngine.exit();
        printTradeDetail();
    }
    protected abstract void openTrade();
}
