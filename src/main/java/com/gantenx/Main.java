package com.gantenx;

import com.gantenx.strategy.qqq.LongHoldingStrategy;
import com.gantenx.strategy.qqq.RsiStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        String startStr = "20231106";
        String endStr = "20241210";
        long initialBalance = 10000L;
        double fee = 0.00001;
        RsiStrategy rsiStrategy = new RsiStrategy(initialBalance, fee, startStr, endStr);
        rsiStrategy.process();
        rsiStrategy.printTradeDetail();
        rsiStrategy.saveImage();
        LongHoldingStrategy longHoldingStrategy = new LongHoldingStrategy(initialBalance, fee, startStr, endStr);
        longHoldingStrategy.process();
        longHoldingStrategy.printTradeDetail();
        longHoldingStrategy.saveImage();
    }
}
