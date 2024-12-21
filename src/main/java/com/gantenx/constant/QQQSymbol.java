package com.gantenx.constant;

public enum QQQSymbol {
    QQQ("data/QQQ.csv"),
    TQQQ("data/TQQQ.csv"),
    SQQQ("data/SQQQ.csv");

    private final String resources;

    QQQSymbol(String resources) {
        this.resources = resources;
    }

    public String getResources() {
        return resources;
    }

}
