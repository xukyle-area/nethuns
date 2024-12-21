package com.gantenx.strategy.crypto;

import com.gantenx.calculator.Profit;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.engine.OrderCalculator;
import com.gantenx.engine.TradeDetail;
import com.gantenx.engine.TradeEngine;
import com.gantenx.model.Kline;
import com.gantenx.service.BinanceService;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jfree.chart.JFreeChart;

import java.util.*;

@Slf4j
public abstract class BaseCryptoStrategy {

    protected final double initialBalance = 10000L;
    protected final double fee = 0.0001;
    protected final String strategyName;
    protected String start;
    protected String end;
    protected CryptoSymbol symbol;
    protected TradeEngine<CryptoSymbol> tradeEngine;
    protected TradeDetail<CryptoSymbol> tradeDetail;
    protected Map<Long, Kline> klineMap;

    public BaseCryptoStrategy(String strategyName, CryptoSymbol symbol, String start, String end) {
        this.strategyName = strategyName;
        this.start = start;
        this.end = end;
        this.symbol = symbol;
        long startTimestamp = DateUtils.getTimestamp(start);
        long endTimestamp = DateUtils.getTimestamp(end);
        tradeEngine = new TradeEngine<>(initialBalance, fee);
        List<Kline> kline = BinanceService.getKline(symbol, startTimestamp, endTimestamp);
        klineMap = CollectionUtils.toTimeMap(kline);
    }

    private void printTradeDetail() {
        Workbook workbook = ExcelUtils.singleSheet(Collections.singletonList(tradeDetail), "trade-detail");
        ExcelUtils.addDataToNewSheet(workbook, tradeDetail.getOrders(), "order-list");
        ExcelUtils.addDataToNewSheet(workbook, tradeDetail.getRecords(), "record-list");
        List<Profit<CryptoSymbol>> profitList = OrderCalculator.calculateProfitAndHoldingDays(tradeDetail.getOrders());
        ExcelUtils.addDataToNewSheet(workbook, profitList, "profit-list");
        ExportUtils.exportWorkbook(workbook, start, end, strategyName, "result");
        JFreeChart tradingChart = getTradingChart();
        if (Objects.nonNull(tradingChart)) {
            ExportUtils.saveJFreeChartAsImage(tradingChart, start, end, strategyName, "lines", 3600, 1200);
        }
    }

    protected JFreeChart getTradingChart() {
        return null;
    }

    public void process() {
        this.openTrade();
        Kline last = CollectionUtils.getLast(klineMap);
        Long latestTime = CollectionUtils.findLatestTime(klineMap);
        tradeDetail = tradeEngine.exit(Collections.singletonMap(symbol, last), latestTime);
        printTradeDetail();
    }

    protected abstract void openTrade();
}
