package com.gantenx.util;

import com.gantenx.model.Time;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionUtils {
    /**
     * 将带有时间的List,映射成map
     */
    public static <T extends Time> Map<Long, T> toTimeMap(List<T> klineList) {
        return klineList.stream().collect(Collectors.toMap(Time::getTimestamp, kline -> kline));
    }
}
