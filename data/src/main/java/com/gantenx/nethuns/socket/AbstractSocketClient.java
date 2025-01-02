package com.gantenx.nethuns.socket;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public abstract class AbstractSocketClient extends WebSocketClient {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public AbstractSocketClient(String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
    }

    @Override
    public void onOpen(ServerHandshake data) {
        log.info("WebSocket 连接已打开!");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection closed by {}, Code: {}, Reason: {}", (remote ? "remote peer" : "us"), code, reason);
        log.info("WebSocket连接已关闭, 准备重新连接...");
    }

    @Override
    public void onError(Exception ex) {
        log.error("WebSocket连接发生错误...", ex);
    }

    protected abstract ApiCallback getApiCallback();

    @Override
    public void onMessage(String message) {
        ApiCallback callback = this.getApiCallback();
        callback.onResponse(message);
    }
}
