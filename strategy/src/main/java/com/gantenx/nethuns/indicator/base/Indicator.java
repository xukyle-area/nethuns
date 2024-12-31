package com.gantenx.nethuns.indicator.base;


import com.gantenx.nethuns.commons.model.Kline;

import java.util.Map;

public interface Indicator<T> {

    T getValue(long timestamp);

    Map<Long, Kline> getKlineMap();
}
