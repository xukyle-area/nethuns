package com.gantenx.nethuns.engine.indicator.base;


import java.util.Map;
import com.gantenx.nethuns.commons.model.Candle;

public interface Indicator<T> {

    T getValue(long timestamp);

    Map<Long, Candle> getKlineMap();
}
