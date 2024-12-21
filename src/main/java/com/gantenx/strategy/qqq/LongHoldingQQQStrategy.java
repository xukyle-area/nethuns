package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.model.Kline;
import com.gantenx.chart.RSIChart;
import com.gantenx.utils.CollectionUtils;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.QQQSymbol.QQQ;

public class LongHoldingQQQStrategy extends BaseQQQStrategy {

    public LongHoldingQQQStrategy(String startStr, String endStr) {
        super(LongHoldingQQQStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps =  CollectionUtils.getTimestamps(rsiOfQQQ);
        for (long ts : timestamps) {
            Double rsi = rsiOfQQQ.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            Kline qqqCandle = qqqKlineMap.get(ts);
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
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
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
