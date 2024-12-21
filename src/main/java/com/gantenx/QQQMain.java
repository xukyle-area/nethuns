package com.gantenx;

import com.gantenx.strategy.BaseStrategy;
import com.gantenx.strategy.qqq.RsiStrategy;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

import static com.gantenx.constant.Constants.QQQ_SYMBOL_LIST;
import static com.gantenx.constant.Symbol.BTCUSDT;

@Slf4j
public class QQQMain {

    public static void main(String[] args) {
        start("20240101", "20250101");
        start("20230101", "20240101");
//        start("20220101", "20230101");
//        start("20210101", "20220101");
//        start("20200101", "20210101");
//        start("20190101", "20200101");
//        start("20180101", "20190101");
//        start("20170101", "20180101");
    }

    public static <T> void start(String startStr, String endStr) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        RsiStrategy rsiStrategy = new RsiStrategy(QQQ_SYMBOL_LIST, start, end);
        BaseStrategy.processAndExport(rsiStrategy);
    }
}