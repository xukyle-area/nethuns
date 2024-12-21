package com.gantenx.utils;

import com.gantenx.model.Kline;
import com.gantenx.model.Time;

import java.util.*;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static <T extends Time> Map<Long, T> toTimeMap(List<T> klineList) {
        return klineList.stream().collect(Collectors.toMap(Time::getTimestamp, kline -> kline));
    }

    public static <T extends Time> T getLast(Map<Long, T> klineMap) {
        if (klineMap == null || klineMap.isEmpty()) {
            return null;
        }

        Long maxTimestamp = Collections.max(klineMap.keySet());
        return klineMap.get(maxTimestamp);
    }

    public static <T extends Time> Long findLatestTime(Map<Long, T> klineMap) {
        if (klineMap == null || klineMap.isEmpty()) {
            return null;
        }

        return Collections.max(klineMap.keySet());
    }


    public static <T> boolean isEmpty(Collection<T> collection) {
        if (Objects.isNull(collection)) {
            return true;
        }
        return collection.isEmpty();
    }

    public static <T> List<T> toList(Map<Long, T> klineMap) {
        // 按照 Map 的 key 进行排序后，将对应的 T 对象添加到列表中
        return klineMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 按照 key 升序排序
                .map(Map.Entry::getValue)          // 提取 T 对象
                .collect(Collectors.toList());     // 转换为 List
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
