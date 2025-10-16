package com.gantenx.nethuns.socket.base;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import com.gantenx.nethuns.commons.constant.Symbol;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketTask {
    private static final int CHECK_INITIAL_DELAY = 5;
    private static final int CHECK_FIXED_DELAY = 15;
    private static final int SUBSCRIPTION_INITIAL_DELAY = 5;
    private static final int SUBSCRIPTION_FIXED_DELAY = 30;

    private final Consumer<String> callback;
    private final Set<Symbol> symbols;
    private final String url;

    private final Function<Set<Symbol>, String> subscriber;

    private SocketClient curClient = null;

    private SocketTask(String url, Consumer<String> callback, Set<Symbol> symbols,
            Function<Set<Symbol>, String> subscriber) {
        this.url = url;
        this.callback = callback;
        this.symbols = symbols;
        this.subscriber = subscriber;
    }

    private void subscribe() {
        if (Objects.nonNull(curClient) && curClient.isOpen()) {
            curClient.send(subscriber.apply(this.symbols));
        }
    }

    private synchronized void check() {
        if (Objects.nonNull(curClient) && curClient.isOpen()) {
            return;
        }
        SocketClient nextClient = new SocketClient(url, this.callback);
        if (curClient != null) {
            curClient.close();
        }
        nextClient.connect();
        curClient = nextClient;
    }

    private void scheduleConnect() {
        SocketThreadPool.scheduleWithFixedDelay(this::check, CHECK_INITIAL_DELAY, CHECK_FIXED_DELAY, TimeUnit.SECONDS);
        SocketThreadPool.scheduleWithFixedDelay(this::subscribe, SUBSCRIPTION_INITIAL_DELAY, SUBSCRIPTION_FIXED_DELAY,
                TimeUnit.SECONDS);
    }

    public static void startSocketJob(String url, Consumer<String> callback, Set<Symbol> symbols,
            Function<Set<Symbol>, String> function) {
        SocketTask socketTask = new SocketTask(url, callback, symbols, function);
        socketTask.scheduleConnect();
    }
}
