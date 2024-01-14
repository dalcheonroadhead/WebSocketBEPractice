package org.websocket.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.websocket.demo.model.ChatMessage;

// WebSocket으로 들어오는 메세지를 처리하는 컨트롤러
// 발행과 구독은 config에서 설정했던 prefix로 구분
//      -> /pub/chat/message == pub 뒤에 주소로 메세지를 발행하는 요청
//      -> /sub/chat/message == sub 뒤에 주소로부터 발행되는 메세지를 받아오겠다는 요청

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    // 메세지가 목적지로 전송되는 경우, 밑의 매소드가 실행된다.
    @MessageMapping("/chat/message")
    public void message (ChatMessage message) {
        // 만약 메세지 타입이 ENTER 이면 입장 메세지를 내용물로 첨부
        if(ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        // 메세지를 채팅방 안에 존재하는 모든 사람들에게 보낸다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
