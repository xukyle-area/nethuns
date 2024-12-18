package com.gantenx.strategy;

import com.gantenx.calculator.IndexCalculator;
import com.gantenx.calculator.TradeCalculator;
import com.gantenx.model.Kline;
import com.gantenx.model.TradeDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.util.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public class AlphaQQQStrategy extends AbstractQQQStrategy {

    public AlphaQQQStrategy(double initialBalance, double fee) {
        super(initialBalance, fee, "alpha");
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

    /**
     * 开始到结束，长期持有 QQQ 的收益
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param kline QQQ 的 K 线
     * @return 策略执行过后的订单列表，盈利信息等
     */
    private static TradeDetail longTermHolding(long start, long end, String symbol, Map<Long, Kline> kline) {
        Kline qqqLastCandle = null;
        long lastTs = 0;

        TradeCalculator tradeCalculator = new TradeCalculator(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            Kline qqqCandle = kline.get(ts);
            qqqLastCandle = qqqCandle;
            if (Objects.isNull(qqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            lastTs = ts;
            double qqqPrice = qqqCandle.getClose();
            // 没有仓位的时候，持有QQQ
            if (!tradeCalculator.hasPosition()) {
                tradeCalculator.buyAll(symbol, qqqPrice, ts);
            }
        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put(symbol, qqqLastCandle.getClose());
        return tradeCalculator.exit(priceMap, lastTs);
    }
}
