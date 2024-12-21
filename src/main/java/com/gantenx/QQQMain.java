package com.gantenx;

import com.gantenx.model.Kline;
import com.gantenx.service.BinanceQuoteService;
import com.gantenx.strategy.qqq.ImprovedRsiStrategy;
import com.gantenx.strategy.qqq.LongHoldingStrategy;
import com.gantenx.strategy.qqq.RsiStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;
import static com.gantenx.constant.CryptoSymbol.BTC_USDT;

@Slf4j
public class QQQMain {

    public static void main(String[] args) {
        start("20240101", "20250101");
        start("20230101", "20240101");
        start("20220101", "20230101");
        start("20210101", "20220101");
        start("20200101", "20210101");
        start("20190101", "20200101");
        start("20180101", "20190101");
        start("20170101", "20180101");
    }

    public static void start(String startStr, String endStr) {
        RsiStrategy rsiStrategy = new RsiStrategy(startStr, endStr);
        rsiStrategy.process();
        ImprovedRsiStrategy improvedRsiStrategy = new ImprovedRsiStrategy(startStr, endStr);
        improvedRsiStrategy.process();
        LongHoldingStrategy longHoldingStrategy = new LongHoldingStrategy(startStr, endStr);
        longHoldingStrategy.process();
    }
}
