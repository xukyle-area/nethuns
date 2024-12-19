package com.gantenx.model;

public class Index extends Time {
    private Double rsi;
    private Double sma;
    private Double ema;
    private double[] bollingerBands;
    private double weightedScore;
    private double macd;
    private Double macdSignal;  // 新增字段
    private String signalStrength;

    public Double getMacdSignal() {
        return macdSignal;
    }

    public void setMacdSignal(Double macdSignal) {
        this.macdSignal = macdSignal;
    }

    public Index(long timestamp) {
        super(timestamp);
    }

    public Double getRsi() {
        return rsi;
    }

    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }

    public Double getEma() {
        return ema;
    }

    public void setEma(Double ema) {
        this.ema = ema;
    }

    public Double getSma() {
        return sma;
    }

    public String getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(String signalStrength) {
        this.signalStrength = signalStrength;
    }

    public void setSma(Double sma) {
        this.sma = sma;
    }

    public double[] getBollingerBands() {
        return bollingerBands;
    }

    public void setBollingerBands(double[] bollingerBands) {
        this.bollingerBands = bollingerBands;
    }

    public double getWeightedScore() {
        return weightedScore;
    }

    public void setWeightedScore(double weightedScore) {
        this.weightedScore = weightedScore;
    }

    public double getMacd() {
        return macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }
}
