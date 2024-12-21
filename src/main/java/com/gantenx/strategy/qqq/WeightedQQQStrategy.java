package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.chart.qqq.WeightScoreChart;
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

import static com.gantenx.constant.Constants.*;
import static com.gantenx.constant.QQQSymbol.QQQ;

@Slf4j
public class WeightedQQQStrategy extends BaseQQQStrategy {

    public WeightedQQQStrategy(String startStr, String endStr) {
        super(WeightedQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    protected void openTrade() {
        Map<Long, Index> indexMap = IndexCalculator.getIndexMap(klineMap.get(QQQ), INDEX_WEIGHTS, INDEX_PERIOD);
        String name = "index-data";
        ExportUtils.exportWorkbook(ExcelUtils.singleSheet(CollectionUtils.toList(indexMap), name), startStr, endStr, strategyName, name);
        while (tradeEngine.hasNextDay()) {
            tradeEngine.nextDay();
            if (!tradeEngine.hasPosition()) {
                tradeEngine.buy(QQQ, PROPORTION_OF_100, "没有仓位的时候，持有QQQ");
            }
        }
    }


    @Override
    protected JFreeChart getTradingChart() {
        WeightScoreChart weightScoreChart = new WeightScoreChart(klineMap, tradeDetail.getOrders());
        return weightScoreChart.getCombinedChart();
    }
}
