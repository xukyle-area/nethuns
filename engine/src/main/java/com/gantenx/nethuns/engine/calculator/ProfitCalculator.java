package com.gantenx.nethuns.engine.calculator;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.engine.model.ProfitRate;

public class ProfitCalculator {
    public static List<ProfitRate> calculator(List<Long> timestampList, Map<Symbol, Map<Long, Kline>> klineMap) {
        long start = timestampList.get(0);
        long end = timestampList.get(timestampList.size() - 1);
        List<ProfitRate> list = new ArrayList<>();
        for (Map.Entry<Symbol, Map<Long, Kline>> entry : klineMap.entrySet()) {
            Symbol symbol = entry.getKey();
            Map<Long, Kline> entryValue = entry.getValue();
            double startPrice = entryValue.get(start).getClose();
            double endPrice = entryValue.get(end).getClose();
            ProfitRate profitRate = new ProfitRate();
            profitRate.setEndPrice(endPrice);
            profitRate.setStartPrice(startPrice);
            profitRate.setSymbol(symbol);
            profitRate.setDays(DateUtils.getDaysBetween(start, end));
            profitRate.setRate(endPrice / startPrice);
            list.add(profitRate);
        }
        return list;
    }
}
