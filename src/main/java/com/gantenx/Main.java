package com.gantenx;

import com.gantenx.model.RSI;
import com.gantenx.service.QuoteService;
import com.gantenx.util.DateUtils;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;

public class Main {

    public static void main(String[] args) {
        QuoteService quoteService = new QuoteService();
        long begin = DateUtils.getTimestamp("20240101");
        long end = DateUtils.getTimestamp("20241201");
        List<RSI> btcusdt = quoteService.getRsi("BTCUSDT", ONE_DAY, begin, end, 500);

    }
}
