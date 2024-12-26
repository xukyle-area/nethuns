package com.gantenx.utils.indicator;

import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.gantenx.constant.Constants.RSI_PERIOD;

public class RsiCalculator {

    public static Map<Long, Double> calculateRSI(Map<Long, Kline> klineMap) {
        Map<Long, Double> rsiMap = new TreeMap<>();
        List<Long> timestamps = CollectionUtils.getTimestamps(klineMap);

        if (timestamps.size() < RSI_PERIOD + 1) {
            return rsiMap; // 返回空Map如果数据不足
        }

        // 第一步：计算价格变化
        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < timestamps.size(); i++) {
            double currentClose = klineMap.get(timestamps.get(i)).getClose();
            double previousClose = klineMap.get(timestamps.get(i - 1)).getClose();
            changes.add(currentClose - previousClose);
        }

        // 第二步：计算初始平均涨跌
        double avgGain = 0;
        double avgLoss = 0;

        // 计算第一个RSI值的平均涨跌
        for (int i = 0; i < RSI_PERIOD; i++) {
            double change = changes.get(i);
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += -change;
            }
        }

        avgGain = avgGain / RSI_PERIOD;
        avgLoss = avgLoss / RSI_PERIOD;

        // 第三步：使用Wilder's smoothing计算后续的RSI值
        // 添加第一个RSI值
        double rs = avgGain / (avgLoss == 0 ? 1 : avgLoss);
        double rsi = 100 - (100 / (1 + rs));
        rsiMap.put(timestamps.get(RSI_PERIOD), rsi);

        // 计算剩余的RSI值
        for (int i = RSI_PERIOD; i < changes.size(); i++) {
            double change = changes.get(i);
            double gain = Math.max(0, change);
            double loss = Math.max(0, -change);

            // Wilder's smoothing
            avgGain = ((avgGain * (RSI_PERIOD - 1)) + gain) / RSI_PERIOD;
            avgLoss = ((avgLoss * (RSI_PERIOD - 1)) + loss) / RSI_PERIOD;

            // 避免除以零
            rs = avgGain / (avgLoss == 0 ? 1e-10 : avgLoss);
            rsi = 100 - (100 / (1 + rs));

            // 确保RSI在0-100范围内
            rsi = Math.min(100, Math.max(0, rsi));

            rsiMap.put(timestamps.get(i + 1), rsi);
        }

        return rsiMap;
    }
}
