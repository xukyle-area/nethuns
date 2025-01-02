package com.gantenx.nethuns.retrofit;


import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;


public final class HmacSignatureGenerator implements SignatureGenerator {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String TIMESTAMP = "timestamp";
    private String apiSecret;

    public HmacSignatureGenerator(String apiSecret) {
        ParameterChecker.checkParameterType(apiSecret, String.class, "apiSecret");
        this.apiSecret = apiSecret;
    }

    /**
     * @param data 格式为 a=b&c=d&e=f&g=h
     */
    public String getSignature(String data, long timestamp) {
        if (Objects.nonNull(data) && !data.isEmpty()) {
            data = data + "&";
        } else {
            data = "";
        }
        data = data + TIMESTAMP + "=" + timestamp;
        byte[] hmacSha256;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(), HMAC_SHA256);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return Hex.encodeHexString(hmacSha256);
    }
}