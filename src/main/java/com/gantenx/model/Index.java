package com.gantenx.model;

import java.util.List;

public class Index extends Time {
    private Double rsi;
    private Double sma;
    private double[] calculateBollingerBands;
    private double weightedScore;
    private double macd;

    public Index(long timestamp) {
        super(timestamp);
    }

    public Double getRsi() {
        return rsi;
    }

    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }

    public Double getSma() {
        return sma;
    }

    public void setSma(Double sma) {
        this.sma = sma;
    }

    public double[] getCalculateBollingerBands() {
        return calculateBollingerBands;
    }

    public void setCalculateBollingerBands(double[] calculateBollingerBands) {
        this.calculateBollingerBands = calculateBollingerBands;
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
