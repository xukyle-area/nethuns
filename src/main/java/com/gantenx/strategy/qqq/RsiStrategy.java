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

import static com.gantenx.constant.SymbolType.*;

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
        if (!tradeMocker.hasPosition()) {
            tradeMocker.buyAll(QQQ, qqqPrice, ts);
        }

        if (rsi < 25) {
            this.allinTQQQ(tqqqPrice, qqqPrice, ts);
            return;
        } else if (rsi > 85) {
            this.allinSQQQ(sqqqPrice, qqqPrice, ts);
            return;
        }
        if (tradeMocker.hasPosition(SQQQ) && rsi <= 60) {
            this.alloutSQQQ(sqqqPrice, qqqPrice, ts);
        } else if (tradeMocker.hasPosition(TQQQ) && rsi >= 60) {
            this.alloutTQQQ(tqqqPrice, qqqPrice, ts);
        }
    }

    public void allinTQQQ(double tqqqPrice, double qqqPrice, long ts) {
        tradeMocker.sellAll(QQQ, qqqPrice, ts);
        tradeMocker.buyAll(TQQQ, tqqqPrice, ts);
    }

    public void alloutTQQQ(double tqqqPrice, double qqqPrice, long ts) {
        tradeMocker.sellAll(TQQQ, tqqqPrice, ts);
        tradeMocker.buyAll(QQQ, qqqPrice, ts);
    }

    public void allinSQQQ(double sqqqPrice, double qqqPrice, long ts) {
        tradeMocker.sellAll(QQQ, qqqPrice, ts);
        tradeMocker.buyAll(SQQQ, sqqqPrice, ts);
    }

    public void alloutSQQQ(double sqqqPrice, double qqqPrice, long ts) {
        tradeMocker.sellAll(SQQQ, sqqqPrice, ts);
        tradeMocker.buyAll(QQQ, qqqPrice, ts);
    }

    @Override
    protected JFreeChart getChart() {
        RSIChart chart = new RSIChart(qqqKlineMap, tqqqKlineMap, sqqqKlineMap, tradeDetail.getOrders());
        return chart.getCombinedChart();
    }
}
