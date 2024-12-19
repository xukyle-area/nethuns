package com.gantenx.calculator;

import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;

import java.util.*;

public class IndexTechnicalIndicators {

    // 计算SMA（简单移动平均）
    public static Map<Long, Double> calculateSMA(Map<Long, Kline> klineMap, int period) {
        Map<Long, Double> smaMap = new TreeMap<>();
        List<Long> timestamps = CollectionUtils.getTimestamps(klineMap);
        Queue<Double> window = new LinkedList<>();
        double sum = 0.0;

        for (Long timestamp : timestamps) {
            double close = klineMap.get(timestamp).getClose();
            window.add(close);
            sum += close;

            if (window.size() > period) {
                sum -= window.poll();
            }

            if (window.size() == period) {
                smaMap.put(timestamp, sum / period);
            }
        }
        return smaMap;
    }

    // 计算布林带
    public static Map<Long, double[]> calculateBollingerBands(Map<Long, Double> smaMap,
                                                              Map<Long, Kline> klineMap,
                                                              int period) {
        Map<Long, double[]> bollingerMap = new TreeMap<>();
        List<Long> timestamps = CollectionUtils.getTimestamps(klineMap);
        Queue<Double> window = new LinkedList<>();

        for (Long timestamp : timestamps) {
            double close = klineMap.get(timestamp).getClose();
            window.add(close);

            if (window.size() > period) {
                window.poll();
            }

            if (window.size() == period) {
                double sma = smaMap.get(timestamp);
                double variance = 0.0;
                for (double price : window) {
                    variance += Math.pow(price - sma, 2);
                }
                double stdDev = Math.sqrt(variance / period);
                bollingerMap.put(timestamp, new double[]{
                        sma - 2 * stdDev, // Lower Band
                        sma,              // Middle Band
                        sma + 2 * stdDev  // Upper Band
                });
            }
        }
        return bollingerMap;
    }

    // 计算RSI（相对强弱指数）
    public static Map<Long, Double> calculateRSI(Map<Long, Kline> klineMap, int period) {
        Map<Long, Double> rsiMap = new TreeMap<>();
        List<Long> timestamps = CollectionUtils.getTimestamps(klineMap);

        double gain = 0.0;
        double loss = 0.0;
        boolean initialized = false;

        for (int i = 1; i < timestamps.size(); i++) {
            long currentTimestamp = timestamps.get(i);
            long previousTimestamp = timestamps.get(i - 1);

            double currentClose = klineMap.get(currentTimestamp).getClose();
            double previousClose = klineMap.get(previousTimestamp).getClose();

            double change = currentClose - previousClose;
            double currentGain = Math.max(0, change);
            double currentLoss = Math.max(0, -change);

            if (!initialized) {
                gain += currentGain;
                loss += currentLoss;
                if (i == period) {
                    initialized = true;
                    gain /= period;
                    loss /= period;
                }
                continue;
            }

            gain = (gain * (period - 1) + currentGain) / period;
            loss = (loss * (period - 1) + currentLoss) / period;

            double rs = gain / loss;
            double rsi = 100 - (100 / (1 + rs));
            rsiMap.put(currentTimestamp, rsi);
        }
        return rsiMap;
    }

    // 计算EMA（指数移动平均）
    public static Map<Long, Double> calculateEMA(Map<Long, Kline> klineMap, int period) {
        Map<Long, Double> emaMap = new LinkedHashMap<>();
        List<Long> sortedTimestamps = CollectionUtils.getTimestamps(klineMap);

        if (sortedTimestamps.size() >= period) {
            double k = 2.0 / (period + 1);

            // 初始 EMA 使用前 period 天的简单移动平均值
            double initialEMA = 0.0;
            for (int i = 0; i < period; i++) {
                initialEMA += klineMap.get(sortedTimestamps.get(i)).getClose();
            }
            initialEMA /= period;

            // 保存初始 EMA
            emaMap.put(sortedTimestamps.get(period - 1), initialEMA);

            // 计算后续的 EMA
            double previousEMA = initialEMA;
            for (int i = period; i < sortedTimestamps.size(); i++) {
                long timestamp = sortedTimestamps.get(i);
                double currentClose = klineMap.get(timestamp).getClose();
                double currentEMA = currentClose * k + previousEMA * (1 - k);
                emaMap.put(timestamp, currentEMA);
                previousEMA = currentEMA;
            }
        }

        return emaMap;
    }

    // 计算MACD（平滑异同移动平均）
    public static Map<Long, Double> calculateMACD(Map<Long, Kline> klineMap) {
        List<Long> sortedTimestamps = CollectionUtils.getTimestamps(klineMap);

        Map<Long, Double> macdMap = new LinkedHashMap<>();
        if (sortedTimestamps.size() >= 26) {
            // 计算短期 (12 日) 和长期 (26 日) 的 EMA
            Map<Long, Double> shortEMA = calculateEMA(klineMap, 12);
            Map<Long, Double> longEMA = calculateEMA(klineMap, 26);

            // 计算 MACD，只有当 26 日 EMA 可用时才计算
            for (int i = 25; i < sortedTimestamps.size(); i++) {
                long timestamp = sortedTimestamps.get(i);
                double macd = shortEMA.get(timestamp) - longEMA.get(timestamp);
                macdMap.put(timestamp, macd);
            }
        }

        return macdMap;
    }
}
