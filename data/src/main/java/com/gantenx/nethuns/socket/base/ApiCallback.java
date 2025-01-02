package com.gantenx.nethuns.socket.base;

@FunctionalInterface
public interface ApiCallback {
    void onResponse(String text);
}
