package com.gantenx.nethuns.commons.utils;

import com.gantenx.nethuns.commons.model.Time;

import java.util.*;
import java.util.stream.Collectors;


public class CollectionUtils {

    public static <T extends Time> Map<Long, T> toTimeMap(List<T> dataList) {
        return dataList.stream().collect(Collectors.toMap(Time::getTimestamp, t -> t));
    }

    public static <T extends Time> Map<Long, List<T>> toListMap(List<T> dataList) {
        return dataList.stream().collect(Collectors.groupingBy(Time::getTimestamp,
                                                               Collectors.mapping(t -> t, Collectors.toList())));
    }

    public static double getMaxValue(Map<Long, Double> map) {
        if (map.isEmpty()) {
            return 100.0;
        }
        return Collections.max(map.values());
    }

    public static <T,V> T get(Map<V, Map<Long, T>> map, V symbol, long timestamp) {
        Map<Long, T> dataMap = map.get(symbol);
        if (Objects.nonNull(dataMap)) {
            return dataMap.get(timestamp);
        }
        return null;
    }

    public static double getMinValue(Map<Long, Double> map) {
        if (map.isEmpty()) {
            return 0.0;
        }
        return Collections.min(map.values());
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        if (Objects.isNull(collection)) {
            return true;
        }
        return collection.isEmpty();
    }

    public static <T> List<Long> getTimestamps(Map<Long, T> klineMap) {
        List<Long> list = new ArrayList<>(klineMap.keySet());
        Collections.sort(list);
        return list;
    }
}
