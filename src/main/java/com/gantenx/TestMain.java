package com.gantenx;

import com.gantenx.chart.Chart;
import com.gantenx.chart.ChartUtils;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.service.KlineService;
import com.gantenx.trend.PriceTrendIdentifier;
import com.gantenx.trend.TrendIdentifier;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExportUtils;
import org.jfree.chart.JFreeChart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gantenx.constant.Constants.CRYPTO_SYMBOL_LIST;
import static com.gantenx.constant.Period.ONE_DAY;
import static com.gantenx.constant.Series.BTC;
import static com.gantenx.constant.Symbol.BTCUSDT;

public class TestMain {
    private static final TrendIdentifier identifier = new PriceTrendIdentifier();

    public static void main(String[] args) {
        String startStr = "20240101";
        long start = DateUtils.getTimestamp(startStr);
        String endStr = "20241220";
        long end = DateUtils.getTimestamp(endStr);
        List<Long> timestampList = DateUtils.genTimeList(ONE_DAY, start, end);
        Map<Symbol, Map<Long, Kline>> symbolKlineMap = KlineService.getSymbolKlineMap(CRYPTO_SYMBOL_LIST,
                                                                                      ONE_DAY,
                                                                                      timestampList);
        Map<Series, Map<Long, Double>> dataMap = CollectionUtils.toSeriesPriceMap(symbolKlineMap,
                                                                                  symbolKlineMap.keySet());
        Map<Long, Kline> kline = symbolKlineMap.get(BTCUSDT);
        Map<Long, Double> priceMap = CollectionUtils.toPriceMap(kline);

        JFreeChart freeChart = ChartUtils.getJFreeChart(new ArrayList<>(), Pair.create(BTC, priceMap), dataMap);
        ExportUtils.saveJFreeChartAsImage(freeChart, startStr, endStr, "测试导出", "k线");
    }


}
