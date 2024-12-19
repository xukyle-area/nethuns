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

    // MACD计算
    public static Map<String, Map<Long, Double>> calculateMACDWithSignal(Map<Long, Kline> klineMap) {
        Map<Long, Double> shortEMA = calculateEMA(klineMap, 12);  // 12日EMA
        Map<Long, Double> longEMA = calculateEMA(klineMap, 26);   // 26日EMA

        // 计算MACD线（DIF）
        Map<Long, Double> macdLine = new HashMap<>();
        for (Long timestamp : klineMap.keySet()) {
            if (shortEMA.containsKey(timestamp) && longEMA.containsKey(timestamp)) {
                double shortValue = shortEMA.get(timestamp);
                double longValue = longEMA.get(timestamp);
                macdLine.put(timestamp, shortValue - longValue);
            }
        }

        // 计算信号线（DEA）- 9日EMA
        Map<Long, Double> signalLine = calculateEMA(macdLine, 9);

        Map<String, Map<Long, Double>> result = new HashMap<>();
        result.put("macd", macdLine);
        result.put("signal", signalLine);

        return result;
    }

    // 计算EMA
    public static Map<Long, Double> calculateEMA(Map<Long, ?> data, int period) {
        Map<Long, Double> emaMap = new TreeMap<>();
        List<Long> timestamps = new ArrayList<>(data.keySet());
        Collections.sort(timestamps);

        // 初始化EMA（使用前period天的SMA作为第一个EMA值）
        if (timestamps.size() >= period) {
            double sum = 0;
            for (int i = 0; i < period; i++) {
                Long timestamp = timestamps.get(i);
                double value = getValue(data.get(timestamp));
                sum += value;
            }
            double firstEMA = sum / period;
            emaMap.put(timestamps.get(period - 1), firstEMA);

            // 计算后续的EMA
            double multiplier = 2.0 / (period + 1);
            double previousEMA = firstEMA;

            for (int i = period; i < timestamps.size(); i++) {
                Long timestamp = timestamps.get(i);
                double currentValue = getValue(data.get(timestamp));
                double currentEMA = (currentValue - previousEMA) * multiplier + previousEMA;
                emaMap.put(timestamp, currentEMA);
                previousEMA = currentEMA;
            }
        }

        return emaMap;
    }

    // 辅助方法：获取值
    private static double getValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof Kline) {
            return ((Kline) obj).getClose();
        }
        throw new IllegalArgumentException("Unsupported data type");
    }
}
