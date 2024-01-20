package org.websocket.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// 이제 STOMP를 이용하여 WebSocket을 구현할 것이기 때문에 따로 WebSockHandler가 필요 없다.
// 왜냐하면 STOMP가 웹 소켓에서 메세지를 다루는 방법에 대한 규약이기 때문이다.

@Configuration
@EnableWebSocketMessageBroker // 웹소켓에서 STOMP를 활성화 하기 위한 어노테이션
public class WebSockConfig implements WebSocketMessageBrokerConfigurer {


    // 메세지 송 수신에 대한 설정을 등록한 매소드이다.
    // 메세지 수신은 sub/room/{방 번호}로 해당 방 번호로 온 메세지만 수신하도록 설정
    // 메세지 발신의 경우에는 pub/room 으로 보내고 방 번호는 responseBody 속 메타데이터로 저장한다.
    @Override
    public void configureMessageBroker (MessageBrokerRegistry config){

        //메세지를 발행하는 요청의 접두사는 /pub가 되도록 설정
        config.setApplicationDestinationPrefixes("/pub");

        //메세지를 구독하는 요청의 접두사는 /sub가 되도록 설정
        config.enableSimpleBroker("/sub");

    }


    // Stomp 소켓을 쓸 주소를 특정한다.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){

        // stomp websocket으로 연결하는 주소의 endpoint는 /ws-stomp로 설정
        // 따라서 전체 주소는 ws://localhost:8080/ws-stomp
        registry.addEndpoint("/ws-stomp").setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
