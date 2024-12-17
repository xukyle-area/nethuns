package com.gantenx.service;

import com.gantenx.model.RSI;
import com.gantenx.model.response.DataConverter;
import com.gantenx.model.response.KlineModel;
import com.gantenx.retrofit.QuoteApi;
import com.gantenx.retrofit.RetrofitClient;
import com.gantenx.retrofit.RetrofitUtils;
import com.gantenx.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.gantenx.constant.Constants.ONE_DAY;

@Slf4j
@Service
public class QuoteService {
    private final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);

    public List<KlineModel> getKline(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        Call<List<List<Object>>> call = quoteApi.getKlines(symbol, interval, startTime, endTime, limit);
        CompletableFuture<List<KlineModel>> future = RetrofitUtils.enqueueRequest(call, DataConverter::convertToKlineModels, log::error);
        return FutureUtils.get(future);
    }

    public List<RSI> getRsi(String symbol, Long startTime, Long endTime) {
        Call<List<List<Object>>> call = quoteApi.getKlines(symbol, ONE_DAY, startTime, endTime, 1500);
        CompletableFuture<List<KlineModel>> future = RetrofitUtils.enqueueRequest(call, DataConverter::convertToKlineModels, log::error);
        List<KlineModel> klineModels = FutureUtils.get(future);
        return RsiCalculator.calculateAndAttachRSI(klineModels, 6);
    }

    public List<RSI> getRsiList(String symbol, String begin, String end) {
        return getRsi(symbol, DateUtils.getTimestamp(begin), DateUtils.getTimestamp(end));
    }
}
