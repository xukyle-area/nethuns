package com.gantenx.strategy.crypto;

import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.PROPORTION_OF_100;
import static com.gantenx.constant.Constants.RSI_PERIOD;

@Slf4j
public class RsiCryptoStrategy extends BaseCryptoStrategy {
    protected final Map<Long, Double> rsiMap;
    protected final List<Long> timestamps;

    public RsiCryptoStrategy(CryptoSymbol symbol, String start, String end) {
        super(RsiCryptoStrategy.class.getSimpleName(), symbol, start, end);
        rsiMap = IndexTechnicalIndicators.calculateRSI(klineMap, RSI_PERIOD);
        timestamps = CollectionUtils.getTimestamps(rsiMap);
    }

    @Override
    protected void openTrade() {
        for (long timestamp : timestamps) {
            Double RSI = rsiMap.get(timestamp);
            if (Objects.isNull(RSI)) {
                log.error("data not found, date:{}", DateUtils.getDate(timestamp));
                continue;
            }
            Kline kline = klineMap.get(timestamp);
            if (RSI < 25) {
                tradeEngine.buy(symbol, kline.getClose(), PROPORTION_OF_100, timestamp, "rsi:" + RSI);
            } else if (tradeEngine.hasPosition(symbol) && RSI >= 60) {
                tradeEngine.sell(symbol, kline.getClose(), PROPORTION_OF_100, timestamp, "rsi:" + RSI);
            }
        }
    }
}
