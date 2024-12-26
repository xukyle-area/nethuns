package com.gantenx.nethuns.trend;


import com.gantenx.nethuns.commons.constant.Trend;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceTrendIdentifier implements TrendIdentifier {

    private static final double strong_threshold = 0.02;
    private static final double wave_threshold = 0.012;

    /**
     * 按照涨幅算:
     * > 0.01 涨
     * 0 ~ 0.01 略涨
     * -0.01 ~ 0.01 波动
     * 0 ~ 0.01 略跌
     * < 0.01 跌
     */
    @Override
    public Map<Long, Trend> identify(Map<Long, Kline> klineMap, List<Long> timestampList, int period) {
        HashMap<Long, Trend> map = new HashMap<>();
        for (Long timestamp : timestampList) {
            List<Kline> klineList = this.getRecentKlines(klineMap, timestamp, period);
            if (klineList.size() < period) {
                continue;
            }
            // 平均涨幅或者跌幅
            double[] increase = new double[period];
            String date = DateUtils.getDate(timestamp);
            double highest = 0d;
            for (int i = 0; i < period; i++) {
                Kline kline = klineList.get(i);
                highest = Math.max(highest, kline.getOpen());
                increase[i] = kline.getClose() / kline.getOpen() - 1;
            }
            double average = Arrays.stream(increase).average().orElse(0);
            Trend trend = this.getTrend(average);
            map.put(timestamp,trend);
        }
        return map;
    }

    private Trend getTrend(double average) {
        Trend trend;
        if (average > strong_threshold) {
            trend = Trend.STRONG_UPTREND;
        } else if (average < -strong_threshold) {
            trend = Trend.STRONG_DOWNTREND;
        } else if (average > wave_threshold && average < strong_threshold) {
            trend = Trend.UPTREND;
        } else if (average < -wave_threshold && average > -strong_threshold) {
            trend = Trend.DOWNTREND;
        } else {
            trend = Trend.SIDEWAYS;
        }
        return trend;
    }

    public double calculateVariance(double[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }

        // Step 1: Calculate the mean
        double sum = 0;
        for (double value : array) {
            sum += value;
        }
        double mean = sum / array.length;

        // Step 2: Calculate the variance
        double variance = 0;
        for (double value : array) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= array.length; // Divide by n to get variance

        return variance;
    }

}
