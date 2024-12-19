package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.constant.Constants;
import com.gantenx.model.*;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class WeightedStrategy extends BaseStrategy {


    public WeightedStrategy(double initialBalance, double fee, String startStr, String endStr) {
        super(initialBalance, fee, "weighted-strategy", DateUtils.getTimestamp(startStr), DateUtils.getTimestamp(endStr));
    }

    @Override
    protected void openTrade() {
        // 计算 QQQ 的 k 线加权参数
        Map<Long, Index> indexMap = IndexCalculator.getIndexMap(qqqKlineMap, Constants.INDEX_WEIGHTS, Constants.INDEX_PERIOD);
        List<Long> timestamps = CollectionUtils.getTimestamps(indexMap);
        String name = "index-data";
        ExportUtils.exportWorkbook(ExcelUtils.singleSheet(CollectionUtils.toList(indexMap), name), strategyName, name);

        // 开启模拟交易
        for (long ts : timestamps) {
            Index index = indexMap.get(ts);
            Double rsi = index.getRsi();
            Kline qqqCandle = qqqKlineMap.get(ts);
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
        }
    }


    @Override
    protected JFreeChart getChart() {
        WeightScoreChart weightScoreChart = new WeightScoreChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return weightScoreChart.getCombinedChart();
    }
}
