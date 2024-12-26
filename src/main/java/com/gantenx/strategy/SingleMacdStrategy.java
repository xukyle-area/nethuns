package com.gantenx.strategy;

import com.gantenx.constant.Period;
import com.gantenx.constant.Proportion;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Pair;
import com.gantenx.strategy.template.SingleStrategy;
import com.gantenx.utils.indicator.MacdCalculator;
import com.gantenx.model.MacdDetail;
import com.gantenx.utils.chart.MacdChartUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.awt.*;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.INITIAL_BALANCE;

@Slf4j
public class SingleMacdStrategy extends SingleStrategy {
    protected final Map<Long, MacdDetail> macdDetailMap;

    public SingleMacdStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
        macdDetailMap = MacdCalculator.calculateMACDWithDetails(klineMap.get(super.symbol));
    }

    @Override
    protected void open() {
        Long lastOne = null;
        Long lastTwo = null;

        while (tradeEngine.hasNext()) {
            long today = tradeEngine.next();
            MacdDetail prevMacdDetail = macdDetailMap.get(lastTwo);
            MacdDetail yesterdayMacdDetail = macdDetailMap.get(lastOne);
            MacdDetail todayMacdDetail = macdDetailMap.get(today);

            if (Objects.isNull(prevMacdDetail) || Objects.isNull(yesterdayMacdDetail) || Objects.isNull(todayMacdDetail)) {
                lastTwo = lastOne;
                lastOne = today;
                continue;
            }

            // 判断是否金叉或死叉
            boolean isGoldenCross = yesterdayMacdDetail.getMacdLine() <= yesterdayMacdDetail.getSignalLine() &&
                    todayMacdDetail.getMacdLine() > todayMacdDetail.getSignalLine();
            boolean isDeathCross = yesterdayMacdDetail.getMacdLine() >= yesterdayMacdDetail.getSignalLine() &&
                    todayMacdDetail.getMacdLine() < todayMacdDetail.getSignalLine();

            // 获取短期和长期均线
            double shortTermMA = calculateMovingAverage(today, 10); // 短期均线
            double longTermMA = calculateMovingAverage(today, 50); // 长期均线

            // 趋势确认
            boolean isUptrend = shortTermMA > longTermMA;
            boolean isDowntrend = shortTermMA < longTermMA;

            if (isGoldenCross && isUptrend) {
                // 开多仓
                tradeEngine.buyAmount(symbol, INITIAL_BALANCE / 2, "Golden Cross");
            } else if (isDeathCross && isDowntrend) {
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
        return MacdChartUtils.getSubMacdChart(tradeDetail.getOrders(),
                                              macdDetailMap,
                                              Pair.create(Series.getSeries(symbol), klineMap.get(symbol)));
    }
}
