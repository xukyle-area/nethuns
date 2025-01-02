package com.gantenx.nethuns.socket.binance;

import com.gantenx.nethuns.socket.base.SocketClient;
import com.gantenx.nethuns.socket.base.ApiCallback;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

@Slf4j
public class BinanceSocketClient extends SocketClient {

    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";

    public BinanceSocketClient(ApiCallback callback) throws URISyntaxException {
        super(BINANCE_URL, callback);
    }
}
