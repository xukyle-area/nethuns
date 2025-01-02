package com.gantenx.nethuns.retrofit;

public interface SignatureGenerator {
    String getSignature(String payload, long timestamp);
}