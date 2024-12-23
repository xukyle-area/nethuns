package com.gantenx.strategy;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.ChartUtils;
import com.gantenx.constant.Period;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.strategy.template.SingleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Proportion.PROPORTION_OF_100;
import static com.gantenx.constant.Series.RSI;

@Slf4j
public class SingleRsiStrategy extends SingleStrategy {
    protected final Map<Long, Double> rsiMap;

    public SingleRsiStrategy(Symbol symbol, Period period, long start, long end) {
        super(SingleRsiStrategy.class.getSimpleName(), period, start, end, symbol);
        rsiMap = IndexTechnicalIndicators.calculateRSI(klineMap.get(symbol));
    }

    @Override
    protected void open() {
        while (tradeEngine.hasNext()) {
            long timestamp = tradeEngine.next();
            Double rsi = rsiMap.get(timestamp);
            if (Objects.isNull(rsi)) {
                continue;
            }
            if (rsi > 70) {
                tradeEngine.sell(symbol, PROPORTION_OF_100, String.format("%.2f", rsi));
            } else if (rsi < 30) {
                tradeEngine.buy(symbol, PROPORTION_OF_100, String.format("%.2f", rsi));
            }

        }
    }

    @Override
    protected JFreeChart getChart() {
        Series series = Series.getSeries(symbol);
        Pair<Series, Map<Long, Kline>> pair = Pair.create(series, klineMap.get(symbol));
        return ChartUtils.getCandleChart(tradeDetail.getOrders(), Pair.create(RSI, rsiMap), pair);
    }
}
