package com.gantenx;

import com.gantenx.chart.ChartUtils;
import com.gantenx.constant.Series;
import com.gantenx.constant.Symbol;
import com.gantenx.model.Kline;
import com.gantenx.model.Pair;
import com.gantenx.service.KlineService;
import com.gantenx.strategy.SingleRsiStrategy;
import com.gantenx.strategy.template.BaseStrategy;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExportUtils;
import org.jfree.chart.JFreeChart;

import java.util.Map;

import static com.gantenx.constant.Period.ONE_DAY;
import static com.gantenx.constant.Symbol.BTCUSDT;

public class TestMain {

    public static void main(String[] args) {
        String startStr = "20240101";
        String endStr = "20241220";
        Symbol symbol = BTCUSDT;
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        SingleRsiStrategy singleRsiStrategy = new SingleRsiStrategy(symbol, ONE_DAY, start, end);
        BaseStrategy.processAndExport(singleRsiStrategy);
//        Map<Long, Kline> kLineMap = KlineService.getKLineMap(symbol, ONE_DAY, start, end);
//        Pair<Series, Map<Long, Kline>> pair = Pair.create(Series.getSeries(symbol), kLineMap);
//        JFreeChart freeChart = ChartUtils.getCandleChart(null, null, pair);
//        ExportUtils.saveJFreeChartAsImage(freeChart, startStr, endStr, "测试导出", "数据");
    }
}
