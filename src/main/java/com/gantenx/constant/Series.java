package com.gantenx.constant;

import java.util.HashMap;
import java.util.Map;

public enum Series {
    QQQ, TQQQ, SQQQ, BTC, ETH, DOGE, PEPE, SOL, ASSET, RSI, SUPERTREND, MACD, MACD_DETAIL, HISTOGRAM;

    private static final Map<Symbol, Series> SYMBOL_TO_SERIES_MAP = new HashMap<>();

    static {
        SYMBOL_TO_SERIES_MAP.put(Symbol.QQQUSD, QQQ);
        SYMBOL_TO_SERIES_MAP.put(Symbol.TQQQUSD, TQQQ);
        SYMBOL_TO_SERIES_MAP.put(Symbol.SQQQUSD, SQQQ);
        SYMBOL_TO_SERIES_MAP.put(Symbol.BTCUSDT, BTC);
        SYMBOL_TO_SERIES_MAP.put(Symbol.ETHUSDT, ETH);
        SYMBOL_TO_SERIES_MAP.put(Symbol.DOGEUSDT, DOGE);
        SYMBOL_TO_SERIES_MAP.put(Symbol.PEPEUSDT, PEPE);
        SYMBOL_TO_SERIES_MAP.put(Symbol.SOLUSDT, SOL);
    }

    public static Series getSeries(Symbol symbol) {
        return SYMBOL_TO_SERIES_MAP.getOrDefault(symbol, null);
    }
}
