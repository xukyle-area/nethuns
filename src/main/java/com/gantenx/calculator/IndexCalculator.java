package com.gantenx.calculator;

import com.gantenx.model.IndexPeriod;
import com.gantenx.model.IndexWeights;
import com.gantenx.model.Index;
import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class IndexCalculator {
    private final Map<Long, Kline> klineMap;
    private Map<Long, Double> smaMap;
    private Map<Long, double[]> bollingerBandsMap;
    private Map<Long, Double> rsiMap;
    private Map<Long, Double> emaMap;
    private Map<Long, Double> macdMap;
    private Map<Long, Double> macdSignalMap;  // 添加MACD信号线
    private final IndexWeights indexWeights;
    private final IndexPeriod indexPeriod;

    // MACD计算的阈值常量
    private static final double MACD_THRESHOLD = 2.0;

    public IndexCalculator(Map<Long, Kline> klineMap, IndexWeights indexWeights, IndexPeriod indexPeriod) {
        this.klineMap = klineMap;
        this.indexPeriod = indexPeriod;
        this.indexWeights = indexWeights;
    }

    public Map<Long, Index> calculate() {
        calculateIndicators();
        return createIndexMap();
    }

    private void calculateIndicators() {
        smaMap = IndexTechnicalIndicators.calculateSMA(klineMap, indexPeriod.getBollinger());
        bollingerBandsMap = IndexTechnicalIndicators.calculateBollingerBands(smaMap, klineMap, indexPeriod.getBollinger());
        rsiMap = IndexTechnicalIndicators.calculateRSI(klineMap, indexPeriod.getRsi());
        emaMap = IndexTechnicalIndicators.calculateEMA(klineMap, indexPeriod.getEma());

        // 获取MACD和信号线
        Map<String, Map<Long, Double>> macdResults = IndexTechnicalIndicators.calculateMACDWithSignal(klineMap);
        macdMap = macdResults.get("macd");
        macdSignalMap = macdResults.get("signal");
    }

    private Map<Long, Index> createIndexMap() {
        Map<Long, Index> resultMap = new HashMap<>(klineMap.size());

        klineMap.forEach((ts, kline) -> {
            Index index = createIndex(ts, kline);
            if (index != null) {
                resultMap.put(ts, index);
            }
        });

        return resultMap;
    }

    private Index createIndex(Long ts, Kline kline) {
        if (!allIndicatorsAvailable(ts)) {
            return null;
        }

        Index index = new Index(ts);
        setIndicatorValues(index, ts, kline);
        return index;
    }

    private boolean allIndicatorsAvailable(Long ts) {
        return smaMap.containsKey(ts) &&
                bollingerBandsMap.containsKey(ts) && rsiMap.containsKey(ts) && macdMap.containsKey(ts) && macdSignalMap.containsKey(ts);
    }

    private void setIndicatorValues(Index index, Long ts, Kline kline) {
        index.setSma(smaMap.get(ts));
        index.setRsi(rsiMap.get(ts));
        index.setEma(emaMap.get(ts));
        index.setBollingerBands(bollingerBandsMap.get(ts));
        index.setMacd(macdMap.get(ts));
        index.setMacdSignal(macdSignalMap.get(ts));  // 设置MACD信号线值

        double score = calculateWeightedScore(ts, kline.getClose());
        index.setWeightedScore(score);
        index.setSignalStrength(getSignalStrength(score));
    }

    private String getSignalStrength(double score) {
        if (score > 0.8) return "Strong Buy";
        else if (score > 0.6) return "Buy";
        else if (score > 0.4) return "Hold";
        else if (score > 0.2) return "Sell";
        else return "Strong Sell";
    }

    private double calculateWeightedScore(Long ts, double closePrice) {
        double rsi = rsiMap.get(ts);
        double macd = macdMap.get(ts);
        double signal = macdSignalMap.get(ts);
        double[] bollingerBands = bollingerBandsMap.get(ts);

        return weightedScore(rsi, macd, signal, bollingerBands, closePrice);
    }

    public static Map<Long, Index> getIndexMap(Map<Long, Kline> klineMap, IndexWeights indexWeights, IndexPeriod indexPeriod) {
        IndexCalculator calculator = new IndexCalculator(klineMap, indexWeights, indexPeriod);
        return calculator.calculate();
    }

    private double weightedScore(double rsi, double macd, double signal,
                                 double[] bollingerBands, double closePrice) {
        double rsiScore = calculateRSIScore(rsi) * indexWeights.getRsi();
        double macdScore = calculateMACDScore(macd, signal) * indexWeights.getMacd();
        double bollingerScore = calculateBollingerScore(closePrice, bollingerBands[2]) * indexWeights.getBollinger();

        log.info("Scores - RSI: {}, MACD: {}, Bollinger: {}", rsiScore, macdScore, bollingerScore);

        return rsiScore + macdScore + bollingerScore;
    }

    // 计算RSI得分
    private double calculateRSIScore(double rsi) {
        if (rsi < 20) {
            return 1;
        } else if (rsi > 80) {
            return 0;
        }

        return (80 - rsi) / 60; // RSI 介于 30 到 50 之间，给一个递减的得分
    }

    private double calculateMACDScore(double macd, double signal) {
        double diff = macd - signal;
        double threshold = Math.abs(signal) * 0.01; // 使用信号线的1%作为阈值

        if (diff > threshold) {
            return 1.0; // 强烈买入信号
        } else if (diff > 0) {
            return diff / threshold; // 介于0和1之间的买入信号
        } else {
            return 0.0; // 卖出信号
        }
    }

    // 计算布林带得分
    private double calculateBollingerScore(double closePrice, double lowerBand) {
        double distance = closePrice - lowerBand;
        if (distance <= 0) {
            return 1; // 收盘价接近或低于下轨，强烈超卖
        } else {
            return Math.max(0, 1 - distance / lowerBand); // 收盘价离下轨越远，得分越低
        }
    }
}