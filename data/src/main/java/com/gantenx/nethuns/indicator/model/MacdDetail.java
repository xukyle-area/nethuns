package com.gantenx.nethuns.indicator.model;

import java.awt.Color;
import lombok.Data;

@Data
public class MacdDetail {
    private Double macdLine; // MACD 主线
    private Double signalLine; // 信号线
    private Double histogram; // 直方图高度
    private Color histogramColor; // 直方图颜色
}
