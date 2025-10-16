package com.gantenx.nethuns.commons.enums;

public enum Trend {
    STRONG_UPTREND("强力上升"),
    UPTREND("上升"),
    SIDEWAYS("震荡"),
    DOWNTREND("下降"),
    STRONG_DOWNTREND("强力下降");

    private final String description;

    Trend(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}