package com.gantenx.strategy;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.calculator.MacdDetail;
import com.gantenx.chart.MacdChartUtils;
import com.gantenx.constant.Period;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.strategy.template.SingleStrategy;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.Map;

@Slf4j
public class SingleMacdStrategy extends SingleStrategy {
    protected final Map<Long, MacdDetail> indexMap;

    public SingleMacdStrategy(Period period, long start, long end, Symbol symbol) {
        super(SingleMacdStrategy.class.getSimpleName(), period, start, end, symbol);
        indexMap = IndexTechnicalIndicators.calculateMACDWithDetails(klineMap.get(super.symbol), 12, 26, 9);
    }

    @Override
    protected void open() {
        while (tradeEngine.hasNext()) {
            tradeEngine.next();
        }
    }

    @Override
    protected JFreeChart getChart() {
        Series series = Series.getSeries(symbol);
        Map<Series, Map<Long, Double>> map = CollectionUtils.toSeriesPriceMap(klineMap, klineMap.keySet());
        return MacdChartUtils.getSubMacdChart(tradeDetail.getOrders(), indexMap, map);
    }
}
