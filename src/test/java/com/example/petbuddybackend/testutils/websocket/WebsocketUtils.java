package com.example.petbuddybackend.testutils.websocket;

import lombok.SneakyThrows;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static java.util.concurrent.TimeUnit.SECONDS;

public class WebsocketUtils {

    @SneakyThrows
    public static StompSession connectToWebSocket(WebSocketStompClient stompClient, int PORT, String websocketUrlPattern) {
        return stompClient.connectAsync(
                String.format(websocketUrlPattern, PORT),
                new StompSessionHandlerAdapter() {}
        ).get(1, SECONDS);
    }

    public static StompSession.Subscription subscribeToTopic(
            StompSession stompSession,
            StompHeaders headers,
            StompFrameHandler stompFrameHandler
    ) {
        return stompSession.subscribe(headers, stompFrameHandler);
    }

}
