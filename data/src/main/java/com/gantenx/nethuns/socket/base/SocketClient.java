package com.gantenx.nethuns.socket.base;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public abstract class SocketClient extends WebSocketClient {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected final ApiCallback callback;
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SocketClient(String serverUri, ApiCallback callback) throws URISyntaxException {
        super(new URI(serverUri));
        this.callback = callback;
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

    @Override
    public void onMessage(String message) {
        this.callback.onResponse(message);
    }
}
