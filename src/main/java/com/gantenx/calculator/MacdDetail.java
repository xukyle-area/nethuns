package com.gantenx.calculator;

public class MacdDetail {
    public Double macdLine;  // MACD 主线
    public Double signalLine;  // 信号线
    public Double histogram;  // 直方图高度
    public Boolean isCross;  // 是否交叉
    public String histogramColor;  // 直方图颜色

    public Double getMacdLine() {
        return macdLine;
    }

    public void setMacdLine(Double macdLine) {
        this.macdLine = macdLine;
    }

    public Double getSignalLine() {
        return signalLine;
    }

    public void setSignalLine(Double signalLine) {
        this.signalLine = signalLine;
    }

    public Double getHistogram() {
        return histogram;
    }

    public void setHistogram(Double histogram) {
        this.histogram = histogram;
    }

    public Boolean getCross() {
        return isCross;
    }

    public void setCross(Boolean cross) {
        isCross = cross;
    }

    public String getHistogramColor() {
        return histogramColor;
    }

    public void setHistogramColor(String histogramColor) {
        this.histogramColor = histogramColor;
    }
}