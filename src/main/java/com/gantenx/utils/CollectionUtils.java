package com.gantenx.utils;

import com.gantenx.model.Time;

import java.util.ArrayList;
import java.util.Collections;
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

    public static <T> List<Long> getTimestamps(Map<Long, T> klineMap) {
        List<Long> list = new ArrayList<>(klineMap.keySet());
        Collections.sort(list);
        return list;
    }
}
