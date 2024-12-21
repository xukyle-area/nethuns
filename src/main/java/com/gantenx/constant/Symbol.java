package com.gantenx.constant;

public enum Symbol {
    QQQ("data/QQQ.csv"),
    TQQQ("data/TQQQ.csv"),
    SQQQ("data/SQQQ.csv");

    private final String resources;

    Symbol(String resources) {
        this.resources = resources;
    }

    public String getResources() {
        return resources;
    }

}
