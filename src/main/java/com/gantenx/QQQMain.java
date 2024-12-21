package com.gantenx;

import com.gantenx.strategy.qqq.LongHoldingQQQStrategy;
import com.gantenx.strategy.qqq.RsiQQQStrategy;
import lombok.extern.slf4j.Slf4j;

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
        RsiQQQStrategy rsiStrategy = new RsiQQQStrategy(startStr, endStr);
        rsiStrategy.process();
        LongHoldingQQQStrategy longHoldingStrategy = new LongHoldingQQQStrategy(startStr, endStr);
        longHoldingStrategy.process();
    }
}
