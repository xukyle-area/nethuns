package com.gantenx.nethuns.strategy;


import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.plot.LinePlot;
import com.gantenx.nethuns.indicator.RsiIndicator;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.strategy.template.SingleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.nethuns.commons.constant.Constants.INITIAL_BALANCE;
import static com.gantenx.nethuns.commons.constant.Series.RSI;


@Slf4j
public class SingleRsiStrategy extends SingleStrategy {
    protected final Map<Long, Double> rsiMap;

    public SingleRsiStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
        rsiMap = RsiIndicator.calculateRSI(klineMap.get(symbol));
    }

    @Override
    protected void open() {
        long preTimestamp = 0;
        while (tradeEngine.hasNext()) {
            long timestamp = tradeEngine.next();
            Double rsi = rsiMap.get(preTimestamp);
            preTimestamp = timestamp;
            if (Objects.isNull(rsi)) {
                continue;
            }
            if (rsi > 70) {
                tradeEngine.sell(symbol, INITIAL_BALANCE / 4, String.format("%.2f", rsi));
            } else if (rsi < 30) {
                tradeEngine.buyAmount(symbol, INITIAL_BALANCE / 4, String.format("%.2f", rsi));
            }
        }
    }

    @Override
    protected JFreeChart getChart() {
        XYPlot mainPlot = CandlePlot.create(Series.getSeries(symbol), klineMap.get(symbol));
        XYPlot subPlot = LinePlot.create(Collections.singletonMap(RSI, rsiMap));
        return Chart.get(mainPlot, subPlot, tradeDetail.getOrders());
    }
}
