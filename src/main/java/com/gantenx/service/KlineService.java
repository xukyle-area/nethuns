package com.gantenx.service;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.Period;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.CsvUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.From.CSV;

public class KlineService {
    public static Map<Long, Kline> getKLineMap(Symbol symbol, Period period, long startTime, long endTime) {
        List<Kline> kline;
        if (CSV.equals(symbol.getFrom())) {
            kline = CsvUtils.getKLineList(symbol.getPath(), startTime, endTime);
        } else {
            kline = BinanceService.getKline(symbol.getPath(), period, startTime, endTime);
        }
        return CollectionUtils.toTimeMap(kline);
    }

    public static Map<Symbol, Map<Long, Kline>> getSymbolKlineMap(List<Symbol> list, Period period, List<Long> timestampList) {
        HashMap<Symbol, Map<Long, Kline>> map = new HashMap<>();
        long startTimestamp = timestampList.get(0);
        long endTimestamp = timestampList.get(timestampList.size() - 1);
        for (Symbol symbol : list) {
            Map<Long, Kline> kLineMap = KlineService.getKLineMap(symbol, period, startTimestamp, endTimestamp);
            map.put(symbol, kLineMap);
        }
        return map;
    }

    public static Map<Symbol, Map<Long, Double>> genRsiMap(Map<Symbol, Map<Long, Kline>> klineMap,
                                                           List<Symbol> symbols) {
        HashMap<Symbol, Map<Long, Double>> hashMap = new HashMap<>();
        for (Symbol symbol : symbols) {
            Map<Long, Double> map = IndexTechnicalIndicators.calculateRSI(klineMap.get(symbol));
            hashMap.put(symbol, map);
        }
        return hashMap;
    }

}
