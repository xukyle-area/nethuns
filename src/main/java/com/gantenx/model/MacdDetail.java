package com.gantenx.model;

import lombok.Data;

import java.awt.*;

@Data
public class MacdDetail {
    private Double macdLine;  // MACD 主线
    private Double signalLine;  // 信号线
    private Double histogram;  // 直方图高度
    private Color histogramColor;  // 直方图颜色
}