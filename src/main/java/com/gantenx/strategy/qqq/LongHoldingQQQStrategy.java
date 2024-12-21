package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.model.Kline;
import com.gantenx.chart.qqq.RSIChart;
import com.gantenx.utils.CollectionUtils;
import org.checkerframework.checker.units.qual.K;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.QQQSymbol.QQQ;
import static com.gantenx.constant.QQQSymbol.TQQQ;

public class LongHoldingQQQStrategy extends BaseQQQStrategy {

    public LongHoldingQQQStrategy(String startStr, String endStr) {
        super(LongHoldingQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(klineMap.get(QQQ), 6);
        List<Long> timestamps =  CollectionUtils.getTimestamps(rsiOfQQQ);
        for (long timestamp : timestamps) {
            Double rsi = rsiOfQQQ.get(timestamp);
            Kline tqqqCandle = klineMap.get(TQQQ).get(timestamp);
            Kline qqqCandle = klineMap.get(QQQ).get(timestamp);
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeEngine.hasPosition()) {
                tradeEngine.buy(QQQ, PROPORTION_OF_100, "没有仓位的时候，持有QQQ");
            }
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        RSIChart chart = new RSIChart(klineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
