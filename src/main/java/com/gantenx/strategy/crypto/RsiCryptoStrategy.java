package com.gantenx.strategy.crypto;

import com.gantenx.calculator.AssetCalculator;
import com.gantenx.calculator.IndexTechnicalIndicators;
import com.gantenx.chart.crypto.RSICryptoChart;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.engine.Order;
import com.gantenx.model.Kline;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.*;
import static com.gantenx.constant.CryptoSymbol.BTC_USDT;
import static com.gantenx.utils.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public class RsiCryptoStrategy extends BaseCryptoStrategy {
    protected final Map<CryptoSymbol, Map<Long, Double>> rsiMap;

    public RsiCryptoStrategy(List<CryptoSymbol> symbols, String start, String end) {
        super(RsiCryptoStrategy.class.getSimpleName(), symbols, start, end);
        rsiMap = genRsiMap(klineMap, symbols);
    }

    public static Map<CryptoSymbol, Map<Long, Double>> genRsiMap(Map<CryptoSymbol, Map<Long, Kline>> klineMap,
                                                                 List<CryptoSymbol> symbols) {
        HashMap<CryptoSymbol, Map<Long, Double>> hashMap = new HashMap<>();
        for (CryptoSymbol symbol : symbols) {
            Map<Long, Double> map = IndexTechnicalIndicators.calculateRSI(klineMap.get(symbol), RSI_PERIOD);
            hashMap.put(symbol, map);
        }
        return hashMap;
    }

    private Double getRsi(CryptoSymbol symbol, long timestamp) {
        return rsiMap.get(symbol).get(timestamp);
    }

    @Override
    protected void openTrade() {
        while (tradeEngine.hasNextDay()) {
            long timestamp = tradeEngine.nextDay();
            for (CryptoSymbol symbol : klineMap.keySet()) {
                Double RSI = this.getRsi(symbol, timestamp);
                if (Objects.isNull(RSI)) {
                    log.error("data not found, date:{}", DateUtils.getDate(timestamp));
                    continue;
                }
                if (this.lowestOfDays(symbol, timestamp) > 5 && RSI < 50) {
                    tradeEngine.buy(symbol, PROPORTION_OF_30, "rsi:" + RSI);
                } else if (tradeEngine.getQuantity(symbol) > 0 && RSI >= 60) {
                    tradeEngine.sell(symbol, PROPORTION_OF_100, "rsi:" + RSI);
                }
            }
        }
        tradeEngine.exit();
    }

    public int lowestOfDays(CryptoSymbol symbol, long timestamp) {
        Double curRsi = this.getRsi(symbol, timestamp);
        if (curRsi == null) {
            throw new IllegalArgumentException("No RSI data for the given timestamp: " + timestamp);
        }

        int days = 0;
        long dayMillis = MS_OF_ONE_DAY;
        while (true) {
            long previousTimestamp = timestamp - (++days) * dayMillis;
            Double previousRsi = this.getRsi(symbol, previousTimestamp);
            if (previousRsi == null || curRsi >= previousRsi) {
                return days - 1;
            }
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        List<Order<CryptoSymbol>> orders = tradeDetail.getOrders();
        Map<Long, Double> assetMap = AssetCalculator.calculateAssetMap(klineMap.get(BTC_USDT), orders, initialBalance);
        return new RSICryptoChart(klineMap.get(BTC_USDT), assetMap, orders).getCombinedChart();
    }
}
