package com.gantenx.nethuns.commons.model;

public class Candle extends Time {
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;

    public Candle(long timestamp) {
        super(timestamp);
    }

    public Candle(long timestamp, double open, double high, double low, double close, double volume) {
        super(timestamp);
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
