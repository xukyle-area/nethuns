package com.gantenx.nethuns.strategy.template;


import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.engine.calculator.AssetCalculator;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.plot.LinePlot;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import java.util.Collections;
import java.util.Map;

import static com.gantenx.nethuns.commons.constant.Series.ASSET;

@Slf4j
public abstract class SingleStrategy extends BaseStrategy {
    protected final Symbol symbol;

    public SingleStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, Collections.singletonList(symbol));
        this.symbol = symbol;
    }

    @Override
    protected void open() {
    }

    @Override
    protected JFreeChart getChart() {
        Map<Long, Double> assetMap = AssetCalculator.calculateAssetMap(klineMap,
                                                                       timestampList,
                                                                       tradeDetail.getOrders(),
                                                                       tradeDetail.getInitialBalance());
        XYPlot mainPlot = CandlePlot.create(Series.getSeries(symbol), klineMap.get(symbol));
        XYPlot subPlot = LinePlot.create(Collections.singletonMap(ASSET, assetMap));
        return Chart.get(mainPlot, subPlot, tradeDetail.getOrders());
    }
}
