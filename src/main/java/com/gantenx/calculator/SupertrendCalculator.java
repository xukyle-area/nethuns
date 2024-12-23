package com.gantenx.calculator;

import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SupertrendCalculator {
    // Multiplier：用于控制 Supertrend 的灵敏度，决定上轨和下轨的宽度，通常在 1.5 到 3 之间调整。
    private static final double MULTIPLIER = 2;
    // Period：用于计算 ATR 的时间窗口，通常选择 10 到 14 之间的值，决定波动性的计算周期。
    private static final int PERIOD = 14;

    // 计算 ATR
    public static Double calculateATR(Map<Long, Kline> klineData, List<Long> timestamps, int curTimestampIndex) {
        if (curTimestampIndex < PERIOD) {
            return 0.0;  // 数据点不足时返回 0.0
        }

        List<Double> trueRangeList = new ArrayList<>();

        // 计算 ATR 需要从当前索引往回 period 个时间点开始计算
        for (int i = curTimestampIndex - PERIOD + 1; i <= curTimestampIndex; i++) {
            long currentTimestamp = timestamps.get(i);
            Kline currentKline = klineData.get(currentTimestamp);
            Kline prevKline = klineData.get(timestamps.get(i - 1));

            double highLow = currentKline.getHigh() - currentKline.getLow();
            double highClose = Math.abs(currentKline.getHigh() - prevKline.getClose());
            double lowClose = Math.abs(currentKline.getLow() - prevKline.getClose());
            double trueRange = Math.max(highLow, Math.max(highClose, lowClose));
            trueRangeList.add(trueRange);
        }

        return trueRangeList.stream().mapToDouble(val -> val).average().orElse(0.0);
    }

    // 获取所有时间戳的 Supertrend 并返回 Map
    public static Map<Long, Double> getSupertrendMap(Map<Long, Kline> klineData) {
        List<Long> timestamps = CollectionUtils.getTimestamps(klineData);
        Map<Long, Double> supertrendMap = new HashMap<>();

        for (int curTimestampIndex = 0; curTimestampIndex < timestamps.size(); curTimestampIndex++) {
            long timestamp = timestamps.get(curTimestampIndex);
            Kline currentKline = klineData.get(timestamp);

            double atr = calculateATR(klineData, timestamps, curTimestampIndex);
            double upperBand = (currentKline.getHigh() + currentKline.getLow()) / 2 + MULTIPLIER * atr;
            double lowerBand = (currentKline.getHigh() + currentKline.getLow()) / 2 - MULTIPLIER * atr;

            double supertrend;

            // 对第一个时间戳的 Supertrend 进行特殊处理
            if (curTimestampIndex == 0) {
                supertrend = currentKline.getClose() > upperBand ? upperBand : lowerBand;
            } else {
                double prevSupertrend = supertrendMap.get(timestamps.get(curTimestampIndex - 1));

                // 根据当前收盘价和前一时刻的 Supertrend 判断当前 Supertrend 是上轨还是下轨
                if (currentKline.getClose() > prevSupertrend) {
                    supertrend = upperBand;
                } else {
                    supertrend = lowerBand;
                }
            }

            supertrendMap.put(timestamp, supertrend);
        }

        return supertrendMap;
    }
}
