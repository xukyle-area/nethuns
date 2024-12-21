package com.gantenx.chart.qqq;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.constant.Constants;
import com.gantenx.constant.QQQSymbol;
import com.gantenx.model.Index;
import com.gantenx.model.Kline;
import com.gantenx.engine.Order;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.QQQSymbol.QQQ;

public class WeightScoreChart extends BaseQQQChart<QQQSymbol> {

    private static final String WEIGHT_SCORE = "WeightScore";

    public WeightScoreChart(Map<QQQSymbol, Map<Long, Kline>> klineMap,
                            List<Order<QQQSymbol>> orderList) {
        super(klineMap, WeightScoreChart.subDataset(klineMap.get(QQQ)), WEIGHT_SCORE, 1.0, orderList);
    }

    private static XYSeriesCollection subDataset(Map<Long, Kline> klineMap) {
        Map<Long, Index> indexMap = IndexCalculator.getIndexMap(klineMap, Constants.INDEX_WEIGHTS, Constants.INDEX_PERIOD);
        XYSeries series = new XYSeries(WEIGHT_SCORE);
        for (Map.Entry<Long, Index> entry : indexMap.entrySet()) {
            series.add((double) entry.getKey(), entry.getValue().getWeightedScore());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        return dataset;
    }
}
