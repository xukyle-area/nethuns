package com.gantenx.nethuns.commons.constant;

public enum Currency {
    BTC("BTC", "btc"),
    USD("BTC", "btc"),
    QQQ("QQQ", "qqq"),
    TQQQ("TQQQ", "tqqq"),
    SQQQ("SQQQ", "sqqq"),
    ETH("ETH", "eth"),
    DOGE("DOGE", "doge"),
    PEPE("PEPE", "pepe"),
    SOL("SOL", "sol"),
    USDT("USDT", "usdt");

    private final String upperName;
    private final String lowerName;

    Currency(String upperName, String lowerName) {
        this.upperName = upperName;
        this.lowerName = lowerName;
    }

    public String getUpperName() {
        return upperName;
    }

    public String getLowerName() {
        return lowerName;
    }

    public Currency toCurrency(String currency) {
        for (Currency value : Currency.values()) {
            if (value.getUpperName().equals(currency)) {
                return value;
            }
        }
        throw new RuntimeException("Currency not found: " + currency);
    }
}
