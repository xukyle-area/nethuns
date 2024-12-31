package com.gantenx.nethuns;


import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.strategy.SingleMacdStrategy;
import com.gantenx.nethuns.strategy.template.BaseStrategy;
import lombok.extern.slf4j.Slf4j;

import static com.gantenx.nethuns.commons.constant.Period.M_15;
import static com.gantenx.nethuns.commons.constant.Period.M_30;
import static com.gantenx.nethuns.commons.constant.Symbol.ETHUSDT;

@Slf4j
public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240901";
        String endStr = "20241001";

        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);

//        runC(start, end, ETHUSDT);
        runB(start, end, ETHUSDT);
    }
    public static void runB(long start, long end, Symbol symbol) {
        SingleMacdStrategy strategy = new SingleMacdStrategy(M_30, start, end, symbol);
        BaseStrategy.processAndExport(strategy);
    }
}
