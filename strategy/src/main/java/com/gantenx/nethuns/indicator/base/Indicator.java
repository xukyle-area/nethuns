package com.gantenx.nethuns.indicator.base;


import java.util.Map;
import com.gantenx.nethuns.commons.model.Kline;

public interface Indicator<T> {

    T getValue(long timestamp);

    Map<Long, Kline> getKlineMap();
}
