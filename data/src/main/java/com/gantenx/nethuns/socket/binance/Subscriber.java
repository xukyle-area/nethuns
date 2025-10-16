package com.gantenx.nethuns.socket.binance;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.commons.utils.JsonUtils;

public class Subscriber {

    private static final String SUBSCRIBE = "SUBSCRIBE";
    private static long id = 1L;

    public static String subscribeKline(Set<Symbol> symbols) {
        List<String> params = new ArrayList<>();
        for (Symbol s : symbols) {
            String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
            String x = symbol.toUpperCase() + "@kline_" + Period.M_15.getDesc();
            params.add(x.toLowerCase());
        }
        Request request = new Request(SUBSCRIBE, params.toArray(new String[0]), id++);
        return JsonUtils.writeValueAsString(request);
    }

    public static String subscribeTicker(Set<Symbol> symbols) {
        List<String> params = new ArrayList<>();
        for (Symbol s : symbols) {
            String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
            String x = symbol.toUpperCase() + "@ticker";
            params.add(x.toLowerCase());
        }
        Request request = new Request(SUBSCRIBE, params.toArray(new String[0]), id++);
        return JsonUtils.writeValueAsString(request);
    }
}
