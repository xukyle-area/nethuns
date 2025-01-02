package com.gantenx.nethuns.service;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.converter.Converter;
import com.gantenx.nethuns.converter.OrderListResponse;
import com.gantenx.nethuns.retrofit.QuoteApi;
import com.gantenx.nethuns.retrofit.RetrofitClient;
import com.gantenx.nethuns.utils.FutureUtils;
import com.gantenx.nethuns.utils.RetrofitUtils;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class BinanceService {
    private static final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance(
            "lcFnD3shoWVpfrQQElgE2IKekSq1Ahrn3oFzKLZVysYXhC0ocFyVUIR4sHIwZJQX",
            "FcN8GP4sCJiH9LOeizdpst2Q01Ze9dEPF8MlFwlUbuT1sRSr8Oelpf1qRpfusalx").create(QuoteApi.class);
    private static final int MAX_DAY_LIMIT = 1500;

    public static List<Kline> getKline(String symbol, Period period, long startTime, long endTime) {

        if (DateUtils.getDaysBetween(startTime, endTime) > MAX_DAY_LIMIT) {
            throw new RuntimeException("Kline time too long!");
        }
        Call<List<List<Object>>> call = quoteApi.getKlines(symbol, period.getDesc(), startTime, endTime, MAX_DAY_LIMIT);
        CompletableFuture<List<Kline>> future = RetrofitUtils.enqueueRequest(call, Converter::kline, o -> {
                                                                             });
        return FutureUtils.get(future);
    }

    public static List<OrderListResponse> getOrderList() {
        Call<List<OrderListResponse>> apiOrders = quoteApi.getOrders();

        CompletableFuture<List<OrderListResponse>> future = RetrofitUtils.enqueueRequest(apiOrders,
                                                                                         Converter::nonOperation,
                                                                                         o -> {
                                                                                         });
        return FutureUtils.get(future);
    }
}
