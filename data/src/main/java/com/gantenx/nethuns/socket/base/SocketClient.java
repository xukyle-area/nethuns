package com.gantenx.nethuns.socket.base;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

@Slf4j
public class SocketClient extends WebSocketClient {

    private final Consumer<String> callback;

    public SocketClient(String serverUri, Consumer<String> callback) {
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
        this.callback.accept(message);
    }

    /**
     * build object of URI
     */
    public static URI getURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax exception, URI: " + uri);
        }
    }
}
