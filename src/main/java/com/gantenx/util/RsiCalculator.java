package com.gantenx.util;

import java.util.ArrayList;
import java.util.List;

public class RsiCalculator {

    /**
     * 计算 RSI 指标
     *
     * @param prices 收盘价列表
     * @param period RSI 计算周期
     * @return RSI 值列表
     */
    public static List<Double> calculateRSI(List<Double> prices, int period) {
        List<Double> rsiValues = new ArrayList<>();

        if (prices == null || prices.size() <= period) {
            throw new IllegalArgumentException("价格数据不足或周期过大");
        }

        // 初始化第一个周期的平均涨幅和跌幅
        double avgGain = 0.0;
        double avgLoss = 0.0;

        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss -= change;
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 第一个 RSI 值
        double rs = avgGain / avgLoss;
        rsiValues.add(100 - (100 / (1 + rs)));

        // 计算后续的 RSI
        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);

            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            // 使用 EMA 更新平均涨幅和跌幅
            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;

            // 计算 RSI
            rs = avgGain / avgLoss;
            double rsi = 100 - (100 / (1 + rs));
            rsiValues.add(rsi);
        }

        // 填补前期 RSI 为 0
        for (int i = 0; i < period; i++) {
            rsiValues.add(0, 0.0);
        }

        return rsiValues;
    }
}

