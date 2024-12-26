package com.gantenx.nethuns.strategy;


import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Proportion;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.model.MacdDetail;
import com.gantenx.nethuns.indicator.MacdCalculator;
import com.gantenx.nethuns.strategy.template.MultiStrategy;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.nethuns.commons.constant.Constants.INITIAL_BALANCE;

@Slf4j
public class MultiMacdStrategy extends MultiStrategy {
    protected final Map<Symbol, Map<Long, MacdDetail>> macdDetailMap = new HashMap<>();

    public MultiMacdStrategy(long start, long end, Period period, Symbol... symbolList) {
        super(period, start, end, Arrays.asList(symbolList));
        for (Symbol symbol : klineMap.keySet()) {
            Map<Long, Kline> map = klineMap.get(symbol);
            macdDetailMap.put(symbol, MacdCalculator.calculateMACDWithDetails(map));
        }
    }

    @Override
    public void open() {
        // 昨天
        Long lastOne = null;
        // 前天
        Long lastTwo = null;
        while (tradeEngine.hasNext()) {
            long today = tradeEngine.next();
            for (Symbol symbol : klineMap.keySet()) {
                Map<Long, MacdDetail> detailMap = macdDetailMap.get(symbol);
                MacdDetail prevMacdDetail = detailMap.get(lastTwo);
                MacdDetail yesterdayMacdDetail = detailMap.get(lastOne);
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
                    tradeEngine.buyAmount(symbol, INITIAL_BALANCE / klineMap.keySet().size(), "" + histogram);
                } else if (isRed(prevColor) && isGreen(yesterdayColor)) {
                    // 红转绿，卖出
                    tradeEngine.sell(symbol, Proportion.PROPORTION_OF_100, "" + histogram);
                }
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
}