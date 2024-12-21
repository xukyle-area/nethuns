package com.gantenx.constant;

import static com.gantenx.constant.Currency.*;
import static com.gantenx.constant.From.BINANCE;
import static com.gantenx.constant.From.CSV;

public enum Symbol {
    QQQUSD(QQQ, USD, CSV, "data/QQQ.csv"),
    TQQQUSD(TQQQ, USD, CSV, "data/TQQQ.csv"),
    SQQQUSD(SQQQ, USD, CSV, "data/SQQQ.csv"),
    BTCUSDT(BTC, USDT, BINANCE, null),
    ETHUSDT(ETH, USDT, BINANCE, null),
    DOGEUSDT(DOGE, USDT, BINANCE, null);

    private final Currency base;
    private final Currency quote;
    private final From from;
    private final String path;

    Symbol(Currency base, Currency quote, From from, String path) {
        this.base = base;
        this.quote = quote;
        this.from = from;
        if (from.equals(BINANCE)) {
            this.path = this.getBinanceSymbol();
        } else {
            this.path = path;
        }
    }

    public Currency getBase() {
        return base;
    }

    public From getFrom() {
        return from;
    }

    public Currency getQuote() {
        return quote;
    }

    private String getBinanceSymbol() {
        return base.getUpperName() + quote.getUpperName();
    }

    public String getPath() {
        return path;
    }

    public static Symbol toSymbol(Currency base, Currency quote) {
        for (Symbol value : Symbol.values()) {
            if (value.getBase().equals(base) && value.getQuote().equals(quote)) {
                return value;
            }
        }
        throw new RuntimeException("symbol not found: " + base + "-" + quote);
    }
}
