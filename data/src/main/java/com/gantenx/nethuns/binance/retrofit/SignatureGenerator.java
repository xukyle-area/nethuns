package com.gantenx.nethuns.binance.retrofit;

public interface SignatureGenerator {
    String getSignature(String payload, long timestamp);
}
