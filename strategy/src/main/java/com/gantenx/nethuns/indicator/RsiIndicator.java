package com.gantenx.nethuns.indicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.plot.LinePlot;
import com.gantenx.nethuns.indicator.base.AbstractIndicator;

public class RsiIndicator extends AbstractIndicator<Double> {

    public RsiIndicator(Map<Long, Candle> klineMap) {
        super(klineMap);
        super.resultMap = this.calculate();
    }

    @Override
    protected Map<Long, Double> calculate() {
        return RsiCalculator.calculateRSI(super.getKlineMap());
    }

    @Override
    public JFreeChart getChart() {
        XYPlot main = LinePlot.create(Collections.singletonMap(Series.RSI, super.resultMap));
        return Chart.get(main, null, new ArrayList<>());
    }
}
