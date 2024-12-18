package com.gantenx.calculator;

import com.gantenx.model.Index;
import com.gantenx.model.Kline;

import java.util.*;

public class IndexCalculator {

    public Map<Long, Index> calculateIndex(Map<Long, Kline> klineMap) {
        HashMap<Long, Index> map = new HashMap<>();
        Map<Long, Double> smaMap = calculateSMA(klineMap, 6);
        Map<Long, double[]> bollingerBandsMap = calculateBollingerBands(smaMap, klineMap, 6);
        Map<Long, Double> rsiMap = calculateRSI(klineMap, 6);
        for (Long ts : klineMap.keySet()) {
            Index index = new Index(ts);
            index.setSma(smaMap.get(ts));
            index.setRsi(rsiMap.get(ts));
            index.setCalculateBollingerBands(bollingerBandsMap.get(ts));
            map.put(ts, index);
        }
        return map;
    }

    // Calculate SMA (Simple Moving Average)
    public static Map<Long, Double> calculateSMA(Map<Long, Kline> klineMap, int period) {
        Map<Long, Double> smaMap = new TreeMap<>();
        List<Long> timestamps = new ArrayList<>(klineMap.keySet());
        Collections.sort(timestamps);

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

    // Calculate Bollinger Bands
    public static Map<Long, double[]> calculateBollingerBands(Map<Long, Double> smaMap, Map<Long, Kline> klineMap, int period) {
        Map<Long, double[]> bollingerMap = new TreeMap<>();

        List<Long> timestamps = new ArrayList<>(klineMap.keySet());
        Collections.sort(timestamps);

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

    // Calculate RSI
    public static Map<Long, Double> calculateRSI(Map<Long, Kline> klineMap, int period) {
        Map<Long, Double> rsiMap = new TreeMap<>();
        List<Long> timestamps = new ArrayList<>(klineMap.keySet());
        Collections.sort(timestamps);

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
}

