package com.gantenx.constant;

public enum CryptoCurrency {
    BTC("BTC", "btc"),
    ETH("ETH", "eth"),
    DOGE("DOGE", "doge"),
    USDT("USDT", "usdt");

    private final String upperName;
    private final String lowerName;

    CryptoCurrency(String upperName, String lowerName) {
        this.upperName = upperName;
        this.lowerName = lowerName;
    }

    public String getUpperName() {
        return upperName;
    }

    public String getLowerName() {
        return lowerName;
    }

    public CryptoCurrency toCurrency(String currency) {
        for (CryptoCurrency value : CryptoCurrency.values()) {
            if (value.getUpperName().equals(currency)) {
                return value;
            }
        }
        throw new RuntimeException("Currency not found: " + currency);
    }
}
