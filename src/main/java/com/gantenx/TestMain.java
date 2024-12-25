package com.gantenx;

import com.gantenx.constant.Symbol;
import com.gantenx.strategy.MultiMacdStrategy;
import com.gantenx.strategy.SingleMacdStrategy;
import com.gantenx.strategy.template.BaseStrategy;
import com.gantenx.utils.DateUtils;

import static com.gantenx.constant.Period.FOUR_HOURS;
import static com.gantenx.constant.Symbol.BTCUSDT;

public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240101";
        String endStr = "20240501";

        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);

        runA(start, end, BTCUSDT);
        runB(start, end, BTCUSDT);
    }

    public static void runA(long start, long end, Symbol... symbols) {
        MultiMacdStrategy strategy = new MultiMacdStrategy(start, end, FOUR_HOURS, symbols);
        BaseStrategy.processAndExport(strategy);
    }

    public static void runB(long start, long end, Symbol symbol) {
        SingleMacdStrategy strategy = new SingleMacdStrategy(FOUR_HOURS, start, end, symbol);
        BaseStrategy.processAndExport(strategy);
    }
}
