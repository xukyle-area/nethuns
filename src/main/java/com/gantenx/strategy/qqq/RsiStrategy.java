package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.RSIChart;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.Symbol.*;

@Slf4j
public class RsiStrategy extends BaseStrategy {

    public RsiStrategy(String startStr, String endStr) {
        super(RsiStrategy.class.getSimpleName(), startStr, endStr);
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps = CollectionUtils.getTimestamps(rsiOfQQQ);

        for (long ts : timestamps) {
            Double rsi = rsiOfQQQ.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            Kline qqqCandle = qqqKlineMap.get(ts);
            Kline sqqqCandle = sqqqKlineMap.get(ts);
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                continue;
            }
            this.dailyTrade(tqqqCandle.getClose(), qqqCandle.getClose(), sqqqCandle.getClose(), rsi, ts);
        }
    }

    /**
     * 策略为，如果
     */
    private void dailyTrade(double tqqqPrice, double qqqPrice, double sqqqPrice, double rsi, long ts) {
        // 没有仓位的时候，持有QQQ
        if (tradeEngine.hasNoPosition()) {
            tradeEngine.buy(QQQ, qqqPrice, PROPORTION_OF_100, ts, "没有任何，卖入QQQ");
        }

        if (rsi < 25) {
            tradeEngine.sell(QQQ, qqqPrice, PROPORTION_OF_100, ts, "RSI达到 " + rsi + ": 超卖，卖出QQQ, 置换到TQQQ");
            tradeEngine.buy(TQQQ, tqqqPrice, PROPORTION_OF_100, ts, "RSI达到 " + rsi + ": 超卖，买入TQQQ");
            return;
        } else if (rsi > 85) {
            tradeEngine.sell(QQQ, qqqPrice, PROPORTION_OF_100, ts, "RSI达到 " + rsi + ": 超卖，卖出QQQ, 置换到SQQQ");
            tradeEngine.buy(SQQQ, sqqqPrice, PROPORTION_OF_100, ts, "RSI达到 " + rsi + ": 超卖，买入SQQQ");
            return;
        }
        if (tradeEngine.hasPosition(SQQQ) && rsi <= 60) {
            tradeEngine.sell(SQQQ,
                             sqqqPrice,
                             PROPORTION_OF_100,
                             ts,
                             "RSI达到 " + rsi + ": 原先持仓SQQQ，目前判断已经下跌，卖出SQQQ，置换到QQQ");
            tradeEngine.buy(QQQ,
                            qqqPrice,
                            PROPORTION_OF_100,
                            ts,
                            "RSI达到 " + rsi + ": 原先持仓SQQQ，目前判断已经下跌，买入QQQ");
        } else if (tradeEngine.hasPosition(TQQQ) && rsi >= 60) {
            tradeEngine.sell(TQQQ,
                             tqqqPrice,
                             PROPORTION_OF_100,
                             ts,
                             "RSI达到 " + rsi + ": 原先持仓TQQQ，目前判断已经上涨，卖出TQQQ，置换到QQQ");
            tradeEngine.buy(QQQ,
                            qqqPrice,
                            PROPORTION_OF_100,
                            ts,
                            "RSI达到 " + rsi + ": 原先持仓TQQQ，目前判断已经上涨，买入QQQ");
        }
    }

    @Override
    protected JFreeChart getChart() {
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
