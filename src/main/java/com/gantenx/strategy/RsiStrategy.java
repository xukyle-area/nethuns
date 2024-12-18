package com.gantenx.strategy;

import com.gantenx.model.Kline;
import com.gantenx.model.KlineWithRSI;
import com.gantenx.model.TradeDetail;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.util.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public class RsiStrategy {

    public TradeDetail process(List<KlineWithRSI> klineWithRsiList) {
        long lastTs = 0;
        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (int i = 0; i < klineWithRsiList.size(); i++) {
            KlineWithRSI kLineWithRsi = klineWithRsiList.get(i);
            Double rsi = kLineWithRsi.getRsi();
            if (rsi == null) {
                continue;
            }
            double price = Double.parseDouble(kLineWithRsi.getClose());
            long closeTime = kLineWithRsi.getTime();
            lastTs = closeTime;
            if (rsi > 70) {
                tradeMocker.sellAll("X", price, closeTime);
            }
            if (rsi < 30) {
                tradeMocker.buyAll("X", price, closeTime);
            }
        }
        return tradeMocker.exit(new HashMap<>(), lastTs);
    }

    public TradeDetail processA(String startStr, String endStr, Map<Long, Kline> tqqqKlineMap, Map<Long, KlineWithRSI> qqqRsiMap) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline tqqqLastKline = null;
        KlineWithRSI qqqLastKlineWithRsi = null;
        long lastTs = 0;

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            KlineWithRSI kwr = qqqRsiMap.get(ts);
            if (Objects.isNull(kwr) || Objects.isNull(kwr.getRsi())) {
                // 说明今日美股不开市
                continue;
            }
            Double rsi = kwr.getRsi();
            Kline kline = tqqqKlineMap.get(ts);
            tqqqLastKline = kline;
            qqqLastKlineWithRsi = kwr;
            lastTs = ts;
            double tqqqPrice = Double.parseDouble(kline.getClose());
            double qqqPrice = Double.parseDouble(kwr.getClose());
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
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", Double.parseDouble(qqqLastKlineWithRsi.getClose()));
        priceMap.put("TQQQ", Double.parseDouble(tqqqLastKline.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }

    public TradeDetail processB(String startStr, String endStr, Map<Long, Kline> tqqqKlineMap, Map<Long, KlineWithRSI> qqqRsiMap) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline tqqqLastKline = null;
        KlineWithRSI qqqLastKlineWithRsi = null;
        long lastTs = 0;

        // 跟踪 RSI 触发的状态
        boolean rsiAbove = false;  // RSI 曾经大于 80
        boolean rsiBelow = false;  // RSI 曾经小于 20

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            KlineWithRSI kwr = qqqRsiMap.get(ts);
            if (Objects.isNull(kwr) || Objects.isNull(kwr.getRsi())) {
                // 说明今日美股不开市
                continue;
            }
            Double rsi = kwr.getRsi();
            Kline kline = tqqqKlineMap.get(ts);
            tqqqLastKline = kline;
            qqqLastKlineWithRsi = kwr;
            lastTs = ts;
            double tqqqPrice = Double.parseDouble(kline.getClose());
            double qqqPrice = Double.parseDouble(kwr.getClose());

            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }

            // RSI > 80 时，检查 RSI 是否跌破 70
            if (rsi > 80 && !rsiAbove) {
                rsiAbove = true; // 标记 RSI 曾经大于 80
            }
            if (rsiAbove && rsi <= 70) {
                // RSI 从大于 80 跌破 70，卖出 TQQQ 并买入 QQQ
                tradeMocker.sellAll("TQQQ", tqqqPrice, ts);
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
                rsiAbove = false;  // 重置标记
            }

            // RSI < 20 时，检查 RSI 是否涨到 30
            if (rsi < 20 && !rsiBelow) {
                rsiBelow = true; // 标记 RSI 曾经小于 20
            }
            if (rsiBelow && rsi >= 30) {
                // RSI 从小于 20 升到 30，卖出 QQQ 并买入 TQQQ
                tradeMocker.sellAll("QQQ", qqqPrice, ts);
                tradeMocker.buyAll("TQQQ", tqqqPrice, ts);
                rsiBelow = false;  // 重置标记
            }
        }

        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", Double.parseDouble(qqqLastKlineWithRsi.getClose()));
        priceMap.put("TQQQ", Double.parseDouble(tqqqLastKline.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }

    public TradeDetail processC(String startStr, String endStr, Map<Long, Kline> tqqqKlineMap, Map<Long, KlineWithRSI> qqqRsiMap) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline tqqqLastKline = null;
        KlineWithRSI qqqLastKlineWithRsi = null;
        long lastTs = 0;

        // 用于跟踪之前的 RSI 值
        Double previousRsi = null;

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            KlineWithRSI kwr = qqqRsiMap.get(ts);
            if (Objects.isNull(kwr) || Objects.isNull(kwr.getRsi())) {
                // 说明今日美股不开市
                continue;
            }
            Double rsi = kwr.getRsi();
            Kline kline = tqqqKlineMap.get(ts);
            tqqqLastKline = kline;
            qqqLastKlineWithRsi = kwr;
            lastTs = ts;
            double tqqqPrice = Double.parseDouble(kline.getClose());
            double qqqPrice = Double.parseDouble(kwr.getClose());

            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }

            // 如果 RSI < 20 (极度超卖)，认为 QQQ 会回升，买入 TQQQ
            if (rsi < 20 && previousRsi != null && previousRsi >= 20) {
                tradeMocker.sellAll("QQQ", qqqPrice, ts);  // 卖出 QQQ
                tradeMocker.buyAll("TQQQ", tqqqPrice, ts); // 买入 TQQQ
            }

            // 如果 RSI > 70 (超买)，认为 QQQ 上涨到高点，卖出 TQQQ，买入 QQQ
            if (rsi > 70 && previousRsi != null && previousRsi <= 70) {
                tradeMocker.sellAll("TQQQ", tqqqPrice, ts); // 卖出 TQQQ
                tradeMocker.buyAll("QQQ", qqqPrice, ts);    // 买入 QQQ
            }

            // 更新前一个 RSI 值
            previousRsi = rsi;
        }

        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", Double.parseDouble(qqqLastKlineWithRsi.getClose()));
        priceMap.put("TQQQ", Double.parseDouble(tqqqLastKline.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }


}
