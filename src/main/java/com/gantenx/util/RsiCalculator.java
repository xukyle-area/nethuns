package com.gantenx.util;


import com.gantenx.model.Kline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RsiCalculator {

    /**
     * 计算 RSI 并将结果组装到 RSI 对象中
     *
     * @param klineList K线数据列表
     * @param period    RSI 的计算周期
     * @return 包含 RSI 的扩展 K 线数据列表
     */
    public static Map<Long, Double> calculateAndAttachRSI(List<Kline> klineList, int period) {
        if (klineList.size() < period + 1) {
            throw new IllegalArgumentException("数据不足以计算 RSI");
        }

        // 提取收盘价
        List<Double> closePrices = new ArrayList<>();
        for (Kline kline : klineList) {
            closePrices.add(Double.parseDouble(kline.getClose()));
        }

        double avgGain = 0.0;
        double avgLoss = 0.0;

        Map<Long, Double> map = new HashMap<>();
        // 初始化计算第一个平均涨幅和跌幅
        for (int i = 1; i <= period; i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss -= change;
            }
        }
        avgGain /= period;
        avgLoss /= period;

        // 第一个 RSI
        double rs = avgGain / avgLoss;
        double firstRsi = 100 - (100 / (1 + rs));
        Kline firstCandle = klineList.get(period);
        map.put(firstCandle.getTimestamp(), firstRsi);
        // 按滑动窗口方式计算后续 RSI
        for (int i = period + 1; i < closePrices.size(); i++) {
            double change = closePrices.get(i) - closePrices.get(i - 1);
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;

            rs = avgGain / avgLoss;
            double rsiValue = 100 - (100 / (1 + rs));
            Kline curCandle = klineList.get(i);
            map.put(curCandle.getTimestamp(), rsiValue);
        }

        return map;
    }
}

