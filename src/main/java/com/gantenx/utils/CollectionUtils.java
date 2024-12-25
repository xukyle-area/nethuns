package com.gantenx.utils;

import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.model.Time;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
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

    public static <O> Pair<Double, Double> getRange(Map<Long, O> map, Function<O, Double> handler) {
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (Map.Entry<Long, O> entry : map.entrySet()) {
            double aDouble = handler.apply(entry.getValue());
            max = Math.max(max, aDouble);
            min = Math.max(min, aDouble);
        }
        return Pair.create(min, max);
    }

    public static Map<Series, Map<Long, Double>> toSeriesPriceMap(Map<Symbol, Map<Long, Kline>> klineMap,
                                                                  Set<Symbol> symbols) {
        Map<Series, Map<Long, Double>> objectObjectHashMap = new HashMap<>();
        for (Map.Entry<Symbol, Map<Long, Kline>> entry : klineMap.entrySet()) {
            Symbol symbol = entry.getKey();
            if (symbols.contains(symbol)) {
                Map<Long, Double> priceMap = CollectionUtils.toPriceMap(entry.getValue());
                objectObjectHashMap.put(Series.getSeries(symbol), priceMap);
            }
        }
        return objectObjectHashMap;
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

    public static double getPrice(Map<Symbol, Map<Long, Kline>> map, Symbol symbol, long timestamp) {
        Kline kline = get(map, symbol, timestamp);
        if (Objects.nonNull(kline)) {
            return kline.getOpen();
        }
        throw new IllegalArgumentException("Illegal symbol or timestamp to get price, " + symbol + ", " + DateUtils.getDate(
                timestamp));
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

    public static Pair<Double, Double> getRange(Map<Long, Kline> map) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (Kline kline : map.values()) {
            min = Math.min(min, kline.getLow());
            max = Math.max(max, kline.getHigh());
        }

        return Pair.create(min, max);
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
