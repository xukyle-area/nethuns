package com.gantenx.strategy.template;

import com.gantenx.chart.ChartUtils;
import com.gantenx.constant.Period;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Pair;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Series.QQQ;
import static com.gantenx.constant.Symbol.QQQUSD;

@Slf4j
public abstract class MultiStrategy extends BaseStrategy {

    public MultiStrategy(String name, Period period, long start, long end, List<Symbol> symbolList) {
        super(name, period, DateUtils.genTimeList(period, start, end), symbolList);
    }

    @Override
    public void open() {
    }


    @Override
    protected JFreeChart getChart() {
        Map<Series, Map<Long, Double>> map = CollectionUtils.toSeriesPriceMap(klineMap, klineMap.keySet());
        Pair<Series, Map<Long, Double>> pair = Pair.create(QQQ, CollectionUtils.toPriceMap(klineMap.get(QQQUSD)));
        return ChartUtils.getLineChart(tradeDetail.getOrders(), pair, map);
    }
}