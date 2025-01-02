package com.gantenx.nethuns.socket;

@FunctionalInterface
public interface ApiCallback {
    void onResponse(String text);
}
