package com.gantenx.nethuns.service;

import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.model.Kline;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.converter.Converter;
import com.gantenx.nethuns.converter.OrderListResponse;
import com.gantenx.nethuns.retrofit.*;
import com.gantenx.nethuns.utils.FutureUtils;
import com.gantenx.nethuns.utils.RetrofitUtils;
import retrofit2.Call;

import java.util.List;

import static com.gantenx.nethuns.utils.FutureUtils.NO_OP_CONSUMER;


public class BinanceService {

    private static final int MAX_DAY_LIMIT = 1500;

    private static final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);

    public static List<Kline> getKline(String symbol, Period period, long startTime, long endTime) {
        if (DateUtils.getDaysBetween(startTime, endTime) > MAX_DAY_LIMIT) {
            throw new RuntimeException("Kline time too long!");
        }
        Call<List<List<Object>>> call = quoteApi.getKline(symbol, period.getDesc(), startTime, endTime, MAX_DAY_LIMIT);
        return FutureUtils.get(RetrofitUtils.enqueueRequest(call, Converter::kline, NO_OP_CONSUMER));
    }

    public static List<OrderListResponse> getOrderList() {
        return FutureUtils.get(quoteApi.getOrder());
    }

    public static OrderResponse orderTest() {
        return FutureUtils.get(quoteApi.orderTest("BTCUSDT", "BUY", "MARKET"));
    }

    public static AccountInfo getAccountInfo() {
        return FutureUtils.get(quoteApi.getAccount());
    }
}
