package com.gantenx.calculator;

import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.ProfitRate;
import com.gantenx.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LongHoldingProfitCalculator {
    public static List<ProfitRate> calculator(List<Long> openDays, Map<Symbol, Map<Long, Kline>> klineMap) {
        long start = openDays.get(0);
        long end = openDays.get(openDays.size() - 1);
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
