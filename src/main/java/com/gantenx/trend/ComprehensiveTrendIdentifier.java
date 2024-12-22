package com.gantenx.trend;

import com.gantenx.constant.Trend;
import com.gantenx.model.Kline;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComprehensiveTrendIdentifier implements TrendIdentifier {

    @Override
    public Map<Long, Trend> identify(Map<Long, Kline> klineMap, List<Long> timestampList, int period) {
        Map<Long, Trend> trendMap = new LinkedHashMap<>();
        if (klineMap == null || klineMap.isEmpty() || timestampList == null || timestampList.isEmpty()) {
            return trendMap;
        }

        // 对每个指定的时间点进行趋势判断
        for (Long ts : timestampList) {
            // 获取该时间点之前的period根K线
            List<Kline> periodKlineList = getRecentKlines(klineMap, ts, period);
            if (periodKlineList.size() < period) {
                trendMap.put(ts, Trend.SIDEWAYS); // 数据不足时返回震荡
                continue;
            }

            // 计算趋势
            Trend trend = calculateTrend(periodKlineList);
            trendMap.put(ts, trend);
        }

        return trendMap;
    }

    private Trend calculateTrend(List<Kline> klineList) {
        // 计算关键指标
        double[] returns = calculateReturns(klineList);
        double avgReturn = calculateAvgReturn(returns);
        double volatility = calculateVolatility(returns);
        int consecutiveDirection = calculateConsecutiveDirection(returns);
        double momentum = calculateMomentum(returns);

        // 评分系统
        int score = 0;

        // 评估平均收益率
        if (avgReturn > 0.02) score += 2;      // 2%以上
        else if (avgReturn > 0.005) score += 1; // 0.5%以上
        else if (avgReturn < -0.02) score -= 2;
        else if (avgReturn < -0.005) score -= 1;

        // 评估连续性
        if (Math.abs(consecutiveDirection) >= 3) {
            score += (int) Math.signum(consecutiveDirection);
        }

        // 评估动量
        if (momentum > 0.015) score += 2;
        else if (momentum > 0.005) score += 1;
        else if (momentum < -0.015) score -= 2;
        else if (momentum < -0.005) score -= 1;

        // 根据波动率调整分数
        if (volatility > 0.03) {
            score = (int) (score * 0.8);
        }

        return scoreTrend(score);
    }


    private double[] calculateReturns(List<Kline> klineList) {
        double[] returns = new double[klineList.size() - 1];
        for (int i = 0; i < klineList.size() - 1; i++) {
            returns[i] = (klineList.get(i).getClose() - klineList.get(i + 1).getClose()) / klineList.get(i + 1).getClose();
        }
        return returns;
    }

    private double calculateAvgReturn(double[] returns) {
        return Arrays.stream(returns).average().orElse(0);
    }

    private double calculateVolatility(double[] returns) {
        double avg = calculateAvgReturn(returns);
        return Math.sqrt(Arrays.stream(returns).map(r -> Math.pow(r - avg, 2)).average().orElse(0));
    }

    private int calculateConsecutiveDirection(double[] returns) {
        int consecutive = 0;
        int maxConsecutive = 0;

        for (int i = 1; i < returns.length; i++) {
            if (Math.signum(returns[i]) == Math.signum(returns[i - 1])) {
                consecutive += (int) Math.signum(returns[i]);
                maxConsecutive = Math.abs(consecutive) > Math.abs(maxConsecutive) ? consecutive : maxConsecutive;
            } else {
                consecutive = (int) Math.signum(returns[i]);
            }
        }

        return maxConsecutive;
    }

    private double calculateMomentum(double[] returns) {
        int mid = returns.length / 2;
        double laterHalf = Arrays.stream(returns, 0, mid).average().orElse(0);
        double firstHalf = Arrays.stream(returns, mid, returns.length).average().orElse(0);
        return laterHalf - firstHalf;
    }

    private Trend scoreTrend(int score) {
        if (score >= 4) return Trend.STRONG_UPTREND;
        if (score >= 2) return Trend.UPTREND;
        if (score <= -4) return Trend.STRONG_DOWNTREND;
        if (score <= -2) return Trend.DOWNTREND;
        return Trend.SIDEWAYS;
    }
}