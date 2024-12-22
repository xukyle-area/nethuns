package com.gantenx.utils;

import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Time;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class CollectionUtils {

    public static <T extends Time> Map<Long, T> toTimeMap(List<T> dataList) {
        return dataList.stream().collect(Collectors.toMap(Time::getTimestamp, kline -> kline));
    }

    public static <T extends Time> Map<Long, List<T>> toListMap(List<T> dataList) {
        return dataList.stream().collect(Collectors.groupingBy(Time::getTimestamp,
                                                               Collectors.mapping(kline -> kline,
                                                                                  Collectors.toList())));
    }

    public static double getMaxValue(Map<Long, Double> map) {
        if (map.isEmpty()) {
            return 100.0;
        }
        return Collections.max(map.values());
    }

    @Nullable
    public static <T> T get(Map<Symbol, Map<Long, T>> map, Symbol symbol, long timestamp) {
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

    public static <T> Long getMinKey(Map<Long, T> map) {
        if (map.isEmpty()) {
            return 0L;
        }
        return Collections.min(map.keySet());
    }

    public static <T> Long getMaxKey(Map<Long, T> map) {
        if (map.isEmpty()) {
            return 0L;
        }
        return Collections.max(map.keySet());
    }

    public static Map<Long, Double> toPriceMap(Map<Long, Kline> klineMap) {
        HashMap<Long, Double> map = new HashMap<>();
        for (Map.Entry<Long, Kline> entry : klineMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getClose());
        }
        return map;
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


    public static <T, U extends Collection<T>> Collection<T> toCollection(Collection<U> dataList) {
        Collection<T> result = new ArrayList<>();
        for (U tCollection : dataList) {
            result.addAll(tCollection);
        }
        return result;
    }
}
