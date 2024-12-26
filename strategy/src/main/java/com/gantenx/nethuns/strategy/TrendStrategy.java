package com.gantenx.nethuns.strategy;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Signal;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.constant.Trend;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.trend.PriceTrendIdentifier;
import com.gantenx.nethuns.trend.TrendUtils;
import com.gantenx.nethuns.strategy.template.SingleStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.nethuns.commons.constant.Constants.CRYPTO_TRADING;
import static com.gantenx.nethuns.commons.constant.Constants.INITIAL_BALANCE;

@Slf4j
public class TrendStrategy extends SingleStrategy {
    protected final Map<Symbol, Map<Long, Trend>> trendMap;
    protected final PriceTrendIdentifier identifier = new PriceTrendIdentifier();
    protected final LinkedList<Trend> trendList = new LinkedList<>();
    private static final int TREND_LIST_SIZE = 7; // 与period相同

    public TrendStrategy(Period period, long start, long end, Symbol symbol) {
        super(period, start, end, symbol);
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
                tradeEngine.buyAmount(symbol, INITIAL_BALANCE / 4, "强力下降转弱势买入");
                break;
            case STRONG_SELL:
                tradeEngine.sellAmount(symbol, INITIAL_BALANCE / 4, "强力上升转弱势卖出");
                break;
            default:
                break;
        }
    }
}
