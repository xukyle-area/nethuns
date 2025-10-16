package com.gantenx.nethuns.commons.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 配置验证工具类
 * 用于在应用启动时验证关键配置项
 */
@Slf4j
public class ConfigValidator {

    /**
     * 验证 Binance API 配置
     * @return true 如果配置完整，false 如果配置缺失
     */
    public static boolean validateBinanceConfig() {
        String apiKey = System.getenv("BINANCE_API_KEY");
        String apiSecret = System.getenv("BINANCE_API_SECRET");

        // 检查环境变量
        if (isValidCredential(apiKey) && isValidCredential(apiSecret)) {
            log.info("✅ Binance API 配置验证通过（环境变量）");
            return true;
        }

        // 检查系统属性
        apiKey = System.getProperty("binance.api.key");
        apiSecret = System.getProperty("binance.api.secret");

        if (isValidCredential(apiKey) && isValidCredential(apiSecret)) {
            log.info("✅ Binance API 配置验证通过（系统属性）");
            return true;
        }

        log.warn("⚠️  Binance API 配置未完整设置");
        log.warn("   请设置环境变量：BINANCE_API_KEY 和 BINANCE_API_SECRET");
        log.warn("   或在配置文件中设置：binance.api.key 和 binance.api.secret");
        log.warn("   如果只进行历史数据回测，可以忽略此警告");

        return false;
    }

    /**
     * 检查凭据是否有效
     */
    private static boolean isValidCredential(String credential) {
        return credential != null && !credential.trim().isEmpty() && !credential.equals("your_api_key_here")
                && !credential.equals("your_api_secret_here") && !credential.equals("demo_key")
                && !credential.equals("demo_secret");
    }

    /**
     * 验证所有关键配置
     */
    public static void validateAllConfigs() {
        log.info("=== 开始配置验证 ===");

        boolean binanceConfigValid = validateBinanceConfig();

        // 可以在这里添加更多配置验证
        validateJavaVersion();
        validateSystemProperties();

        if (!binanceConfigValid) {
            log.warn("=== 配置验证完成（存在警告）===");
            log.warn("应用程序将在受限模式下运行");
        } else {
            log.info("=== 配置验证完成（所有检查通过）===");
        }
    }

    /**
     * 验证 Java 版本
     */
    private static void validateJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        log.info("Java 版本: {}", javaVersion);

        if (javaVersion.startsWith("1.8") || javaVersion.startsWith("8")) {
            log.info("✅ Java 版本检查通过");
        } else {
            log.warn("⚠️  推荐使用 Java 8，当前版本: {}", javaVersion);
        }
    }

    /**
     * 验证系统属性
     */
    private static void validateSystemProperties() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        log.info("操作系统: {} ({})", os, arch);

        // 检查是否是无头模式（适用于服务器部署）
        String headless = System.getProperty("java.awt.headless");
        if ("true".equals(headless)) {
            log.info("✅ 无头模式已启用，适合服务器部署");
        } else {
            log.info("桌面模式运行，支持图表生成");
        }
    }
}
