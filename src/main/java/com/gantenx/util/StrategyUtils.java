package com.gantenx.util;

import com.gantenx.model.Kline;
import com.gantenx.model.TradeDetail;
import com.gantenx.model.TradingChart;
import com.gantenx.strategy.qqq.BaseStrategy;

import java.util.List;
import java.util.Map;

public class StrategyUtils {
    public static void replay(BaseStrategy strategy, String startStr, String endStr) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);

        // 从 CSV 文件中获取历史数据
        List<Kline> qqqKlineList = CsvUtils.getKLineFromCsv("data/QQQ.csv", start, end);
        List<Kline> tqqqKlineList = CsvUtils.getKLineFromCsv("data/TQQQ.csv", start, end);
        List<Kline> sqqqKlineList = CsvUtils.getKLineFromCsv("data/SQQQ.csv", start, end);
        // 转换成 map 格式
        Map<Long, Kline> tqqqKlineMap = CollectionUtils.toTimeMap(tqqqKlineList);
        Map<Long, Kline> sqqqKlineMap = CollectionUtils.toTimeMap(sqqqKlineList);
        Map<Long, Kline> qqqKlineMap = CollectionUtils.toTimeMap(qqqKlineList);

        strategy.process(qqqKlineMap, tqqqKlineMap, sqqqKlineMap);
        TradeDetail tradeDetail = strategy.printTradeDetail();

        TradingChart tradingChart = new TradingChart(qqqKlineList, tqqqKlineList, tradeDetail.getOrders());
        ChartUtils.saveJFreeChartAsImage(tradingChart.getCombinedChart(), "export/" + strategy.getStrategyName() + ".png", 1600, 1200);
    }
}
