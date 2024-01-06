package org.websocket.demo.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.websocket.demo.model.ChatRoom;

import java.io.IOException;
import java.util.*;

/*
* 채팅방을 생성, 채팅방을 조회, 하나의 세션에 메세지를 발송하는 서비스를 구현
*/

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ObjectMapper objectMapper;

    // 서버에 생성된 모든 채팅방의 정보를 모아둔 구조체. (채팅방의 정보 저장은 빠른 구현을 위해 일단 DB 안 쓰고 HashMap 저장으로 구현)
    private Map<String, ChatRoom> chatRooms;

    // 생성자 이용해 생성된 후에 할 일을 정의
    @PostConstruct
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    // 모든 방 조회
    public List<ChatRoom> findAllRoom() {
        return  new ArrayList<>(chatRooms.values());
    }

    // 방 번호를 이용한 방 찾기
    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }

    // 방 생성
    public ChatRoom createRoom(String name) {
        // 방번호로 쓸 아이디 생성
        String randomId = UUID.randomUUID().toString();

        // 채팅방 (1 개) 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(randomId)
                .name(name)
                .build();
        //생성된 채팅방을 채팅방 리스트에 넣기
        chatRooms.put(randomId, chatRoom);

        return chatRoom;
    }

    // 세션 하나에 메세지를 보내는 매소드
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsBytes(message)));
        }catch (IOException e){
            log.error(e.getMessage(), e);
        }
    }
}
