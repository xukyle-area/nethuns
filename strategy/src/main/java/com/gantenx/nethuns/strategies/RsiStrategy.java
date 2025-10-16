package com.gantenx.nethuns.strategies;

import java.util.Map;
import com.gantenx.nethuns.CandleService;
import com.gantenx.nethuns.commons.enums.Period;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.chart.ExportUtils;
import com.gantenx.nethuns.executor.TradeExecutor;
import com.gantenx.nethuns.indicator.RsiIndicator;
import com.gantenx.nethuns.rule.CrossedDownIndicatorRule;
import com.gantenx.nethuns.rule.CrossedUpIndicatorRule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RsiStrategy {

    public static void main(String[] args) {
        Symbol symbol = Symbol.BTCUSDT;
        String startStr = "20240101";
        String endStr = "20241001";
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Map<Long, Candle> klineMap = CandleService.getKLineMap(symbol, Period.D_1, start, end);
        RsiIndicator rsiIndicator = new RsiIndicator(klineMap);
        ExportUtils.saveJFreeChartAsImage(rsiIndicator.getChart(), "RSI");
        //
        // 跌破 30
        CrossedDownIndicatorRule buyRule = new CrossedDownIndicatorRule(rsiIndicator, 30.0d);
        // 涨破 70
        CrossedUpIndicatorRule sellRule = new CrossedUpIndicatorRule(rsiIndicator, 70.0d);
        TradeExecutor tradeExecutor = new TradeExecutor(klineMap, symbol, buyRule, sellRule);
        TradeExecutor.processAndExport(tradeExecutor);
    }
}
