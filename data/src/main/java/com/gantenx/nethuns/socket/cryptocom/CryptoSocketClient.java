package com.gantenx.nethuns.socket.cryptocom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gantenx.nethuns.socket.base.SocketClient;
import com.gantenx.nethuns.socket.base.ApiCallback;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

@Slf4j
public class CryptoSocketClient extends SocketClient {

    private final static String CRYPTO_URL = "wss://stream.crypto.com/v2/market";

    public CryptoSocketClient(ApiCallback callback) throws URISyntaxException {
        super(CRYPTO_URL, callback);
    }

    @Override
    public void onMessage(String message) {
        if (message.contains("public/heartbeat")) {
            try {
                CryptoRequest request = objectMapper.readValue(message, CryptoRequest.class);
                request.setMethod("public/respond-heartbeat");
                request.setNonce(System.currentTimeMillis());
                this.send(objectMapper.writeValueAsString(request));
            } catch (JsonProcessingException e) {
                log.info("handle heartbeat exception, message:{}", message, e);
            }
        } else {
            super.onMessage(message);
        }
    }
}
