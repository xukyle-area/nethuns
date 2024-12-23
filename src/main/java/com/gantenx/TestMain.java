package com.gantenx;

import com.gantenx.constant.Symbol;
import com.gantenx.retrofit.RetrofitClient;
import com.gantenx.strategy.SingleRsiStrategy;
import com.gantenx.strategy.template.BaseStrategy;
import com.gantenx.utils.DateUtils;

import static com.gantenx.constant.Period.CSV;
import static com.gantenx.constant.Symbol.QQQUSD;

public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240101";
        String endStr = "20241220";
        Symbol symbol = QQQUSD;
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        SingleRsiStrategy singleRsiStrategy = new SingleRsiStrategy(symbol, CSV, start, end);
        BaseStrategy.processAndExport(singleRsiStrategy);
//        Map<Long, Kline> kLineMap = KlineService.getKLineMap(symbol, ONE_DAY, start, end);
//        Pair<Series, Map<Long, Kline>> pair = Pair.create(Series.getSeries(symbol), kLineMap);
//        JFreeChart freeChart = ChartUtils.getCandleChart(null, null, pair);
//        ExportUtils.saveJFreeChartAsImage(freeChart, startStr, endStr, "测试导出", "数据");
        RetrofitClient.shutdown();
    }
}
