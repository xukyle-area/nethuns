package com.gantenx.chart.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.QQQSymbol;
import com.gantenx.model.Kline;
import com.gantenx.engine.Order;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.QQQSymbol.QQQ;

public class RSIChart extends BaseQQQChart<QQQSymbol> {
    private static final String RSI = "RSI";

    public RSIChart(Map<QQQSymbol, Map<Long, Kline>> klineMap, List<Order<QQQSymbol>> orderList) {
        super(klineMap, RSIChart.subDataset(klineMap.get(QQQ)), RSI, 100.0, orderList);
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
