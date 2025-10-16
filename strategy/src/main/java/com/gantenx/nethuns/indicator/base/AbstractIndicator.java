package com.gantenx.nethuns.indicator.base;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jfree.chart.JFreeChart;
import com.gantenx.nethuns.commons.model.Kline;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class AbstractIndicator<T> implements Indicator<T> {

    private final Map<Long, Kline> klineMap;
    private final List<Long> timestamps;
    protected Map<Long, T> resultMap;

    protected AbstractIndicator(Map<Long, Kline> klineMap) {
        this.klineMap = klineMap;
        this.timestamps = klineMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public Map<Long, Kline> getKlineMap() {
        return klineMap;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    protected List<Long> getTimestamps() {
        return timestamps;
    }


    protected abstract Map<Long, T> calculate();

    @Override
    public synchronized T getValue(long timestamp) {
        return resultMap.get(timestamp);
    }

    public abstract JFreeChart getChart();
}
