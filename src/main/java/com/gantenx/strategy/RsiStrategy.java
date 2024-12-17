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

    public TradeDetail processForQQQ(String startStr, String endStr, Map<Long, Kline> tqqqKlineMap, Map<Long, KlineWithRSI> qqqRsiMap) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline tqqqLastKline = null;
        KlineWithRSI qqqLastKlineWithRsi = null;
        long lastTs = 0;

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            KlineWithRSI kwr = qqqRsiMap.get(ts);
            if (Objects.isNull(kwr) || Objects.isNull(kwr.getRsi())) {
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
            if (rsi > 80) {
                tradeMocker.sellAll("TQQQ", tqqqPrice, ts);
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达低点，抛售 QQQ，进行短期期持有 TQQQ
            if (rsi < 20) {
                tradeMocker.sellAll("QQQ", qqqPrice, ts);
                tradeMocker.buyAll("TQQQ", tqqqPrice, ts);
            }
        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", Double.parseDouble(qqqLastKlineWithRsi.getClose()));
        priceMap.put("TQQQ", Double.parseDouble(tqqqLastKline.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }
}
