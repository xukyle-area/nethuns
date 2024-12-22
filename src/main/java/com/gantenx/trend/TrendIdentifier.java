package com.gantenx.trend;

import com.gantenx.constant.Trend;
import com.gantenx.model.Kline;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface TrendIdentifier {
    Map<Long, Trend> identify(Map<Long, Kline> klineMap, List<Long> timestampList, int period);

    default List<Kline> getRecentKlines(Map<Long, Kline> klineMap, long timestamp, int period) {
        return klineMap.entrySet().stream()
                .filter(entry -> entry.getKey() < timestamp)
                .sorted(Map.Entry.<Long, Kline>comparingByKey().reversed())
                .limit(period)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
