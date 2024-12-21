package com.gantenx.constant;

import static com.gantenx.constant.CryptoCurrency.*;

public enum CryptoSymbol {
    BTC_USDT(BTC, USDT),
    ETH_USDT(ETH, USDT),
    DOGE_USDT(DOGE, USDT);

    private final CryptoCurrency base;
    private final CryptoCurrency quote;

    CryptoSymbol(CryptoCurrency base, CryptoCurrency quote) {
        this.base = base;
        this.quote = quote;
    }

    public CryptoCurrency getBase() {
        return base;
    }

    public CryptoCurrency getQuote() {
        return quote;
    }

    public String getBinanceSymbol() {
        return base.getUpperName() + quote.getUpperName();
    }

    public static CryptoSymbol toSymbol(CryptoCurrency base, CryptoCurrency quote) {
        for (CryptoSymbol value : CryptoSymbol.values()) {
            if (value.getBase().equals(base) && value.getQuote().equals(quote)) {
                return value;
            }
        }
        throw new RuntimeException("symbol not found: " + base + "-" + quote);
    }
}
