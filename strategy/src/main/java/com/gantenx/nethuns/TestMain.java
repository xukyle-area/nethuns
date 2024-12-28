package com.gantenx.nethuns;


import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.strategy.MultiMacdStrategy;
import com.gantenx.nethuns.strategy.SingleMacdStrategy;
import com.gantenx.nethuns.strategy.SingleRsiStrategy;
import com.gantenx.nethuns.strategy.template.BaseStrategy;
import lombok.extern.slf4j.Slf4j;

import static com.gantenx.nethuns.commons.constant.Period.CSV;
import static com.gantenx.nethuns.commons.constant.Period.FOUR_HOURS;
import static com.gantenx.nethuns.commons.constant.Symbol.BTCUSDT;

@Slf4j
public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240501";
        String endStr = "20241001";

        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);

//        runA(start, end, BTCUSDT);
        runB(start, end, BTCUSDT);
    }

    public static void runA(long start, long end, Symbol... symbols) {
        MultiMacdStrategy strategy = new MultiMacdStrategy(start, end, CSV, symbols);
        BaseStrategy.processAndExport(strategy);
    }

    public static void runB(long start, long end, Symbol symbol) {
        SingleMacdStrategy strategy = new SingleMacdStrategy(FOUR_HOURS, start, end, symbol);
        BaseStrategy.processAndExport(strategy);
    }

    public static void runC(long start, long end, Symbol symbol) {
        BaseStrategy strategy = new SingleRsiStrategy(FOUR_HOURS, start, end, symbol);
        BaseStrategy.processAndExport(strategy);
    }
}
