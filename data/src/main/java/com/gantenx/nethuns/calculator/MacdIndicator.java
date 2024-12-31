package com.gantenx.nethuns.calculator;

import com.gantenx.nethuns.commons.constant.Index;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.calculator.model.MacdDetail;
import com.gantenx.nethuns.commons.utils.CollectionUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.gantenx.nethuns.commons.constant.Index.DIF;
import static com.gantenx.nethuns.commons.constant.Index.EMA;


public class MacdIndicator {

    public static Map<Long, MacdDetail> calculateMACDWithDetails(Map<Long, Kline> klineMap) {

        int fastLength = 12;
        int slowLength = 26;
        int signalLength = 9;
        // Index 包括 DIF 和 EMA
        Map<Index, Map<Long, Double>> indexMapMap = calculateMACD(klineMap, fastLength, slowLength, signalLength);

        // 构建 MACD 主线（DIF）
        Map<Long, MacdDetail> resultMap = new HashMap<>();
        Map<Long, Double> difMap = indexMapMap.get(DIF);
        Map<Long, Double> emaMap = indexMapMap.get(EMA);

        // 设置 MACD 主线和信号线
        for (Long timestamp : difMap.keySet()) {
            Double dif = difMap.get(timestamp);
            Double ema = emaMap.get(timestamp);

            if (Objects.isNull(dif) || Objects.isNull(ema)) {
                continue;
            }
            MacdDetail macdDetail = new MacdDetail();
            macdDetail.setMacdLine(dif);
            macdDetail.setSignalLine(ema);
            macdDetail.setHistogram(dif - ema);
            resultMap.put(timestamp, macdDetail);
        }

        // 交叉点与直方图颜色计算
        List<Long> sortedTimestamps = CollectionUtils.getTimestamps(resultMap);
        Long previousTimestamp = null;

        for (Long timestamp : sortedTimestamps) {
            MacdDetail macdDetail = resultMap.get(timestamp);
            // 颜色逻辑
            Color color = determineHistogramColor(previousTimestamp, macdDetail, resultMap);
            macdDetail.setHistogramColor(color);
            previousTimestamp = timestamp;
        }

        return resultMap;
    }

    private static Color determineHistogramColor(Long previousTimestamp,
                                                 MacdDetail macdDetail,
                                                 Map<Long, MacdDetail> resultMap) {
        if (previousTimestamp == null) {
            return Color.GRAY; // 没有前一个时间点，返回默认颜色
        } else {
            double previousHistogram = resultMap.get(previousTimestamp).getHistogram();
            double currentHistogram = macdDetail.getHistogram();

            if (currentHistogram > 0) {
                // 直方图值大于 0
                return currentHistogram > previousHistogram ? Color.RED : Color.PINK;
            } else {
                // 直方图值小于 0
                return currentHistogram > previousHistogram ? Color.GREEN.brighter() : Color.GREEN.darker();
            }
        }
    }

    private static Map<Index, Map<Long, Double>> calculateMACD(Map<Long, Kline> klineMap,
                                                               int fastLength,
                                                               int slowLength,
                                                               int signalLength) {
        Map<Long, Double> shortEMA = calculateEMA(klineMap, fastLength);  // 12日EMA
        Map<Long, Double> longEMA = calculateEMA(klineMap, slowLength);   // 26日EMA

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
        Map<Long, Double> signalLine = calculateEMA(macdLine, signalLength);

        Map<Index, Map<Long, Double>> result = new HashMap<>();
        result.put(DIF, macdLine);
        result.put(EMA, signalLine);

        return result;
    }

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

    private static double getValue(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof Kline) {
            return ((Kline) obj).getClose();
        }
        throw new IllegalArgumentException("Unsupported data type");
    }

}
