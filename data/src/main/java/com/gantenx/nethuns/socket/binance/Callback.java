package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gantenx.nethuns.commons.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Callback {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void tickerCallback(String text) {
        Event<Ticker> event;
        try {
            event = objectMapper.readValue(text, new TypeReference<Event<Ticker>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Ticker ticker = event.getData();

        log.info("ticker:{}", JsonUtils.toJson(ticker));
    }

    public static void klineCallback(String text) {
        Event<Ticker> event;
        try {
            event = objectMapper.readValue(text, new TypeReference<Event<Ticker>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Ticker ticker = event.getData();

        log.info("kline:{}", JsonUtils.toJson(ticker));
    }
}
