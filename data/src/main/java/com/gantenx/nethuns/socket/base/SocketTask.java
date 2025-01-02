package com.gantenx.nethuns.socket.base;

import com.gantenx.nethuns.commons.constant.Symbol;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SocketTask {
    private static final int CHECK_INITIAL_DELAY = 5;
    private static final int CHECK_FIXED_DELAY = 15;
    private static final int SUBSCRIPTION_INITIAL_DELAY = 5;
    private static final int SUBSCRIPTION_FIXED_DELAY = 30;
    private SocketClient curClient = null;

    private SocketTask() {
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
            String sub = SubscriptionUtils.ofCandleSubscription(Collections.singleton(Symbol.BTCUSDT));
            curClient.send(sub);
        }
    }

    private synchronized void checkAndReconnect() {
        if (curClient != null && curClient.isOpen()) {
            return;
        }
        SocketClient nextClient = new BinanceSocketClient(text -> log.info("{}", text));
        if (curClient != null) {
            curClient.close();
        }
        nextClient.connect();
        curClient = nextClient;
    }

    public static void startSocketJob() {
        SocketTask socketTask = new SocketTask();
        socketTask.scheduleConnect();
    }
}
