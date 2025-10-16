package com.gantenx.nethuns.source.binance.socket;


import java.util.Collections;
import com.gantenx.nethuns.commons.base.SocketTask;
import com.gantenx.nethuns.commons.constant.Symbol;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketMain {

    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";

    public static void main(String[] args) {
        SocketTask.startSocketJob(BINANCE_URL, Callback::klineCallback, Collections.singleton(Symbol.BTCUSDT),
                Subscriber::subscribeKline);
    }
}
