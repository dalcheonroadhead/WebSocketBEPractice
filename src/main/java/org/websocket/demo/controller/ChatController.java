package org.websocket.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.websocket.demo.model.ChatMessage;
import org.websocket.demo.repo.ChatRoomRepository;
import org.websocket.demo.service.JwtTokenProvider;

// WebSocket으로 들어오는 메세지를 처리하는 컨트롤러
// 발행과 구독은 config에서 설정했던 prefix로 구분
//      -> /pub/chat/message == pub 뒤에 주소로 메세지를 발행하는 요청
//      -> /sub/chat/message == sub 뒤에 주소로부터 발행되는 메세지를 받아오겠다는 요청

// 클라이언트가 채팅방 입장 시 채팅방(TOPIC)에서 대화가 가능하도록 리스너를 연동하는 enterChatRoom 메서드를 세팅
// 채팅방에 발행된 메세지는 서로 다른 서버에 공유하기 위해 redis의 Topic으로 다시 발행


@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;
    private final JwtTokenProvider jwtTokenProvider;

    // @MessageMapping == 메세지가 WebSocket으로 발행되는 경우 밑의 매소드가 실행된다.
    @MessageMapping("/chat/message")
    public void message (ChatMessage message, @Header("token") String token) {

        //WebSocket 연결 후에 메세지 전송 시 마다 유효한 Token을 가졌는지 검사하는 코드
        // 만약 유효하지 않은 토큰을 가졌다면, if문 이하 코드가 실행 안될터이고, websocket을 통해 보낸 메세지는 무시된다.
        String nickname = jwtTokenProvider.getUserNameFromJwt(token);


        // 채팅방 입장 시에는 대화명과 메세지를 자동으로 세팅한다.
        if(ChatMessage.MessageType.ENTER.equals(message.getType())){
            message.setSender("[알림]");
            message.setMessage(nickname + "님이 입장하셨습니다.");
        }

        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);

        //WebSocket에 발행된 메세지를 redis로 발행(publish)
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}
