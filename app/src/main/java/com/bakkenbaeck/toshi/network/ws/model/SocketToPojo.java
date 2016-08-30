package com.bakkenbaeck.toshi.network.ws.model;


import com.bakkenbaeck.toshi.network.ws.SocketObservables;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SocketToPojo {

    private final Moshi moshi;
    private final JsonAdapter<WebSocketMessage> jsonAdapter;
    private final JsonAdapter<Payment> paymentAdapter;
    private final SocketObservables socketObservables;

    public SocketToPojo(final SocketObservables socketObservables) {
        this.socketObservables = socketObservables;
        this.moshi = new Moshi
                            .Builder()
                            .build();
        this.jsonAdapter = this.moshi.adapter(WebSocketMessage.class);
        this.paymentAdapter = this.moshi.adapter(Payment.class);
    }

    public void handleNewMessage(final String json) {
        try {
            convertAndEmitPojo(json);
        } catch (final IOException e) {
            LogUtil.e(getClass(), e.toString());
        }

    }

    private void convertAndEmitPojo(final String json) throws IOException {
        final WebSocketMessage message = getWebSocketMessageFromJson(json);
        if (message == null) {
            return;
        }

        switch (message.type) {
            case "hello":
                // this type can be ignored
                break;
            case "payment":
                final Payment payment = this.paymentAdapter.fromJson(json);
                this.socketObservables.emit(payment);
                break;
            default:
                LogUtil.e(getClass(), "Unrecognised websocket message type - " + message.type);
        }
    }

    private WebSocketMessage getWebSocketMessageFromJson(final String message) {
        try {
            final WebSocketMessage webSocketMessage = this.jsonAdapter.fromJson(message);
            return webSocketMessage;
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Invalid JSON input. " + e);
            return null;
        }
    }
}
