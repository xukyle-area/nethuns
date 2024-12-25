package com.gantenx.strategy;

import com.gantenx.constant.Period;
import com.gantenx.constant.Proportion;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Pair;
import com.gantenx.strategy.template.SingleStrategy;
import com.gantenx.utils.calculator.IndexTechnicalIndicators;
import com.gantenx.utils.calculator.MacdDetail;
import com.gantenx.utils.chart.MacdChartUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class SingleMacdStrategy extends SingleStrategy {
    protected final Map<Long, MacdDetail> macdDetailMap;

    public SingleMacdStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
        macdDetailMap = IndexTechnicalIndicators.calculateMACDWithDetails(klineMap.get(super.symbol));
    }

    @Override
    protected void open() {
        // 昨天
        Long lastOne = null;
        // 前天
        Long lastTwo = null;
        while (tradeEngine.hasNext()) {
            long today = tradeEngine.next();
            MacdDetail prevMacdDetail = macdDetailMap.get(lastTwo);
            MacdDetail yesterdayMacdDetail = macdDetailMap.get(lastOne);
            if (Objects.isNull(prevMacdDetail) || Objects.isNull(yesterdayMacdDetail)) {
                lastTwo = lastOne;
                lastOne = today;
                continue;
            }
            Double histogram = yesterdayMacdDetail.getHistogram();

            // 获取直方图值
            Color prevColor = prevMacdDetail.getHistogramColor();
            Color yesterdayColor = yesterdayMacdDetail.getHistogramColor();
            if (isGreen(prevColor) && isRed(yesterdayColor)) {
                // 绿转红，买入
                tradeEngine.buy(symbol, Proportion.PROPORTION_OF_100, "" + histogram);
            } else if (isRed(prevColor) && isGreen(yesterdayColor)) {
                // 红转绿，卖出
                tradeEngine.sell(symbol, Proportion.PROPORTION_OF_100, "" + histogram);
            }
            lastTwo = lastOne;
            lastOne = today;
        }
    }

    private boolean isRed(Color color) {
        if (color.equals(Color.RED) || color.equals(Color.PINK)) {
            return true;
        }
        return false;
    }

    private boolean isGreen(Color color) {
        if (color.equals(Color.GREEN.darker()) || color.equals(Color.GREEN.brighter())) {
            return true;
        }
        return false;
    }

    @Override
    protected JFreeChart getChart() {
        return MacdChartUtils.getSubMacdChart(tradeDetail.getOrders(),
                                              macdDetailMap,
                                              Pair.create(Series.getSeries(symbol), klineMap.get(symbol)));
    }
}
