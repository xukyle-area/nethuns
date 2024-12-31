package com.gantenx.nethuns.strategies;

import com.gantenx.nethuns.indicator.RsiIndicator;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.executor.Executor;
import com.gantenx.nethuns.rule.CrossedDownIndicatorRule;
import com.gantenx.nethuns.rule.CrossedUpIndicatorRule;
import com.gantenx.nethuns.service.KlineService;

import java.util.Map;

public class RsiStrategy {

    public static void main(String[] args) {
        Symbol symbol = Symbol.BTCUSDT;
        String startStr = "20240101";
        String endStr = "20241001";
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Map<Long, Kline> klineMap = KlineService.getKLineMap(symbol, Period.D_1, start, end);
        RsiIndicator rsiIndicator = new RsiIndicator(klineMap);
        ExportUtils.saveJFreeChartAsImage(rsiIndicator.getChart(), "RSI");

        // 跌破 30
        CrossedDownIndicatorRule buyRule = new CrossedDownIndicatorRule(rsiIndicator, 30.0d);
        // 涨破 70
        CrossedUpIndicatorRule sellRule = new CrossedUpIndicatorRule(rsiIndicator, 30.0d);
        Executor executor = new Executor(klineMap, symbol, buyRule, sellRule);
        Executor.processAndExport(executor);
    }
}
