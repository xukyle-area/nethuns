package com.gantenx.nethuns;


import static com.gantenx.nethuns.commons.enums.From.CSV;
import java.util.List;
import java.util.Map;
import com.gantenx.nethuns.commons.enums.Period;
import com.gantenx.nethuns.commons.enums.Symbol;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.CollectionUtils;
import com.gantenx.nethuns.commons.utils.CsvUtils;
import com.gantenx.nethuns.source.binance.restful.BinanceService;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CandleService {
    public static Map<Long, Candle> getKLineMap(Symbol symbol, Period period, long startTime, long endTime) {
        List<Candle> kline;
        if (CSV.equals(symbol.getFrom())) {
            kline = CsvUtils.getKLineList(symbol.getPath(), startTime, endTime);
        } else {
            kline = BinanceService.getKline(symbol.getPath(), period, startTime, endTime);
        }
        return CollectionUtils.toTimeMap(kline);
    }
}
