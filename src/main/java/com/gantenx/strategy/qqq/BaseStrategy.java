package com.gantenx.strategy.qqq;

import com.gantenx.engine.OrderCalculator;
import com.gantenx.engine.TradeEngine;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.engine.Order;
import com.gantenx.calculator.Profit;
import com.gantenx.engine.TradeDetail;
import com.gantenx.utils.CsvUtils;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Symbol.*;

@Slf4j
public abstract class BaseStrategy {

    protected final double initialBalance;
    protected final double fee;
    protected final String strategyName;
    protected TradeDetail tradeDetail;
    protected Map<Long, Kline> tqqqKlineMap;
    protected Map<Long, Kline> sqqqKlineMap;
    protected Map<Long, Kline> qqqKlineMap;
    protected TradeEngine tradeEngine;
    protected String startStr;
    protected String endStr;

    public BaseStrategy(String strategyName, String startStr, String endStr) {
        this.initialBalance = 10000L;
        this.fee = 0.0001;
        this.strategyName = strategyName;
        this.startStr = startStr;
        this.endStr = endStr;
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        tradeEngine = new TradeEngine(initialBalance, fee);
        tqqqKlineMap = CsvUtils.getKLineMap(TQQQ, start, end);
        sqqqKlineMap = CsvUtils.getKLineMap(SQQQ, start, end);
        qqqKlineMap = CsvUtils.getKLineMap(QQQ, start, end);
    }

    private void printTradeDetail() {
        List<Order> orders = tradeDetail.getOrders();
        String detail = "trade-detail";
        Workbook tradeDetailWorkbook = ExcelUtils.singleSheet(Collections.singletonList(tradeDetail), detail);
        ExportUtils.exportWorkbook(tradeDetailWorkbook, startStr, endStr, strategyName, detail);
        String orderList = "order-list";
        Workbook orderWorkbook = ExcelUtils.singleSheet(orders, orderList);
        ExportUtils.exportWorkbook(orderWorkbook, startStr, endStr, strategyName, orderList);

        Map<String, Profit> results = OrderCalculator.calculateProfitAndHoldingDays(orders);
        for (Map.Entry<String, Profit> entry : results.entrySet()) {
            Profit profit = entry.getValue();
            log.info("{}: holding days:{}, profit:{}", entry.getKey(), profit.getTotalHoldingDays(), profit.getProfit());
        }
        log.info("init balance:{}, finish balance:{}", tradeDetail.getInitialBalance(), tradeDetail.getBalance());
        log.info("fee: {}", tradeDetail.getFeeCount());
        this.saveImage();
    }

    private void saveImage() {
        JFreeChart chart = getChart();
        if (chart != null) {
            ExportUtils.saveJFreeChartAsImage(chart, startStr, endStr, strategyName, "lines", 3600, 1200);
        }
    }

    protected JFreeChart getChart() {
        return null;
    }

    public void process() {
        openTrade();
        HashMap<Symbol, Kline> map = new HashMap<>();
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
