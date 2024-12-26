package com.gantenx.nethuns.service;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.converter.DataConverter;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.utils.FutureUtils;
import com.gantenx.nethuns.retrofit.QuoteApi;
import com.gantenx.nethuns.retrofit.RetrofitClient;
import com.gantenx.nethuns.utils.RetrofitUtils;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class BinanceService {
    private static final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);
    private static final int MAX_DAY_LIMIT = 1500;

    public static List<Kline> getKline(String symbol, Period period, long startTime, long endTime) {

        if (DateUtils.getDaysBetween(startTime, endTime) > MAX_DAY_LIMIT) {
            throw new RuntimeException("Kline time too long!");
        }
        Call<List<List<Object>>> call = quoteApi.getKlines(symbol, period.getDesc(), startTime, endTime, MAX_DAY_LIMIT);
        CompletableFuture<List<Kline>> future = RetrofitUtils.enqueueRequest(call,
                                                                             DataConverter::convertToKlineModels,
                                                                             o -> {
                                                                             });
        return FutureUtils.get(future);
    }
}
