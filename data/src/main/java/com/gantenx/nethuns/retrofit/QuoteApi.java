package com.gantenx.nethuns.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface QuoteApi {

    /**
     * 获取 K 线数据（历史行情）
     *
     * @param symbol    交易对，例如 "BTCUSDT"
     * @param interval  时间间隔，例如 "1m", "1h", "1d"
     * @param startTime 起始时间（毫秒），可选
     * @param endTime   结束时间（毫秒），可选
     * @param limit     返回条目数，默认 500，最大 1500，可选
     * @return K线数据列表
     */
    @GET("api/v3/klines")
    Call<List<List<Object>>> getKlines(
            @Query("symbol") String symbol,
            @Query("interval") String interval,
            @Query("startTime") Long startTime,
            @Query("endTime") Long endTime,
            @Query("limit") Integer limit
    );
}
