package com.gantenx.nethuns.commons.constant;

public enum Signal {
    STRONG_BUY("强买入"),
    BUY("买入"),
    WAITING("观望"),
    SELL("卖出"),
    STRONG_SELL("强卖出");

    private final String description;

    Signal(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}