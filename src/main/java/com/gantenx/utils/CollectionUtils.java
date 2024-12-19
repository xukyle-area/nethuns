package com.gantenx.utils;

import com.gantenx.model.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static <T extends Time> Map<Long, T> toTimeMap(List<T> klineList) {
        return klineList.stream().collect(Collectors.toMap(Time::getTimestamp, kline -> kline));
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
}
