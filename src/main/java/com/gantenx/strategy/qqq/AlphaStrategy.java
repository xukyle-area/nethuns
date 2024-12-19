package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.model.Kline;
import com.gantenx.model.RSIChart;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AlphaStrategy extends BaseStrategy {

    public AlphaStrategy(double initialBalance, double fee, String startStr, String endStr) {
        super(initialBalance, fee, "alpha-strategy", DateUtils.getTimestamp(startStr), DateUtils.getTimestamp(endStr));
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps = CollectionUtils.getTimestamps(rsiOfQQQ);

        for (long ts : timestamps) {
            Double rsi = rsiOfQQQ.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            Kline qqqCandle = qqqKlineMap.get(ts);
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            double tqqqPrice = tqqqCandle.getClose();
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达高点，抛售 TQQQ，进行长期持有 QQQ
            if (rsi > 70) {
                tradeMocker.sellAll("TQQQ", tqqqPrice, ts);
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达低点，抛售 QQQ，进行短期期持有 TQQQ
            if (rsi < 30) {
                tradeMocker.sellAll("QQQ", qqqPrice, ts);
                tradeMocker.buyAll("TQQQ", tqqqPrice, ts);
            }
        }
    }

    @Override
    protected JFreeChart getChart() {
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
