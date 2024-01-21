package org.websocket.demo.config.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.websocket.demo.service.JwtTokenProvider;

// WebSocket 연결 시 요청 header의 jwtToekn 유효성을 검증하는 코드
// 유효하지 않은 Jwt 토큰이 세팅될 경우 webSocket 연결을 하지 않고 예외 처리한다.
@Slf4j
@RequiredArgsConstructor // JwtTokenProvider에 대한 생성자 주입이 일어난다.
@Component
public class StompHandler implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    // webSocket을 통해 들어온 요청이 처리 되기 전에 실행된다.
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // webSocket 연결 시 헤더의 jwt Token 검증
        if(StompCommand.CONNECT == accessor.getCommand()){
            jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("token"));
        }

        return message;
    }

}
