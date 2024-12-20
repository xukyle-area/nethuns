package com.gantenx;

import com.gantenx.strategy.qqq.AlphaStrategy;
import com.gantenx.strategy.qqq.WeightedStrategy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        String startStr = "20231106";
        String endStr = "20241210";
        long initialBalance = 100000L;
        double fee = 0.00001;
        WeightedStrategy strategy = new WeightedStrategy(initialBalance, fee, startStr, endStr);
        strategy.process();
        strategy.printTradeDetail();
        strategy.saveImage();
    }
}
