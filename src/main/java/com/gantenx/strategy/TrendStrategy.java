package com.gantenx.strategy;

import com.gantenx.constant.Period;
import com.gantenx.constant.Signal;
import com.gantenx.constant.Symbol;
import com.gantenx.constant.Trend;
import com.gantenx.strategy.template.BaseStrategy;
import com.gantenx.strategy.template.SingleStrategy;
import com.gantenx.trend.PriceTrendIdentifier;
import com.gantenx.trend.TrendUtils;
import com.gantenx.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.gantenx.constant.Constants.*;

@Slf4j
public class TrendStrategy extends SingleStrategy {
    protected final Map<Symbol, Map<Long, Trend>> trendMap;
    protected final PriceTrendIdentifier identifier = new PriceTrendIdentifier();
    protected final LinkedList<Trend> trendList = new LinkedList<>();
    private static final int TREND_LIST_SIZE = 7; // 与period相同

    public TrendStrategy(Period period, long start, long end, Symbol symbol) {
        super(TrendStrategy.class.getSimpleName(), period, start, end, symbol);
        trendMap = new HashMap<>();
        for (int i = 0; i < TREND_LIST_SIZE; i++) {
            trendList.add(Trend.SIDEWAYS);
        }
    }

    @Override
    protected void open() {
        Map<Long, Trend> longTrendMap = identifier.identify(klineMap.get(CRYPTO_TRADING), timestampList, 3);
        while (tradeEngine.hasNext()) {
            long timestamp = tradeEngine.next();
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
}
