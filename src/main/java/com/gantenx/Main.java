package com.gantenx;

import com.gantenx.model.Kline;
import com.gantenx.strategy.AlphaQQQStrategy;
import com.gantenx.util.CollectionUtils;
import com.gantenx.util.CsvUtils;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) {
        replay("20231106", "20241210");
    }


    public static void replay(String startStr, String endStr) {
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

        AlphaQQQStrategy strategy = new AlphaQQQStrategy(100000L, 0.00001);
        strategy.process(qqqKlineMap, tqqqKlineMap, sqqqKlineMap);
        strategy.printTradeDetail();
    }
}
