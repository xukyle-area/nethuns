package com.gantenx;

import com.gantenx.strategy.crypto.RsiCryptoStrategy;
import lombok.extern.slf4j.Slf4j;

import static com.gantenx.constant.CryptoSymbol.BTC_USDT;

@Slf4j
public class CryptoMain {

    public static void main(String[] args) {
        start("20230101", "20240101");
        System.exit(1);
    }

    public static void start(String startStr, String endStr) {
        RsiCryptoStrategy rsiStrategy = new RsiCryptoStrategy(BTC_USDT, startStr, endStr);
        rsiStrategy.process();
    }
}
