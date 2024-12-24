package com.gantenx.strategy.template;

import com.gantenx.utils.calculator.AssetCalculator;
import com.gantenx.utils.chart.CandleChartUtils;
import com.gantenx.constant.Period;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.Collections;
import java.util.Map;

import static com.gantenx.constant.Series.ASSET;

@Slf4j
public abstract class SingleStrategy extends BaseStrategy {
    protected final Symbol symbol;

    public SingleStrategy(String name, Period period, long start, long end, Symbol symbol) {
        super(name, period, DateUtils.genTimeList(period, start, end), Collections.singletonList(symbol));
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
        Series series = Series.getSeries(symbol);
        Pair<Series, Map<Long, Kline>> pair = Pair.create(series, klineMap.get(symbol));
        return CandleChartUtils.getCandleChart(tradeDetail.getOrders(), Pair.create(ASSET, assetMap), pair);
    }
}
