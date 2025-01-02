package com.gantenx.nethuns.socket;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.constant.Period;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.base.SocketTask;
import com.gantenx.nethuns.socket.model.BinanceRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
public class SocketMain {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String SUBSCRIBE = "SUBSCRIBE";
    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";
    private static long id = 1L;
    public static void main(String[] args) {
        SocketTask.startSocketJob(BINANCE_URL,
                                  text -> log.info("{}", text),
                                  Collections.singleton(Symbol.BTCUSDT),
                                  SocketMain::buildSubscription);
    }

    private static String buildSubscription(Set<Symbol> symbols) {
        try {
            List<String> params = new ArrayList<>();
            for (Symbol s : symbols) {
                String symbol = s.getBase().getLowerName() + s.getQuote().getLowerName();
                String x = symbol.toUpperCase() + "@kline_" + Period.M_15.getDesc();
                params.add(x.toLowerCase());
            }
            BinanceRequest request = new BinanceRequest(SUBSCRIBE, params.toArray(new String[0]), id++);
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
