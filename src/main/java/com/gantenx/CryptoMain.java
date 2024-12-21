package com.gantenx;

import com.gantenx.constant.CryptoSymbol;
import com.gantenx.engine.TradeEngine;
import com.gantenx.model.Kline;
import com.gantenx.service.BinanceService;
import com.gantenx.strategy.crypto.RsiCryptoStrategy;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.CryptoSymbol.BTC_USDT;

@Slf4j
public class CryptoMain {

    public static void main(String[] args) {
        // start("20240101", "20241101");

        long startTimestamp = DateUtils.getTimestamp("20240101");
        long endTimestamp = DateUtils.getTimestamp("20241101");

        List<Kline> kline = BinanceService.getKline(BTC_USDT, startTimestamp, endTimestamp);
        Map<Long, Kline> klineMap = CollectionUtils.toTimeMap(kline);

        System.exit(1);
    }

    public static void start(String startStr, String endStr) {
        RsiCryptoStrategy rsiStrategy = new RsiCryptoStrategy(BTC_USDT, startStr, endStr);
        rsiStrategy.process();
    }
}
