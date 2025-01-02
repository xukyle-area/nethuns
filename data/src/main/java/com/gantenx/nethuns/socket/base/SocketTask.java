package com.gantenx.nethuns.socket.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gantenx.nethuns.commons.constant.Market;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.binance.BinanceSocketClient;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SocketTask {
    private static final int CHECK_INITIAL_DELAY = 5;
    private static final int CHECK_FIXED_DELAY = 15;
    private static final int SUBSCRIPTION_INITIAL_DELAY = 5;
    private static final int SUBSCRIPTION_FIXED_DELAY = 30;
    private SocketClient curClient = null;
    private final Market type;

    public SocketTask() {
        this.type = Market.BINANCE;
    }

    public void scheduleConnect() {
        SocketThreadPool.scheduleWithFixedDelay(this::checkAndReconnect,
                                                CHECK_INITIAL_DELAY,
                                                CHECK_FIXED_DELAY,
                                                TimeUnit.SECONDS);
        SocketThreadPool.scheduleWithFixedDelay(this::subscribe,
                                                SUBSCRIPTION_INITIAL_DELAY,
                                                SUBSCRIPTION_FIXED_DELAY,
                                                TimeUnit.SECONDS);
    }

    private void subscribe() {
        if (curClient != null && curClient.isOpen()) {
            try {
                String sub = SubscriptionUtils.ofCandleSubscription(type, Collections.singleton(Symbol.BTCUSDT));
                curClient.send(sub);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("websocket of {} is not open", type);
        }
    }


    private void checkAndReconnect() {
        if (curClient == null || !curClient.isOpen()) {
            this.reconnect();
        }
    }

    public synchronized void reconnect() {
        SocketClient nextClient;
        try {
            log.info("try to connect websocket of {}", type);
            ApiCallback callback = text -> log.info("{}", text);
            if (type.equals(Market.BINANCE)) {
                nextClient = new BinanceSocketClient(callback);
            } else {
                log.error("Unsupported QuoteEnum type: {}", type);
                return;
            }
        } catch (URISyntaxException e) {
            log.error("URL format is invalid!", e);
            return;
        }
        log.info("build websocket client success, websocket of {}", type);
        try {
            if (curClient != null) {
                curClient.close();
            }
            nextClient.connect();
            curClient = nextClient;
        } catch (Exception e) {
            log.error("Error reconnecting: {}", e.getMessage(), e);
        }
    }
}
