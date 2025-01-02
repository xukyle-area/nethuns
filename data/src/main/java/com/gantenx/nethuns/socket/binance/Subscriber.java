package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Subscriber {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String SUBSCRIBE = "SUBSCRIBE";
    private static long id = 1L;

    public static String subscribeKline(Set<Symbol> symbols) {
        try {
            List<String> params = new ArrayList<>();
            for (Symbol s : symbols) {
                String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
                String x = symbol.toUpperCase() + "@kline_" + Period.M_15.getDesc();
                params.add(x.toLowerCase());
            }
            Request request = new Request(SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String subscribeTicker(Set<Symbol> symbols) {
        try {
            List<String> params = new ArrayList<>();
            for (Symbol s : symbols) {
                String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
                String x = symbol.toUpperCase() + "@ticker";
                params.add(x.toLowerCase());
            }
            Request request = new Request(SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
