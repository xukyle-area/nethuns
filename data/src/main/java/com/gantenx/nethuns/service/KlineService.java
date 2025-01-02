package com.gantenx.nethuns.service;


import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.CollectionUtils;
import com.gantenx.nethuns.commons.utils.CsvUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.gantenx.nethuns.commons.constant.From.CSV;


@Slf4j
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
}
