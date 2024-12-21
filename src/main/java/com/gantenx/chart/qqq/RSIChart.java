package com.gantenx.chart.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Symbol.QQQUSD;

public class RSIChart extends BaseQQQChart {
    private static final String RSI = "RSI";

    public RSIChart(Map<Symbol, Map<Long, Kline>> klineMap, List<Order> orderList) {
        super(klineMap, RSIChart.subDataset(klineMap.get(QQQUSD)), RSI, 100.0, orderList);
    }

    private static XYSeriesCollection subDataset(Map<Long, Kline> qqqMap) {
        Map<Long, Double> rsiMap = IndexTechnicalIndicators.calculateRSI(qqqMap, 6);
        XYSeries series = new XYSeries(RSI);
        for (Map.Entry<Long, Double> entry : rsiMap.entrySet()) {
            series.add(entry.getKey(), entry.getValue());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
}
