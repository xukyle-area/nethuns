package com.gantenx.nethuns.socket.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.model.BinanceRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public final class SubscriptionUtils {

    private SubscriptionUtils() {
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    private final static String BINANCE_SUBSCRIBE = "SUBSCRIBE";
    private static long id = 1L;

    public static String ofTickerSubscription(Set<Symbol> symbols) {
        try {
            List<String> params = new ArrayList<>();
            for (Symbol s : symbols) {
                String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
                String x = symbol.toUpperCase() + "@ticker";
                params.add(x.toLowerCase());
            }
            BinanceRequest request = new BinanceRequest(BINANCE_SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String ofCandleSubscription(Set<Symbol> symbols) {
        try {
            List<String> params = new ArrayList<>();
            for (Symbol s : symbols) {
                String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
                String x = symbol.toUpperCase() + "@kline_" + Period.M_15.getDesc();
                params.add(x.toLowerCase());
            }
            BinanceRequest request = new BinanceRequest(BINANCE_SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
