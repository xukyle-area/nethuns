package com.gantenx.nethuns.retrofit;

import com.gantenx.nethuns.converter.OrderListResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface QuoteApi {

    @GET("api/v3/klines")
    Call<List<List<Object>>> getKline(
            @Query("symbol") String symbol,
            @Query("interval") String interval,
            @Query("startTime") Long startTime,
            @Query("endTime") Long endTime,
            @Query("limit") Integer limit);

    @AuthRequired
    @GET("/api/v3/allOrderList")
    Call<List<OrderListResponse>> getOrder();

    @AuthRequired
    @GET("/api/v3/account")
    Call<AccountInfo> getAccount();

    @AuthRequired
    @FormUrlEncoded
    @POST("/api/v3/order/test")
    Call<OrderResponse> orderTest(@Field("symbol") String symbol,
                                  @Field("side") String side,
                                  @Field("type") String type);

}
