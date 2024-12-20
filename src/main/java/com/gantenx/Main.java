package com.gantenx;

import com.gantenx.strategy.qqq.LongHoldingStrategy;
import com.gantenx.strategy.qqq.RsiStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        start("20230101", "20240101");
        start("20240101", "20250101");
        start("20220101", "20230101");
    }

    public static void start(String startStr, String endStr) {
        RsiStrategy rsiStrategy = new RsiStrategy(startStr, endStr);
        rsiStrategy.process();
        LongHoldingStrategy longHoldingStrategy = new LongHoldingStrategy(startStr, endStr);
        longHoldingStrategy.process();
    }
}
