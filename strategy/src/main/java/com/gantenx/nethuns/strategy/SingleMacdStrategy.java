package com.gantenx.nethuns.strategy;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Proportion;
import com.gantenx.nethuns.commons.constant.Series;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.engine.chart.Chart;
import com.gantenx.nethuns.engine.chart.plot.CandlePlot;
import com.gantenx.nethuns.engine.chart.plot.MacdPlot;
import com.gantenx.nethuns.indicator.MacdIndicator;
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

    public SingleMacdStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
        macdDetailMap = MacdIndicator.calculateMACDWithDetails(klineMap.get(super.symbol));
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

            // 判断是否金叉或死叉
            boolean isGoldenCross = prevMacdDetail.getMacdLine() <= prevMacdDetail.getSignalLine() &&
                    yesterdayMacdDetail.getMacdLine() > yesterdayMacdDetail.getSignalLine();
            boolean isDeathCross = prevMacdDetail.getMacdLine() >= prevMacdDetail.getSignalLine() &&
                    yesterdayMacdDetail.getMacdLine() < yesterdayMacdDetail.getSignalLine();

            if (isGoldenCross) {
                // 开多仓
                tradeEngine.buyAmount(symbol, INITIAL_BALANCE / 2, "Golden Cross");
            } else if (isDeathCross) {
                // 开空仓
                tradeEngine.sell(symbol, Proportion.PROPORTION_OF_100, "Death Cross");
            }

            lastTwo = lastOne;
            lastOne = today;
        }
    }


    private boolean isRed(Color color) {
        return color.equals(Color.RED) || color.equals(Color.PINK);
    }

    private boolean isGreen(Color color) {
        return color.equals(Color.GREEN.darker()) || color.equals(Color.GREEN.brighter());
    }

    private boolean greenTurnBrighter(Color prevColor, Color yesterdayColor) {
        return prevColor.equals(Color.GREEN.darker()) && yesterdayColor.equals(Color.GREEN.brighter());
    }

    private boolean redTurnBrighter(Color prevColor, Color yesterdayColor) {
        return prevColor.equals(Color.RED) && yesterdayColor.equals(Color.PINK);
    }

    @Override
    protected JFreeChart getChart() {
        XYPlot mainPlot = CandlePlot.create(Series.getSeries(symbol), klineMap.get(symbol));
        XYPlot subPlot = MacdPlot.create(macdDetailMap);
        return Chart.get(subPlot, mainPlot, tradeDetail.getOrders());
    }
}
