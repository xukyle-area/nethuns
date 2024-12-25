package com.gantenx.utils.calculator;

import com.gantenx.constant.Index;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.gantenx.constant.Constants.RSI_PERIOD;
import static com.gantenx.constant.Index.DIF;
import static com.gantenx.constant.Index.EMA;

public class IndexTechnicalIndicators {
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

    // MACD计算
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
            macdDetail.setCross(isCross(macdDetail, resultMap.get(previousTimestamp)));
            // 颜色逻辑
            Color color = determineHistogramColor(previousTimestamp, macdDetail, resultMap);
            macdDetail.setHistogramColor(color);

            previousTimestamp = timestamp;
        }

        return resultMap;
    }

    // 交叉点计算方法
    private static boolean isCross(MacdDetail macdDetail, MacdDetail previousMacdDetail) {
        if (Objects.isNull(previousMacdDetail)) {
            return false;
        }
        double previousMacd = previousMacdDetail.getMacdLine();
        double previousSignal = previousMacdDetail.getSignalLine();
        double currentMacd = macdDetail.getMacdLine();
        double currentSignal = macdDetail.getSignalLine();
        return (previousMacd <= previousSignal && currentMacd > currentSignal) || (previousMacd >= previousSignal && currentMacd < currentSignal);
    }

    // 颜色计算方法
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
}
