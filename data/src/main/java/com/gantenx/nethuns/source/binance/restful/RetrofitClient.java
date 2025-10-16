package com.gantenx.nethuns.source.binance.restful;

import static com.gantenx.nethuns.commons.constant.Constants.BINANCE_URL;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClient {

    private static Retrofit retrofit;
    private static OkHttpClient okHttpClient;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            okHttpClient = new OkHttpClient.Builder().addInterceptor(new AuthInterceptor()).build();

            retrofit = new Retrofit.Builder().baseUrl(BINANCE_URL).addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient).build();
        }
        return retrofit;
    }

    // 新增方法：关闭 OkHttp 的连接池
    public static void shutdown() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
        }
    }
}
