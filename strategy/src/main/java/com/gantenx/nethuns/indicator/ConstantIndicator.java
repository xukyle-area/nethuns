package com.gantenx.nethuns.indicator;


import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.indicator.base.AbstractIndicator;

public class ConstantIndicator<T> extends AbstractIndicator<T> {

    private final T value;

    /**
     * Constructor.
     *
     * @param series the bar series
     * @param t      the constant value
     */
    public ConstantIndicator(Map<Long, Candle> series, T t) {
        super(series);
        this.value = t;
    }

    @Override
    public T getValue(long index) {
        return value;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " Value: " + value;
    }

    @Override
    protected Map<Long, T> calculate() {
        HashMap<Long, T> map = new HashMap<>();
        for (Long timestamp : super.getTimestamps()) {
            map.put(timestamp, value);
        }
        return map;
    }

    @Override
    public JFreeChart getChart() {
        return null;
    }
}
