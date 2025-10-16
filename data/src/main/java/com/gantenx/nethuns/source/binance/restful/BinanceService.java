package com.gantenx.nethuns.source.binance.restful;

import static com.gantenx.nethuns.utils.FutureUtils.NO_OP_CONSUMER;
import java.util.List;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.model.Candle;
import com.gantenx.nethuns.commons.utils.DateUtils;
import com.gantenx.nethuns.converter.CandleConverter;
import com.gantenx.nethuns.converter.OrderListResponse;
import com.gantenx.nethuns.source.binance.model.AccountInfo;
import com.gantenx.nethuns.source.binance.model.OrderResponse;
import com.gantenx.nethuns.utils.FutureUtils;
import com.gantenx.nethuns.utils.RetrofitUtils;
import retrofit2.Call;


public class BinanceService {

    private static final int MAX_DAY_LIMIT = 1500;

    private static final QuoteApi quoteApi = RetrofitClient.getRetrofitInstance().create(QuoteApi.class);

    public static List<Candle> getKline(String symbol, Period period, long startTime, long endTime) {
        if (DateUtils.getDaysBetween(startTime, endTime) > MAX_DAY_LIMIT) {
            throw new RuntimeException("Kline time too long!");
        }
        Call<List<List<Object>>> call = quoteApi.getKline(symbol, period.getDesc(), startTime, endTime, MAX_DAY_LIMIT);
        return FutureUtils.get(RetrofitUtils.enqueueRequest(call, CandleConverter::kline, NO_OP_CONSUMER));
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
