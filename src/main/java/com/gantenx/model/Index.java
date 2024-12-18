package com.gantenx.model;

public class Index extends Time {
    private Double rsi;

    public Index(long timestamp) {
        super(timestamp);
    }

    public Double getRsi() {
        return rsi;
    }

    public void setRsi(Double rsi) {
        this.rsi = rsi;
    }
}
