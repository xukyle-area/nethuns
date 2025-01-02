package com.gantenx.nethuns.socket.binance;

import com.gantenx.nethuns.commons.utils.JsonUtils;
import com.gantenx.nethuns.socket.AbstractSocketClient;
import com.gantenx.nethuns.socket.ApiCallback;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

@Slf4j
public class BinanceSocketClient extends AbstractSocketClient {

    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream";

    public BinanceSocketClient() throws URISyntaxException {
        super(BINANCE_URL);
    }

    @Override
    protected ApiCallback getApiCallback() {
        return text -> {
            try {
                BinanceEvent binanceEvent = objectMapper.readValue(text, BinanceEvent.class);
                BinanceTicker data = binanceEvent.getData();
                String symbol = data.getSymbol();
                log.info("{},{}", symbol, JsonUtils.toJson(data));
            } catch (Exception e) {
                log.error("error during sink.{}", text, e);
            }
        };
    }
}
