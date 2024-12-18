package com.gantenx;

import com.gantenx.strategy.qqq.AlphaStrategy;
import com.gantenx.strategy.qqq.LongHoldingQQQStrategy;
import com.gantenx.util.StrategyUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        String startStr = "20231106";
        String endStr = "20241210";
        long initialBalance = 100000L;
        double fee = 0.00001;

        AlphaStrategy alpha = new AlphaStrategy(initialBalance, fee);
        StrategyUtils.replay(alpha, startStr, endStr);

        LongHoldingQQQStrategy longHolding = new LongHoldingQQQStrategy(initialBalance, fee);
        StrategyUtils.replay(longHolding, startStr, endStr);
    }
}
