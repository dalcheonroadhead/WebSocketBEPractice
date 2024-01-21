package org.websocket.demo.repo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;
import org.websocket.demo.model.ChatRoom;


import java.util.*;

// 채팅방을 생성하고 특정 채팅방 정보를 조회하는 Repository
// 생성된 채팅방은 초기화 되지 않도록 생성 시 Redis Hash에 저장하도록 처리
// 방 정보를 조회할 때는 Redis Hash에 저장된 데이터를 불러오도록 메서드 내용을 수정
// 채팅방 입장 시에는 채팅방 ID로 Redis Topic을 조회하여 pub/sub 메세지 리스너와 연동

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
    // 채팅방에 발행되는 메세지를 처리할 Listener
    private final RedisMessageListenerContainer redisMessageListener;

    // Redis 안의 채팅방 저장소 이름을 CHAT_ROOMS으로 하겠다는 의미
    private static final String CHAT_ROOMS = "CHAT_ROOM";

    // Redis의 ChatRoom 저장소와 CRUD를 진행하기 위함.
    private final RedisTemplate<String, Object> redisTemplate;

    // chatRoom이란 이름의 HashMap에 <K: 방 번호, V: 채팅방 객체> 형태로 저장
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;


    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById (String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성: 서버 간 채팅방 공유를 위해 redis hash에 저장한다.

    public ChatRoom createChatRoom(String name) {
        // 채팅방을 만들고
        ChatRoom chatRoom = ChatRoom.create(name);

        //Redis Hash에 저장
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);

        // 그 후 만든 ChatRoom을 반환
        return chatRoom;
    }



}
