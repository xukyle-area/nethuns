package com.gantenx;

import com.gantenx.strategy.BaseStrategy;
import com.gantenx.strategy.crypto.RsiStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import static com.gantenx.constant.Constants.CRYPTO_SYMBOL_LIST;

@Slf4j
public class CryptoMain {

    public static void main(String[] args) {
        start("20230101", "20241221");
        System.exit(1);
    }

    public static void start(String startStr, String endStr) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        RsiStrategy rsiStrategy = new RsiStrategy(CRYPTO_SYMBOL_LIST, start, end);
        BaseStrategy.processAndExport(rsiStrategy);
    }
}
