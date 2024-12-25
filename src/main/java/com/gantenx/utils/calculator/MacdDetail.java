package com.gantenx.utils.calculator;

import java.awt.*;

public class MacdDetail {
    private Double macdLine;  // MACD 主线
    private Double signalLine;  // 信号线
    private Double histogram;  // 直方图高度
    private Boolean isCross;  // 是否交叉
    private Color histogramColor;  // 直方图颜色

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

    public Color getHistogramColor() {
        return histogramColor;
    }

    public void setHistogramColor(Color histogramColor) {
        this.histogramColor = histogramColor;
    }
}