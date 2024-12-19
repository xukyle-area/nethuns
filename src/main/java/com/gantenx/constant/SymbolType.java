package com.gantenx.constant;

public enum SymbolType {
    QQQ("data/QQQ.csv"), TQQQ("data/TQQQ.csv"), SQQQ("data/SQQQ.csv");

    private final String path;

    SymbolType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
