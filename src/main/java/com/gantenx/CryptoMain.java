package com.gantenx;

import com.gantenx.strategy.crypto.RsiCryptoStrategy;
import com.gantenx.strategy.crypto.WyckoffStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import static com.gantenx.constant.CryptoSymbol.*;

@Slf4j
public class CryptoMain {

    public static void main(String[] args) {
        start("20230101", "20241221");
        System.exit(1);
    }

    public static void start(String startStr, String endStr) {
        RsiCryptoStrategy rsiStrategy = new RsiCryptoStrategy(Collections.singletonList(BTC_USDT), startStr, endStr);
        rsiStrategy.process();
//        WyckoffStrategy wyckoffStrategy = new WyckoffStrategy(BTC_USDT, startStr, endStr);
//        wyckoffStrategy.process();
    }
}
