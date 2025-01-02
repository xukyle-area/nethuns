package com.gantenx.nethuns.retrofit;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import retrofit2.Invocation;

import java.io.IOException;

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
        this.apiKey = "lcFnD3shoWVpfrQQElgE2IKekSq1Ahrn3oFzKLZVysYXhC0ocFyVUIR4sHIwZJQX";
        String secretKey = "FcN8GP4sCJiH9LOeizdpst2Q01Ze9dEPF8MlFwlUbuT1sRSr8Oelpf1qRpfusalx";
        this.signatureGenerator = new HmacSignatureGenerator(secretKey);
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

        Request.Builder builder = request.newBuilder()
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .header(X_MBX_APIKEY, apiKey);

        // Generate signature based on request method (GET or POST)
        String signature = this.generateSignature(method, requestUrl, body, timestamp);

        // Build the URL with the timestamp and signature query parameters
        HttpUrl httpUrl = this.buildUrlWithParams(requestUrl, timestamp, signature);
        builder.url(httpUrl);

        return chain.proceed(builder.build());
    }

    private String generateSignature(String method,
                                     HttpUrl requestUrl,
                                     RequestBody body,
                                     long timestamp) throws IOException {
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
        return originalUrl.newBuilder()
                .addQueryParameter(TIMESTAMP, String.valueOf(timestamp))
                .addQueryParameter(SIGNATURE, signature)
                .build();
    }
}