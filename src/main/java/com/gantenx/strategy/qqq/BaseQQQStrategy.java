package com.gantenx.strategy.qqq;

import com.gantenx.engine.*;
import com.gantenx.constant.QQQSymbol;
import com.gantenx.model.Kline;
import com.gantenx.calculator.Profit;
import com.gantenx.utils.CsvUtils;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.*;

import static com.gantenx.constant.QQQSymbol.*;

@Slf4j
public abstract class BaseQQQStrategy {

    protected final double initialBalance;
    protected final double fee;
    protected final String strategyName;
    protected TradeDetail<QQQSymbol> tradeDetail;
    protected Map<Long, Kline> tqqqKlineMap;
    protected Map<Long, Kline> sqqqKlineMap;
    protected Map<Long, Kline> qqqKlineMap;
    protected TradeEngine<QQQSymbol> tradeEngine;
    protected String startStr;
    protected String endStr;

    public BaseQQQStrategy(String strategyName, String startStr, String endStr) {
        this.initialBalance = 10000L;
        this.fee = 0.0001;
        this.strategyName = strategyName;
        this.startStr = startStr;
        this.endStr = endStr;
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        tradeEngine = new TradeEngine<>(initialBalance, fee);
        tqqqKlineMap = CsvUtils.getKLineMap(TQQQ, start, end);
        sqqqKlineMap = CsvUtils.getKLineMap(SQQQ, start, end);
        qqqKlineMap = CsvUtils.getKLineMap(QQQ, start, end);
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
            ExportUtils.saveJFreeChartAsImage(tradingChart, startStr, endStr, strategyName, "lines", 3600, 1200);
        }
    }

    protected JFreeChart getTradingChart() {
        return null;
    }

    public void process() {
        openTrade();
        HashMap<QQQSymbol, Kline> map = new HashMap<>();
        map.put(TQQQ, findLatestKline(tqqqKlineMap));
        map.put(QQQ, findLatestKline(qqqKlineMap));
        map.put(SQQQ, findLatestKline(sqqqKlineMap));
        tradeDetail = tradeEngine.exit(map, findLatestTime(qqqKlineMap));
        printTradeDetail();
    }

    public static Kline findLatestKline(Map<Long, Kline> klineMap) {
        if (klineMap == null || klineMap.isEmpty()) {
            return null;
        }

        Long maxTimestamp = Collections.max(klineMap.keySet());
        return klineMap.get(maxTimestamp);
    }

    public static Long findLatestTime(Map<Long, Kline> klineMap) {
        if (klineMap == null || klineMap.isEmpty()) {
            return null;
        }

        return Collections.max(klineMap.keySet());
    }


    protected abstract void openTrade();
}
