package com.gantenx.model;

public class IndexPeriod {
    private int rsi;
    private int ema;
    private int bollinger;

    public IndexPeriod(int rsiPeriod, int ema, int bollinger) {
        this.rsi = rsiPeriod;
        this.ema = ema;
        this.bollinger = bollinger;
    }

    public int getRsi() {
        return rsi;
    }

    public void setRsi(int rsi) {
        this.rsi = rsi;
    }

    public int getEma() {
        return ema;
    }

    public void setEma(int ema) {
        this.ema = ema;
    }

    public int getBollinger() {
        return bollinger;
    }

    public void setBollinger(int bollinger) {
        this.bollinger = bollinger;
    }
}