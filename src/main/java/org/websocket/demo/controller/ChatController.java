package org.websocket.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.websocket.demo.model.ChatMessage;
import org.websocket.demo.repo.ChatRoomRepository;
import org.websocket.demo.pubsub.RedisPublisher;

// WebSocket으로 들어오는 메세지를 처리하는 컨트롤러
// 발행과 구독은 config에서 설정했던 prefix로 구분
//      -> /pub/chat/message == pub 뒤에 주소로 메세지를 발행하는 요청
//      -> /sub/chat/message == sub 뒤에 주소로부터 발행되는 메세지를 받아오겠다는 요청

// 클라이언트가 채팅방 입장 시 채팅방(TOPIC)에서 대화가 가능하도록 리스너를 연동하는 enterChatRoom 메서드를 세팅
// 채팅방에 발행된 메세지는 서로 다른 서버에 공유하기 위해 redis의 Topic으로 다시 발행


@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRepository chatRoomRepository;

    // @MessageMapping == 메세지가 WebSocket으로 발행되는 경우 밑의 매소드가 실행된다.
    @MessageMapping("/chat/message")
    public void message (ChatMessage message) {
        if(ChatMessage.MessageType.ENTER.equals(message.getType())){
            chatRoomRepository.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }


        //메세지를 redis Publisher를 통해 발행 -> RedisDB에 저장된 해당 TOPIC으로 메세지를 보냄
        redisPublisher.publish(chatRoomRepository.getTopic(message.getRoomId()), message);
    }
}
