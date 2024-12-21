package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.chart.WeightScoreChart;
import com.gantenx.constant.Constants;
import com.gantenx.model.*;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.ExcelUtils;
import com.gantenx.utils.ExportUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.Symbol.QQQ;

@Slf4j
public class WeightedStrategy extends BaseStrategy {

    public WeightedStrategy(String startStr, String endStr) {
        super(WeightedStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    protected void openTrade() {
        // 计算 QQQ 的 k 线加权参数
        Map<Long, Index> indexMap = IndexCalculator.getIndexMap(qqqKlineMap, Constants.INDEX_WEIGHTS, Constants.INDEX_PERIOD);
        List<Long> timestamps = CollectionUtils.getTimestamps(indexMap);
        String name = "index-data";
        ExportUtils.exportWorkbook(ExcelUtils.singleSheet(CollectionUtils.toList(indexMap), name), startStr, endStr, strategyName, name);

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
            if (tradeEngine.hasNoPosition()) {
                tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, ts, "没有仓位的时候，持有QQQ");
            }
        }
    }


    @Override
    protected JFreeChart getTradingChart() {
        WeightScoreChart weightScoreChart = new WeightScoreChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return weightScoreChart.getCombinedChart();
    }
}
