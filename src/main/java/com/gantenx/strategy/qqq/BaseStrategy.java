package com.gantenx.strategy.qqq;

import com.gantenx.calculator.OrderCalculator;
import com.gantenx.calculator.TradeMocker;
import com.gantenx.constant.SymbolType;
import com.gantenx.model.*;
import com.gantenx.utils.CsvUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class BaseStrategy {

    protected final double initialBalance;
    protected final double fee;
    protected final String strategyName;
    protected TradeDetail tradeDetail;
    protected Map<Long, Kline> tqqqKlineMap;
    protected Map<Long, Kline> sqqqKlineMap;
    protected Map<Long, Kline> qqqKlineMap;
    protected TradeMocker tradeMocker;

    public BaseStrategy(double initialBalance, double fee, String strategyName, long start, long end) {
        this.initialBalance = initialBalance;
        this.fee = fee;
        this.strategyName = strategyName;
        tradeMocker = new TradeMocker(initialBalance, fee);
        tqqqKlineMap = CsvUtils.getKLineMap(SymbolType.TQQQ, start, end);
        sqqqKlineMap = CsvUtils.getKLineMap(SymbolType.SQQQ, start, end);
        qqqKlineMap = CsvUtils.getKLineMap(SymbolType.QQQ, start, end);
    }

    public void printTradeDetail() {
        List<Order> orders = tradeDetail.getOrders();
        Workbook tradeDetailWorkbook = ExcelUtils.singleSheet(Collections.singletonList(tradeDetail), "trade-detail");
        ExportUtils.exportWorkbook(tradeDetailWorkbook, strategyName, "trade-detail");
        Workbook orderWorkbook = ExcelUtils.singleSheet(orders, "order-list");
        ExportUtils.exportWorkbook(orderWorkbook, strategyName, "orders-list");

        Map<String, ProfitResult> results = OrderCalculator.calculateProfitAndHoldingDays(orders);
        for (Map.Entry<String, ProfitResult> entry : results.entrySet()) {
            ProfitResult profitResult = entry.getValue();
            log.info("{}: holding days:{}, profit:{}", entry.getKey(), profitResult.getTotalHoldingDays(), profitResult.getProfit());
        }
        log.info("init balance:{}, finish balance:{}", tradeDetail.getInitialBalance(), tradeDetail.getBalance());
        log.info("fee: {}", tradeDetail.getFeeCount());
    }

    public void saveImage() {
        TradingChart tradingChart = new TradingChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        ExportUtils.saveJFreeChartAsImage(tradingChart.getCombinedChart(), strategyName, "lines", 1600, 1200);
    }

    public void process() {
        openTrade();
        HashMap<String, Kline> map = new HashMap<>();
        map.put(SymbolType.TQQQ.name(), findLatestKline(tqqqKlineMap));
        map.put(SymbolType.QQQ.name(), findLatestKline(qqqKlineMap));
        map.put(SymbolType.SQQQ.name(), findLatestKline(sqqqKlineMap));
        tradeDetail = tradeMocker.exit(map, findLatestTime(qqqKlineMap));
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
