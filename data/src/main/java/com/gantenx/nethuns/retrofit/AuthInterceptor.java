package com.gantenx.nethuns.retrofit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AuthInterceptor implements Interceptor {

    private final String apiKey;
    private final String secretKey;

    private final HmacSignatureGenerator hmacSignatureGenerator;

    public AuthInterceptor() {
        this.apiKey = "lcFnD3shoWVpfrQQElgE2IKekSq1Ahrn3oFzKLZVysYXhC0ocFyVUIR4sHIwZJQX";
        this.secretKey = "FcN8GP4sCJiH9LOeizdpst2Q01Ze9dEPF8MlFwlUbuT1sRSr8Oelpf1qRpfusalx";
        this.hmacSignatureGenerator = new HmacSignatureGenerator(this.secretKey);
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获取请求原始信息
        Request originalRequest = chain.request();

        // 获取当前时间戳
        long timestamp = System.currentTimeMillis();

        // 根据请求方法（GET/POST）处理不同的签名
        String signature;
        if (originalRequest.method().equalsIgnoreCase("GET")) {
            // 对于 GET 请求，将 timestamp 添加到 query string
            HttpUrl url = originalRequest.url().newBuilder().addQueryParameter("timestamp",
                                                                               String.valueOf(timestamp)).build();
            signature = hmacSignatureGenerator.getSignature(url.query());  // 使用 query string 生成签名

            // 构造带有签名的 GET 请求
            Request request = originalRequest.newBuilder().url(url.newBuilder().addQueryParameter("signature",
                                                                                                  signature).build()).header(
                            "X-MBX-APIKEY",
                            apiKey)  // 添加 API 密钥
                    .build();

            return chain.proceed(request);
        } else if (originalRequest.method().equalsIgnoreCase("POST")) {
            // 对于 POST 请求，将 timestamp 添加到 body 中
            String bodyString = originalRequest.body() != null ? originalRequest.body().toString() : "";
            String postData = bodyString + "&timestamp=" + timestamp;
            signature = hmacSignatureGenerator.getSignature(postData);  // 使用 body 内容生成签名

            // 构造带有签名的 POST 请求
            Request request = originalRequest.newBuilder().header("X-MBX-APIKEY", apiKey)  // 添加 API 密钥
                    .header("X-Signature", signature)  // 添加签名
                    .header("X-Timestamp", String.valueOf(timestamp))  // 添加时间戳
                    .build();

            return chain.proceed(request);
        } else {
            // 对于其他请求类型，直接通过原始请求
            return chain.proceed(originalRequest);
        }
    }
}
