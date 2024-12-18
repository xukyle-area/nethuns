package com.gantenx;

import com.gantenx.model.Kline;
import com.gantenx.model.KlineWithRSI;
import com.gantenx.model.Order;
import com.gantenx.model.TradeDetail;
import com.gantenx.service.BinanceQuoteService;
import com.gantenx.strategy.RsiStrategy;
import com.gantenx.util.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gantenx.constant.Constants.ONE_DAY;

@Slf4j
public class Main {
    private static final RsiStrategy rsiStrategy = new RsiStrategy();
    private static final BinanceQuoteService QUOTE_SERVICE = new BinanceQuoteService();

    @Deprecated
    private static void testForCrypto(String symbol) {
        long startTime = DateUtils.getTimestamp("20200103");
        long endTime = DateUtils.getTimestamp("20241203");
        List<Kline> kline = QUOTE_SERVICE.getKline(symbol.toUpperCase(), ONE_DAY, startTime, endTime, 1500);
        List<KlineWithRSI> klineWithRsiList = RsiCalculator.calculateAndAttachRSI(kline, 6);
        TradeDetail tradeDetail = rsiStrategy.process(klineWithRsiList);
        //TradingChart.show(klineWithRsiList, tradeDetail.getOrders());
    }

    public static void main(String[] args) {
        // testForCrypto("BTCUSDT");
        testForQQQ();
    }

    private static void testForQQQ() {
        String start = "20230101"; //384
        String end = "20241203"; //264
        List<Kline> qqqKlineList = CsvUtils.getKLineFromCsv("data/QQQ.csv", start, end);
        List<Kline> tqqqKlineList = CsvUtils.getKLineFromCsv("data/TQQQ.csv", start, end);
        List<KlineWithRSI> klineWithRsiList = RsiCalculator.calculateAndAttachRSI(qqqKlineList, 6);
        Map<Long, Kline> klineMap = toMap(tqqqKlineList);
        Map<Long, KlineWithRSI> rsiMap = toMap(klineWithRsiList);
        TradeDetail td = rsiStrategy.processA(start, end, klineMap, rsiMap);
        List<Order> orders = td.getOrders();
        TradingChart.show(klineWithRsiList, tqqqKlineList, orders);
        for (Order order : orders) {
            long timestamp = order.getTimestamp();
            String date = DateUtils.getDate(timestamp);
            KlineWithRSI klineWithRSI = rsiMap.get(timestamp);
            double price = order.getPrice();
            double quantity = order.getQuantity();
            String symbol = order.getSymbol();
            String type = order.getType();
            Double rsi = klineWithRSI.getRsi();
            log.info("{}: {} {}, {} * {} = {}, QQQ_RSI: {}", date, type, symbol, price, quantity, price * quantity, rsi);
        }

        Map<String, OrderCalculator.Result> results = OrderCalculator.calculateProfitAndHoldingDays(orders);
        for (Map.Entry<String, OrderCalculator.Result> entry : results.entrySet()) {
            OrderCalculator.Result result = entry.getValue();
            log.info("{}: holding days:{}, profit:{}", entry.getKey(), result.getTotalHoldingDays(), result.getProfit());
        }
        log.info("init balance:{}, finish balance:{}", td.getInitialBalance(), td.getBalance());
        log.info("fee:{}", td.getFeeCount());
    }

    private static <T extends Kline> Map<Long, T> toMap(List<T> klineList) {
        return klineList.stream().collect(Collectors.toMap(Kline::getTime, kline -> kline));
    }
}
