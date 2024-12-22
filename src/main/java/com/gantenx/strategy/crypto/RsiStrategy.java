package com.gantenx.strategy.crypto;

import com.gantenx.calculator.AssetCalculator;
import com.gantenx.chart.crypto.RSIAndAssetChart;
import com.gantenx.constant.Period;
import com.gantenx.constant.Symbol;
import com.gantenx.engine.Order;
import com.gantenx.service.KlineService;
import com.gantenx.strategy.BaseStrategy;
import com.gantenx.utils.CollectionUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.constant.Constants.*;

@Slf4j
public class RsiStrategy extends BaseStrategy {
    protected final Map<Symbol, Map<Long, Double>> rsiMap;

    public RsiStrategy(List<Symbol> symbolList, Period period, long start, long end) {
        super(RsiStrategy.class.getSimpleName(), symbolList, period, DateUtils.genTimeList(period, start, end));
        rsiMap = KlineService.genRsiMap(klineMap, symbolList);
    }

    @Override
    protected void open() {
        while (tradeEngine.hasNextDay()) {
            long timestamp = tradeEngine.nextDay();
            String dateStr = DateUtils.getDate(timestamp);
            for (Symbol symbol : klineMap.keySet()) {
                Double RSI = CollectionUtils.get(rsiMap, symbol, timestamp);
                if (Objects.isNull(RSI)) {
                    log.error("data not found, date:{}", dateStr);
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

    public int lowestOfDays(Symbol symbol, long timestamp) {
        Double curRsi = CollectionUtils.get(rsiMap, symbol, timestamp);
        if (curRsi == null) {
            throw new IllegalArgumentException("No RSI data for the given timestamp: " + timestamp);
        }

        int days = 0;
        while (true) {
            long previousTimestamp = timestamp - (++days) * Period.ONE_DAY.getMillisecond();
            Double previousRsi = CollectionUtils.get(rsiMap, symbol, previousTimestamp);
            if (previousRsi == null || curRsi >= previousRsi) {
                return days - 1;
            }
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        List<Order> orders = tradeDetail.getOrders();
        Map<Long, Double> assetMap = AssetCalculator.calculateAssetMap(klineMap, openDayList, orders, INITIAL_BALANCE);
        return new RSIAndAssetChart(klineMap.get(CRYPTO_TRADING), assetMap, orders).getCombinedChart();
    }
}
