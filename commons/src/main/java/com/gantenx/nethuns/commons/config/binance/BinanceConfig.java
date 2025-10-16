package com.gantenx.nethuns.commons.config.binance;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binance API配置类
 * 用于管理API密钥等敏感信息
 */
@Component
@ConfigurationProperties(prefix = "binance.api")
public class BinanceConfig {

    private String key;
    private String secret;

    public String getKey() {
        // 优先从环境变量获取
        String envKey = System.getenv("BINANCE_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        // 优先从环境变量获取
        String envSecret = System.getenv("BINANCE_API_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * 验证API配置是否完整
     */
    public boolean isConfigured() {
        String apiKey = getKey();
        String apiSecret = getSecret();
        return apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty();
    }
}
