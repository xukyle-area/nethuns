package com.gantenx.utils;

import com.gantenx.model.Kline;
import com.gantenx.model.Time;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY;

@Slf4j
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

    public static <T> boolean isComplete(Map<Long, T> dataMap, String start, String end) {
        long startTimestamp = DateUtils.getTimestamp(start);
        long endTimestamp = DateUtils.getTimestamp(end);
        for (long i = startTimestamp; i <= endTimestamp; i += MS_OF_ONE_DAY) {
            T obj = dataMap.get(i);
            if (Objects.isNull(obj)) {
                log.error("Data missing for: {}", DateUtils.getDate(i));
                return false;
            }
            log.error("Data {} for {}", JsonUtils.toJson(obj), DateUtils.getDate(i));
        }
        return true;
    }

    public static double getMaxValue(Map<Long, Double> map) {
        if (map.isEmpty()) {
            return 100.0;
        }
        return Collections.max(map.values());
    }

    public static double getMinValue(Map<Long, Double> map) {
        if (map.isEmpty()) {
            return 0.0;
        }
        return Collections.min(map.values());
    }

    public static Map<Long, Double> toPriceMap(Map<Long, Kline> klineMap) {
        HashMap<Long, Double> map = new HashMap<>();
        for (Map.Entry<Long, Kline> entry : klineMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getClose());
        }
        return map;
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
