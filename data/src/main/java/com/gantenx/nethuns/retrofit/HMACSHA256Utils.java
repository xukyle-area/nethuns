package com.gantenx.nethuns.retrofit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSHA256Utils {

    /**
     * 使用 HMAC-SHA256 算法生成签名
     *
     * @param data     要加密的数据
     * @param secretKey 密钥
     * @return 返回 HMAC-SHA256 签名
     */
    public static String generateSignature(String data, String secretKey) {
        try {
            // 使用 HMAC-SHA256 算法
            Mac mac = Mac.getInstance("HmacSHA256");

            // 使用 UTF-8 编码
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);

            // 计算 HMAC
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : rawHmac) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败", e);
        }
    }
}
