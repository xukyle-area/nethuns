package com.gantenx.constant;

public enum Symbol {
    QQQ("data/QQQ.csv"), TQQQ("data/TQQQ.csv"), SQQQ("data/SQQQ.csv");

    private final String path;

    Symbol(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
