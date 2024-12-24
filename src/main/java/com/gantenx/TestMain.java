package com.gantenx;

import com.gantenx.retrofit.RetrofitClient;
import com.gantenx.strategy.SingleMacdStrategy;
import com.gantenx.strategy.template.BaseStrategy;
import com.gantenx.utils.DateUtils;

import static com.gantenx.constant.Period.ONE_DAY;
import static com.gantenx.constant.Symbol.BTCUSDT;

public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240101";
        String endStr = "20241101";

        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);

        SingleMacdStrategy strategy = new SingleMacdStrategy(ONE_DAY, start, end, BTCUSDT);
        BaseStrategy.processAndExport(strategy);
        RetrofitClient.shutdown();
    }
}
