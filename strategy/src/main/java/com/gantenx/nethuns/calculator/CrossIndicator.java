package com.gantenx.nethuns.calculator;

import com.gantenx.nethuns.calculator.base.AbstractIndicator;
import com.gantenx.nethuns.calculator.base.Indicator;
import org.jfree.chart.JFreeChart;

import java.util.HashMap;
import java.util.Map;

public class CrossIndicator extends AbstractIndicator<Boolean> {

    private final Indicator<Double> up;
    private final Indicator<Double> low;

    public CrossIndicator(Indicator<Double> up, Indicator<Double> low) {
        super(up.getKlineMap());
        this.low = low;
        this.up = up;
        super.resultMap = this.calculate();
    }

    public CrossIndicator(Indicator<Double> up, Double low) {
        super(up.getKlineMap());
        this.up = up;
        this.low = new ConstantIndicator<>(up.getKlineMap(), low);
    }

    /**
     * up 线从上而下穿越 low 线的时刻，返回 true
     */
    @Override
    protected Map<Long, Boolean> calculate() {
        Map<Long, Boolean> resultMap = new HashMap<>();

        // 用于跟踪上一个时间点的差值
        Double previousDelta = null;

        for (Long timestamp : super.getTimestamps()) {
            // 获取上轨和下轨的值
            Double upValue = up.getValue(timestamp);
            Double lowValue = low.getValue(timestamp);

            // 如果值为空，直接标记为 false，并重置状态
            if (upValue == null || lowValue == null) {
                resultMap.put(timestamp, false);
                previousDelta = null;
                continue;
            }

            // 当前差值
            Double delta = upValue - lowValue;

            if (delta > 0) {
                // 上轨大于下轨，无交叉
                resultMap.put(timestamp, false);
            } else {
                // 检查是否从正变为负，判断是否发生交叉
                boolean isCrossed = previousDelta == null || previousDelta > 0;
                resultMap.put(timestamp, isCrossed);
            }

            // 更新 previousDelta
            previousDelta = delta;
        }

        return resultMap;
    }

    @Override
    public JFreeChart getChart() {
        return null;
    }

    public Indicator<Double> getLow() {
        return low;
    }

    public Indicator<Double> getUp() {
        return up;
    }

    @Override
    public String toString() {
        return super.getClass().getSimpleName() + " " + low + " " + up;
    }
}
