package com.gantenx.service;

import com.gantenx.converter.DataConverter;
import com.gantenx.model.Kline;
import com.gantenx.retrofit.QuoteApi;
import com.gantenx.retrofit.RetrofitClient;
import com.gantenx.retrofit.RetrofitUtils;
import com.gantenx.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class BinanceQuoteService {
    private final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);

    public List<Kline> getKline(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        Call<List<List<Object>>> call = quoteApi.getKlines(symbol, interval, startTime, endTime, limit);
        CompletableFuture<List<Kline>> future = RetrofitUtils.enqueueRequest(call, DataConverter::convertToKlineModels, log::error);
        return FutureUtils.get(future);
    }
}
