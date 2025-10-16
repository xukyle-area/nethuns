package com.gantenx.nethuns.indicator.model;

import java.awt.Color;
import lombok.Data;

@Data
public class MacdDetail {
    // MACD 主线
    private Double macdLine;
    // 信号线
    private Double signalLine;
    // 直方图高度
    private Double histogram;
    // 直方图颜色
    private Color histogramColor;
}
