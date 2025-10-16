package com.gantenx.nethuns.source.binance.restful;

public interface SignatureGenerator {
    String getSignature(String payload, long timestamp);
}
