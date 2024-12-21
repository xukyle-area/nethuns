package com.gantenx.strategy.crypto;

import com.gantenx.constant.CryptoSymbol;
import com.gantenx.model.Kline;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_10;

@Slf4j
public class WyckoffStrategy extends BaseCryptoStrategy {
    private static final double SUPPORT_THRESHOLD = 0.005; // 支撑位阈值
    private static final double RESISTANCE_THRESHOLD = 0.005; // 阻力位阈值

    public WyckoffStrategy(List<CryptoSymbol> symbols, String start, String end) {
        super(WyckoffStrategy.class.getSimpleName(), symbols, start, end);
    }

    @Override
    protected void openTrade() {
        double lastSupport = 0;
        double lastResistance = 0;
        boolean inAccumulationPhase = false; // 当前是否在吸筹阶段
        boolean hasPosition = false; // 是否已经持有仓位

        while (tradeEngine.hasNextDay()) {
            long timestamp = tradeEngine.nextDay();
            for (CryptoSymbol symbol : klineMap.keySet()) {
                Kline kline = tradeEngine.getKline(symbol);
                if (Objects.isNull(kline)) {
                    log.error("Kline data not found for timestamp: {}", DateUtils.getDate(timestamp));
                    continue;
                }

                double low = kline.getLow();
                double high = kline.getHigh();
                double close = kline.getClose();

                // 判断支撑位和阻力位
                if (lastSupport == 0 || low < lastSupport * (1 - SUPPORT_THRESHOLD)) {
                    lastSupport = low; // 重新设置支撑位
                }
                if (lastResistance == 0 || high > lastResistance * (1 + RESISTANCE_THRESHOLD)) {
                    lastResistance = high; // 重新设置阻力位
                }

                // 维可夫吸筹阶段：价格在支撑位附近震荡，形成吸筹区间
                if (close > lastSupport * (1 + SUPPORT_THRESHOLD) && close < lastResistance * (1 - RESISTANCE_THRESHOLD)) {
                    inAccumulationPhase = true;
                }

                // 买入策略：当价格接近支撑位并且处于吸筹阶段
                if (inAccumulationPhase && !hasPosition && close < lastSupport * (1 + SUPPORT_THRESHOLD)) {
                    log.info("Buying at price {} at timestamp {}", close, DateUtils.getDate(timestamp));
                    tradeEngine.buy(symbol, PROPORTION_OF_10, "Wyckoff Accumulation Buy");
                    hasPosition = true;
                }

                // 卖出策略：当价格接近阻力位并且已经持仓
                if (inAccumulationPhase && hasPosition && close > lastResistance * (1 - RESISTANCE_THRESHOLD)) {
                    log.info("Selling at price {} at timestamp {}", close, DateUtils.getDate(timestamp));
                    tradeEngine.sell(symbol, PROPORTION_OF_10, "Wyckoff Accumulation Sell");
                    hasPosition = false;
                }
            }
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        return null;
    }
}
