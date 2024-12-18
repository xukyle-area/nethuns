package com.gantenx.strategy.qqq;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.calculator.TradeCalculator;
import com.gantenx.model.Kline;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AlphaStrategy extends BaseStrategy {

    public AlphaStrategy(double initialBalance, double fee) {
        super(initialBalance, fee, "alpha-strategy");
    }

    @Override
    public void process(Map<Long, Kline> qqqKlineMap, Map<Long, Kline> tqqqKlineMap, Map<Long, Kline> sqqqKlineMap) {
        Map<Long, Double> rsiOfQQQ = IndexCalculator.calculateRSI(qqqKlineMap, 6);
        List<Long> timestamps = new ArrayList<>(rsiOfQQQ.keySet());
        Collections.sort(timestamps);

        Kline tqqqLastCandle = null;
        Kline qqqLastCandle = null;
        long lastTs = 0;

        TradeCalculator tradeCalculator = new TradeCalculator(super.initialBalance, super.fee);
        for (long ts : timestamps) {
            Double rsi = rsiOfQQQ.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            tqqqLastCandle = tqqqCandle;
            Kline qqqCandle = qqqKlineMap.get(ts);
            qqqLastCandle = qqqCandle;
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            lastTs = ts;
            double tqqqPrice = tqqqCandle.getClose();
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeCalculator.hasPosition()) {
                tradeCalculator.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达高点，抛售 TQQQ，进行长期持有 QQQ
            if (rsi > 70) {
                tradeCalculator.sellAll("TQQQ", tqqqPrice, ts);
                tradeCalculator.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达低点，抛售 QQQ，进行短期期持有 TQQQ
            if (rsi < 30) {
                tradeCalculator.sellAll("QQQ", qqqPrice, ts);
                tradeCalculator.buyAll("TQQQ", tqqqPrice, ts);
            }
        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", qqqLastCandle.getClose());
        priceMap.put("TQQQ", tqqqLastCandle.getClose());
        super.tradeDetail = tradeCalculator.exit(priceMap, lastTs);
    }
}
