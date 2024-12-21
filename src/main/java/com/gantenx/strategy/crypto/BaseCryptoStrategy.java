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

import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public abstract class BaseCryptoStrategy {
    protected final String strategyName;
    protected String start;
    protected String end;
    protected TradeEngine<CryptoSymbol> tradeEngine;
    protected TradeDetail<CryptoSymbol> tradeDetail;
    protected Map<CryptoSymbol, Map<Long, Kline>> klineMap;

    public BaseCryptoStrategy(String strategyName, List<CryptoSymbol> symbols, String start, String end) {
        this.strategyName = strategyName;
        this.start = start;
        this.end = end;
        long startTimestamp = DateUtils.getTimestamp(start);
        long endTimestamp = DateUtils.getTimestamp(end);
        Map<CryptoSymbol, Map<Long, Kline>> klineMap = genKlineMap(symbols, startTimestamp, endTimestamp);
        this.klineMap = klineMap;
        tradeEngine = new TradeEngine<>(genTimeList(startTimestamp, endTimestamp), klineMap);
    }

    public static List<Long> genTimeList(long startTimestamp, long endTimestamp) {
        List<Long> list = new ArrayList<>();
        for (long i = startTimestamp; i <= endTimestamp; i += MS_OF_ONE_DAY) {
            list.add(i);
        }
        return list;
    }



    public static Map<CryptoSymbol, Map<Long, Kline>> genKlineMap(List<CryptoSymbol> list,
                                                               long startTimestamp,
                                                               long endTimestamp) {
        HashMap<CryptoSymbol, Map<Long, Kline>> map = new HashMap<>();
        for (CryptoSymbol cryptoSymbol : list) {
            List<Kline> kline = BinanceService.getKline(cryptoSymbol, startTimestamp, endTimestamp);
            map.put(cryptoSymbol, CollectionUtils.toTimeMap(kline));
        }
        return map;
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
            ExportUtils.saveJFreeChartAsImage(tradingChart, start, end, strategyName, "lines", 2400, 1200);
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
