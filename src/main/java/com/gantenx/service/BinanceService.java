package com.gantenx.service;

import com.gantenx.constant.CryptoSymbol;
import com.gantenx.converter.DataConverter;
import com.gantenx.model.Kline;
import com.gantenx.retrofit.QuoteApi;
import com.gantenx.retrofit.RetrofitClient;
import com.gantenx.retrofit.RetrofitUtils;
import com.gantenx.utils.*;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.gantenx.constant.Constants.ONE_DAY;

@Slf4j
public class BinanceService {
    private static final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);
    private static final int MAX_DAY_LIMIT = 1500;

    public static<T> List<Kline> getKline(CryptoSymbol symbol, Long startTime, Long endTime) {
        String binanceSymbol = symbol.getBinanceSymbol();

        if (DateUtils.getDaysBetween(startTime, endTime) > MAX_DAY_LIMIT) {
            throw new RuntimeException("Kline time too long!");
        }
        Call<List<List<Object>>> call = quoteApi.getKlines(binanceSymbol, ONE_DAY, startTime, endTime, MAX_DAY_LIMIT);
        CompletableFuture<List<Kline>> future = RetrofitUtils.enqueueRequest(call, DataConverter::convertToKlineModels, log::error);
        return FutureUtils.get(future);
    }
}
