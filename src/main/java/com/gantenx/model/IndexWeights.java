package com.gantenx.model;

public class IndexWeights {
    private double rsi;
    private double macd;
    private double bollinger;

    public IndexWeights(double rsi, double macd, double bollinger) {
        this.rsi = rsi;
        this.macd = macd;
        this.bollinger = bollinger;
    }

    public double getRsi() {
        return rsi;
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    public double getMacd() {
        return macd;
    }

    public void setMacd(double macd) {
        this.macd = macd;
    }

    public double getBollinger() {
        return bollinger;
    }

    public void setBollinger(double bollinger) {
        this.bollinger = bollinger;
    }
}
