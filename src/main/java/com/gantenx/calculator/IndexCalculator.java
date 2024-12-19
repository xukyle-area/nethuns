package com.gantenx.calculator;

import com.gantenx.model.IndexPeriod;
import com.gantenx.model.IndexWeights;
import com.gantenx.model.Index;
import com.gantenx.model.Kline;

import java.util.HashMap;
import java.util.Map;

public class IndexCalculator {
    private final Map<Long, Kline> klineMap;
    private Map<Long, Double> smaMap;
    private Map<Long, double[]> bollingerBandsMap;
    private Map<Long, Double> rsiMap;
    private Map<Long, Double> emaMap;
    private Map<Long, Double> macdMap;
    private final IndexWeights indexWeights;
    private final IndexPeriod indexPeriod;

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
        macdMap = IndexTechnicalIndicators.calculateMACD(klineMap);
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
                bollingerBandsMap.containsKey(ts) &&
                rsiMap.containsKey(ts) &&
                macdMap.containsKey(ts);
    }

    private void setIndicatorValues(Index index, Long ts, Kline kline) {
        index.setSma(smaMap.get(ts));
        index.setRsi(rsiMap.get(ts));
        index.setCalculateBollingerBands(bollingerBandsMap.get(ts));
        index.setMacd(macdMap.get(ts));

        double score = calculateWeightedScore(ts, kline.getClose());
        index.setWeightedScore(score);
    }

    private double calculateWeightedScore(Long ts, double closePrice) {
        double rsi = rsiMap.get(ts);
        double macd = macdMap.get(ts);
        double ema = emaMap.get(ts);
        double[] bollingerBands = bollingerBandsMap.get(ts);

        return weightedScore(rsi, macd, ema, bollingerBands, closePrice);
    }

    public static Map<Long, Index> getIndexMap(Map<Long, Kline> klineMap, IndexWeights indexWeights, IndexPeriod indexPeriod) {
        IndexCalculator calculator = new IndexCalculator(klineMap, indexWeights, indexPeriod);
        return calculator.calculate();
    }

    private double weightedScore(double rsi, double macd, double ema,
                                 double[] bollingerBands, double closePrice) {
        return calculateRSIScore(rsi) * indexWeights.getRsi() +
                calculateMACDScore(macd, ema) * indexWeights.getMacd() +
                calculateBollingerScore(closePrice, bollingerBands[2]) * indexWeights.getBollinger();
    }

    // 计算RSI得分
    private double calculateRSIScore(double rsi) {
        if (rsi < 30) {
            return 1; // RSI 低于 30 是强烈超卖
        } else if (rsi >= 30 && rsi < 50) {
            return (50 - rsi) / 20; // RSI 介于 30 到 50 之间，给一个递减的得分
        } else {
            return 0; // RSI 大于 50，表示强势市场
        }
    }

    // 计算MACD得分
    private double calculateMACDScore(double macd, double signal) {
        double macdDiff = macd - signal;
        if (macdDiff > 0.05) {
            return 1; // MACD 强烈超越信号线
        } else if (macdDiff > 0) {
            return macdDiff / 0.05; // MACD 轻微超越信号线
        } else {
            return 0; // MACD 低于信号线
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