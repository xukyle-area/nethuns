package com.gantenx;

import com.gantenx.strategy.BaseStrategy;
import com.gantenx.strategy.crypto.TrendStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import static com.gantenx.constant.Constants.CRYPTO_SYMBOL_LIST;
import static com.gantenx.constant.Period.FOUR_HOURS;

@Slf4j
public class CryptoMain {

    public static void main(String[] args) {
        start("20240414", "20240519");
        System.exit(1);
    }

    public static void start(String startStr, String endStr) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        TrendStrategy trendStrategy = new TrendStrategy(CRYPTO_SYMBOL_LIST, FOUR_HOURS, start, end);
        BaseStrategy.processAndExport(trendStrategy);
    }
}
