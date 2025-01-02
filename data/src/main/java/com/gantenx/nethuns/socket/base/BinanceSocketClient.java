package com.gantenx.nethuns.socket.base;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinanceSocketClient extends SocketClient {

    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";

    public BinanceSocketClient(ApiCallback callback) {
        super(BINANCE_URL, callback);
    }
}
