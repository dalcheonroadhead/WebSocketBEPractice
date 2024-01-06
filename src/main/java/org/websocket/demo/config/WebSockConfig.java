package org.websocket.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// BasicHandler를 이용하여 WebSocket을 활성화 하기 위한 Config 파일

@RequiredArgsConstructor
@Configuration
@EnableWebSocket // 웹 소켓 활성화
public class WebSockConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 웹 소켓에 접속하기 위한 endpoint를 "ws/chat"으로 설정
        // 다른 서버에서도 접속 가능하도록 CORS : setAllowedOrigins("*")을 설정
        registry.addHandler(webSocketHandler, "ws/chat").setAllowedOrigins("*");
    }
}
