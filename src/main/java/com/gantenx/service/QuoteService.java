package com.gantenx.service;

import com.gantenx.model.response.KlineModel;
import com.gantenx.retrofit2.QuoteApi;
import com.gantenx.util.DataConverter;
import com.gantenx.util.FutureUtils;
import com.gantenx.util.RetrofitClient;
import com.gantenx.util.RetrofitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class QuoteService {
    private final QuoteApi quoteService = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);

    public List<KlineModel> getKline(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        Call<List<List<Object>>> call = quoteService.getKlines(symbol, interval, startTime, endTime, limit);
        CompletableFuture<List<KlineModel>> future = RetrofitUtils.enqueueRequest(call, DataConverter::convertToKlineModels, log::error);
        return FutureUtils.get(future);
    }
}
