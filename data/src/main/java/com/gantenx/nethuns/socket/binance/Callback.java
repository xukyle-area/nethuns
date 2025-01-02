package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gantenx.nethuns.commons.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Callback {

    public static void tickerCallback(String text) {
        Event<Ticker> event = JsonUtils.readValue(text, new TypeReference<Event<Ticker>>() {
        });
        Ticker ticker = event.getData();
        log.info("ticker:{}", JsonUtils.toJson(ticker));
    }

    public static void klineCallback(String text) {
        log.info("text:{}", text);
    }
}
