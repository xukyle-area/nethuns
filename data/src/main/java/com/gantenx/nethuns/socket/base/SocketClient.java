package com.gantenx.nethuns.socket.base;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class SocketClient extends WebSocketClient {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected final ApiCallback callback;
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public SocketClient(String serverUri, ApiCallback callback) {
        super(SocketClient.getURI(serverUri));
        this.callback = callback;
    }

    @Override
    public void onOpen(ServerHandshake data) {
        log.info("WebSocket connection opened!");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Connection closed by {}, Code: {}, Reason: {}", (remote ? "remote peer" : "us"), code, reason);
        log.info("WebSocket connection closed, preparing to reconnect...");
    }

    @Override
    public void onError(Exception ex) {
        log.error("Error occurred in WebSocket connection...", ex);
    }

    @Override
    public void onMessage(String message) {
        this.callback.onResponse(message);
    }

    public static URI getURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax exception, URI: " + uri);
        }
    }
}
