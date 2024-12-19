package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.calculator.TradeMocker;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LongHoldingQQQStrategy extends BaseStrategy {

    public LongHoldingQQQStrategy(double initialBalance, double fee, String startStr, String endStr) {
        super(initialBalance, fee, "long-holding-qqq", DateUtils.getTimestamp(startStr), DateUtils.getTimestamp(endStr));
    }

    @Override
    public void openTrade() {
        Map<Long, Double> rsiOfQQQ = IndexTechnicalIndicators.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps =  CollectionUtils.getTimestamps(rsiOfQQQ);

        TradeMocker tradeMocker = new TradeMocker(super.initialBalance, super.fee);
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
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
        }
    }
}
