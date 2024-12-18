package com.gantenx.strategy;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.calculator.TradeCalculator;
import com.gantenx.model.Kline;

import java.util.*;

public class LongHoldingQQQStrategy extends AbstractQQQStrategy {

    public LongHoldingQQQStrategy(double initialBalance, double fee) {
        super(initialBalance, fee, "long holding QQQ");
    }

    @Override
    public void process(Map<Long, Kline> qqqKlineMap, Map<Long, Kline> tqqqKlineMap, Map<Long, Kline> sqqqKlineMap) {
        Map<Long, Double> rsiOfQQQ = IndexCalculator.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps = new ArrayList<>(rsiOfQQQ.keySet());
        Collections.sort(timestamps);

        Kline qqqLastCandle = null;
        long lastTs = 0;

        TradeCalculator tradeCalculator = new TradeCalculator(super.initialBalance, super.fee);
        for (long ts : timestamps) {
            Double rsi = rsiOfQQQ.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            Kline qqqCandle = qqqKlineMap.get(ts);
            qqqLastCandle = qqqCandle;
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            lastTs = ts;
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeCalculator.hasPosition()) {
                tradeCalculator.buyAll("QQQ", qqqPrice, ts);
            }

        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", qqqLastCandle.getClose());
        super.tradeDetail = tradeCalculator.exit(priceMap, lastTs);
    }
}
