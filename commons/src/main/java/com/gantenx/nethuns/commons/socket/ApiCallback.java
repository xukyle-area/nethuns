package com.gantenx.nethuns.commons.socket;

@FunctionalInterface
public interface ApiCallback {
    void onResponse(String text);
}
