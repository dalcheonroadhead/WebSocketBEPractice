package org.websocket.demo.config.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.websocket.demo.model.ChatMessage;
import org.websocket.demo.model.ChatRoom;
import org.websocket.demo.service.ChatService;

// Console log에 클라이언트의 입력을 찍어내기 위한 핸들러
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSockChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    @Override
    protected void handleTextMessage (WebSocketSession session, TextMessage message ) throws Exception{

        // 클라이언트로부터 받은 메세지를 콘솔로그로 출력 ->
        String payLoad = message.getPayload();
        log.info("payload {}", payLoad);

        // 클라이언트를 환영하는 콘솔 메세지 출력 (맨 처음 기본 단계)
        // TextMessage textMessage = new TextMessage("Welcome Chatting Server ~ ^^");
        // session.sendMessage(textMessage);

        // 웹소켓 클라이언트로부터 채팅 메세지를 전달받아 채팅 메세지 객체로 변환
        ChatMessage chatMessage = objectMapper.readValue(payLoad, ChatMessage.class);

        // 전달 받은 메세지에 담긴 채팅방 ID로 발송 대상의 채팅방 정보를 조회함.
        ChatRoom room = chatService.findRoomById(chatMessage.getRoomId());

        // 해당 채팅방에 입장해있는 모든 클라이언트들(Websocket session)에게 타입에 따른 메세지 발송
        room.handleActions(session, chatMessage, chatService);



    }
}
