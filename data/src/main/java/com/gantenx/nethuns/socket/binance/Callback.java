package com.gantenx.nethuns.socket.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gantenx.nethuns.commons.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Callback {

    public static void tickerCallback(String text) {
        Event<TickerEvent> event = JsonUtils.readValue(text, new TypeReference<Event<TickerEvent>>() {});
        TickerEvent tickerEvent = event.getData();
        log.info("ticker:{}", JsonUtils.toJson(tickerEvent));
    }

    public static void klineCallback(String text) {
        Event<KlineEvent> event = JsonUtils.readValue(text, new TypeReference<Event<KlineEvent>>() {});
        KlineEvent klineEvent = event.getData();
        log.info("kline:{}", JsonUtils.toJson(klineEvent.getKline()));
    }
}
