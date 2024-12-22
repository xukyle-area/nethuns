package com.gantenx.strategy.crypto;

import com.gantenx.calculator.AssetCalculator;
import com.gantenx.chart.crypto.RSIAndAssetChart;
import com.gantenx.constant.Period;
import com.gantenx.constant.Signal;
import com.gantenx.constant.Symbol;
import com.gantenx.constant.Trend;
import com.gantenx.engine.Order;
import com.gantenx.strategy.BaseStrategy;
import com.gantenx.trend.ComprehensiveTrendIdentifier;
import com.gantenx.trend.PriceTrendIdentifier;
import com.gantenx.trend.TrendIdentifier;
import com.gantenx.trend.TrendUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.JFreeChart;

import java.util.*;

import static com.gantenx.constant.Constants.*;

@Slf4j
public class TrendStrategy extends BaseStrategy {
    protected final Map<Symbol, Map<Long, Trend>> trendMap;
    protected final PriceTrendIdentifier identifier = new PriceTrendIdentifier();
    protected final LinkedList<Trend> trendList = new LinkedList<>();
    private static final int TREND_LIST_SIZE = 7; // 与period相同

    public TrendStrategy(List<Symbol> symbolList, Period period, long start, long end) {
        super(TrendStrategy.class.getSimpleName(), symbolList, period, DateUtils.genTimeList(period, start, end));
        trendMap = new HashMap<>();
        for (int i = 0; i < TREND_LIST_SIZE; i++) {
            trendList.add(Trend.SIDEWAYS);
        }
    }

    @Override
    protected void open() {
        Map<Long, Trend> longTrendMap = identifier.identify(klineMap.get(CRYPTO_TRADING), openDayList, 3);
        while (tradeEngine.hasNextDay()) {
            long timestamp = tradeEngine.nextDay();
            String dateStr = DateUtils.getDate(timestamp);

            for (Symbol symbol : klineMap.keySet()) {
                Trend currentTrend = longTrendMap.get(timestamp);
                if (Objects.isNull(currentTrend)) {
                    continue;
                }
                if (trendList.size() == TREND_LIST_SIZE) {
                    trendList.removeFirst();
                }
                trendList.addLast(currentTrend);
                Signal signal = this.getSignal();

                log.info("date:{}, symbol:{}, trend:{}, signal:{}",
                         dateStr,
                         symbol.name(),
                         currentTrend.getDescription(),
                         signal.getDescription());
                this.executeTradeSignal(symbol, signal);
            }
        }
        tradeEngine.exit();
    }

    public Signal getSignal() {
        if (TrendUtils.isStrongBuy(trendList)) {
            return Signal.STRONG_BUY;
        }
        if (TrendUtils.isStrongSell(trendList)) {
            return Signal.STRONG_SELL;
        }
        return Signal.WAITING;
    }

    private void executeTradeSignal(Symbol symbol, Signal signal) {
        switch (signal) {
            case STRONG_BUY:
                tradeEngine.buy(symbol, PROPORTION_OF_15, "强力下降转弱势买入");
                break;
            case STRONG_SELL:
                tradeEngine.sell(symbol, PROPORTION_OF_15, "强力上升转弱势卖出");
                break;
            default:
                break;
        }
    }

    @Override
    protected JFreeChart getTradingChart() {
        List<Order> orders = tradeDetail.getOrders();
        Map<Long, Double> assetMap = AssetCalculator.calculateAssetMap(klineMap, openDayList, orders, INITIAL_BALANCE);
        return new RSIAndAssetChart(klineMap.get(CRYPTO_TRADING), assetMap, orders).getCombinedChart();
    }
}
