package com.gantenx.nethuns.strategy;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.engine.chart.plot.MacdPlot;
import com.gantenx.nethuns.indicator.MacdIndicator;
import com.gantenx.nethuns.indicator.RsiIndicator;
import com.gantenx.nethuns.indicator.model.MacdDetail;
import com.gantenx.nethuns.strategy.template.SingleStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.nethuns.commons.constant.Constants.INITIAL_BALANCE;

@Slf4j
public class SingleMacdStrategy extends SingleStrategy {
    protected final Map<Long, MacdDetail> macdDetailMap;
    protected final Map<Long, Double> rsiMap;

    public SingleMacdStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
        Map<Long, Kline> map = klineMap.get(super.symbol);
        macdDetailMap = MacdIndicator.calculateMACDWithDetails(map);
        rsiMap = RsiIndicator.calculateRSI(map);
    }

    @Override
    protected void open() {
        Long lastOne = null;
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

            Color prevColor = prevMacdDetail.getHistogramColor();
            Color yesterdayColor = yesterdayMacdDetail.getHistogramColor();

            if (this.isGreen(prevColor) && this.isRed(yesterdayColor)) {
                tradeEngine.buyAmount(symbol, INITIAL_BALANCE / 3, "macd 最低");
            } else if (this.isSell(prevColor, yesterdayColor)) {
                tradeEngine.sell(symbol, INITIAL_BALANCE / 3, "macd 最高");
            }

            lastTwo = lastOne;
            lastOne = today;
        }
    }


    public boolean isSell(Color prevColor, Color yesterdayColor) {
        if (prevColor.equals(Color.RED) && yesterdayColor.equals(Color.PINK)) {
            return true;
        }
        return false;
    }

    public boolean isRed(Color color) {
        if (color.equals(Color.RED) || color.equals(Color.PINK)) {
            return true;
        }
        return false;
    }

    public boolean isGreen(Color color) {
        if (color.equals(Color.GREEN.darker()) || color.equals(Color.GREEN.brighter())) {
            return true;
        }
        return false;
    }

    @Override
    protected JFreeChart getChart() {
        XYPlot mainPlot = CandlePlot.create(Series.getSeries(symbol), klineMap.get(symbol));
        XYPlot subPlot = MacdPlot.create(macdDetailMap);
        return Chart.get(mainPlot, subPlot, tradeDetail.getOrders());
    }
}
