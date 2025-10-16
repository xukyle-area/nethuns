package com.gantenx.nethuns.source.binance.restful;

import java.util.List;
import com.gantenx.nethuns.converter.OrderListResponse;
import com.gantenx.nethuns.source.binance.model.AccountInfo;
import com.gantenx.nethuns.source.binance.model.OrderResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface QuoteApi {

        @GET("api/v3/klines")
        Call<List<List<Object>>> getKline(@Query("symbol") String symbol, @Query("interval") String interval,
                        @Query("startTime") Long startTime, @Query("endTime") Long endTime,
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
        Call<OrderResponse> orderTest(@Field("symbol") String symbol, @Field("side") String side,
                        @Field("type") String type);

}
