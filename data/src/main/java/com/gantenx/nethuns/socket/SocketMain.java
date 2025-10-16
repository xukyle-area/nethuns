package com.gantenx.nethuns.socket;


import java.util.Collections;
import com.gantenx.nethuns.commons.constant.Symbol;
import com.gantenx.nethuns.socket.base.SocketTask;
import com.gantenx.nethuns.socket.binance.Callback;
import com.gantenx.nethuns.socket.binance.Subscriber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketMain {

    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";

    public static void main(String[] args) {
        SocketTask.startSocketJob(BINANCE_URL, Callback::klineCallback, Collections.singleton(Symbol.BTCUSDT),
                Subscriber::subscribeKline);
    }
}
