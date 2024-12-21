package com.gantenx;

import com.gantenx.constant.CryptoSymbol;
import com.gantenx.model.Kline;
import com.gantenx.service.BinanceQuoteService;
import com.gantenx.strategy.qqq.LongHoldingStrategy;
import com.gantenx.strategy.qqq.RsiStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;
import static com.gantenx.constant.CryptoSymbol.BTC_USDT;

@Slf4j
public class Main {

    private final static BinanceQuoteService binanceQuoteService = new BinanceQuoteService();
    public static void main(String[] args) {
//        start("20240101", "20250101");
//        start("20230101", "20240101");
//        start("20220101", "20230101");
//        start("20210101", "20220101");
//        start("20200101", "20210101");
//        start("20190101", "20200101");
//        start("20180101", "20190101");
//        start("20170101", "20180101");
        testBinanceApi();
    }

    public static void start(String startStr, String endStr) {
        RsiStrategy rsiStrategy = new RsiStrategy(startStr, endStr);
        rsiStrategy.process();
//        ImprovedRsiStrategy improvedRsiStrategy = new ImprovedRsiStrategy(startStr, endStr);
//        improvedRsiStrategy.process();
        LongHoldingStrategy longHoldingStrategy = new LongHoldingStrategy(startStr, endStr);
        longHoldingStrategy.process();
    }

    public static void testBinanceApi() {
        long timestamp1 = DateUtils.getTimestamp("20230101");
        long timestamp2 = DateUtils.getTimestamp("20240101");
        List<Kline> kline = binanceQuoteService.getKline(BTC_USDT, ONE_DAY, timestamp1, timestamp2, 1500);
        for (Kline kline1 : kline) {
            System.out.println(kline1.toString());
        }
    }
}
