package com.gantenx.strategy.crypto;

import com.gantenx.calculator.AssetCalculator;
import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.crypto.RSICryptoChart;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY;

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
            if (this.lowestOfDays(timestamp) > 5 && RSI < 50) {
                tradeEngine.buy(symbol, kline.getClose(), PROPORTION_OF_30, timestamp, "rsi:" + RSI);
            } else if (tradeEngine.getQuantity(symbol) > 0 && RSI >= 60) {
                tradeEngine.sell(symbol, kline.getClose(), PROPORTION_OF_100, timestamp, "rsi:" + RSI);
            }
        }
    }

    public int lowestOfDays(long timestamp) {
        Double curRsi = rsiMap.get(timestamp);
        if (curRsi == null) {
            throw new IllegalArgumentException("No RSI data for the given timestamp: " + timestamp);
        }

        int days = 0;
        long dayMillis = MS_OF_ONE_DAY;
        while (true) {
            long previousTimestamp = timestamp - (++days) * dayMillis;
            Double previousRsi = rsiMap.get(previousTimestamp);
            if (previousRsi == null || curRsi >= previousRsi) {
                return days - 1;
            }
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        List<Order<CryptoSymbol>> orders = tradeDetail.getOrders();
        Map<Long, Double> assetMap = AssetCalculator.calculateAssetMap(klineMap, orders, initialBalance);
        return new RSICryptoChart(klineMap, assetMap, orders).getCombinedChart();
    }
}
