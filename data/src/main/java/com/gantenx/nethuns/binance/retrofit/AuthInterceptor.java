package com.gantenx.nethuns.binance.retrofit;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import retrofit2.Invocation;

@Slf4j
public class AuthInterceptor implements Interceptor {

    private static final String TIMESTAMP = "timestamp";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String SIGNATURE = "signature";
    private static final String X_MBX_APIKEY = "X-MBX-APIKEY";
    private static final String APPLICATION_JSON = "application/json";
    private static final String GET = "GET";
    private static final String POST = "POST";

    private final String apiKey;
    private final HmacSignatureGenerator signatureGenerator;

    public AuthInterceptor() {
        // 从环境变量或配置文件读取API密钥
        this.apiKey = getApiKey();
        String secretKey = getApiSecret();

        if (apiKey == null || apiKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            log.warn("Binance API credentials not configured. API calls requiring authentication will fail.");
            log.info(
                    "Please set BINANCE_API_KEY and BINANCE_API_SECRET environment variables or configure them in application.properties");
        }

        this.signatureGenerator = new HmacSignatureGenerator(secretKey != null ? secretKey : "");
    }

    private String getApiKey() {
        // 优先从环境变量获取
        String envKey = System.getenv("BINANCE_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return envKey;
        }

        // 从系统属性获取（适用于配置文件注入）
        String propKey = System.getProperty("binance.api.key");
        if (propKey != null && !propKey.isEmpty()) {
            return propKey;
        }

        return null;
    }

    private String getApiSecret() {
        // 优先从环境变量获取
        String envSecret = System.getenv("BINANCE_API_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            return envSecret;
        }

        // 从系统属性获取（适用于配置文件注入）
        String propSecret = System.getProperty("binance.api.secret");
        if (propSecret != null && !propSecret.isEmpty()) {
            return propSecret;
        }

        return null;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Invocation invocation = request.tag(Invocation.class);

        // If no AuthRequired annotation or method is not marked, proceed without modifying
        if (invocation == null || !invocation.method().isAnnotationPresent(AuthRequired.class)) {
            return chain.proceed(request);
        }

        long timestamp = System.currentTimeMillis();
        String method = request.method();
        HttpUrl requestUrl = request.url();
        RequestBody body = request.body();

        Request.Builder builder =
                request.newBuilder().header(CONTENT_TYPE, APPLICATION_JSON).header(X_MBX_APIKEY, apiKey);

        // Generate signature based on request method (GET or POST)
        String signature = this.generateSignature(method, requestUrl, body, timestamp);

        // Build the URL with the timestamp and signature query parameters
        HttpUrl httpUrl = this.buildUrlWithParams(requestUrl, timestamp, signature);
        builder.url(httpUrl);

        return chain.proceed(builder.build());
    }

    private String generateSignature(String method, HttpUrl requestUrl, RequestBody body, long timestamp)
            throws IOException {
        if (method.equalsIgnoreCase(GET)) {
            // For GET requests, use the query part of the URL to generate the signature
            return signatureGenerator.getSignature(requestUrl.query(), timestamp);
        } else if (method.equalsIgnoreCase(POST) && body != null) {
            // For POST requests, read the body and generate the signature
            Buffer buffer = new Buffer();
            body.writeTo(buffer);
            String requestBody = buffer.readUtf8();
            return signatureGenerator.getSignature(requestBody, timestamp);
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    private HttpUrl buildUrlWithParams(HttpUrl originalUrl, long timestamp, String signature) {
        // Add the timestamp and signature as query parameters to the URL
        return originalUrl.newBuilder().addQueryParameter(TIMESTAMP, String.valueOf(timestamp))
                .addQueryParameter(SIGNATURE, signature).build();
    }
}
