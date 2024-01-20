package org.websocket.demo.repo;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;
import org.websocket.demo.model.ChatRoom;
import org.websocket.demo.pubsub.RedisSubscriber;


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

    // 구독 처리 서비스
    private final RedisSubscriber redisSubscriber;

    // Redis
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;


    // chatRoom이란 이름으로 저장된 모든 HashMap들
    private HashOperations<String, String, ChatRoom> opsHashChatRoom;

    // 채팅방의 대화 메세지를 발행하기 위한  redis topic의 정보
    // 서버 별로 채팅방에 매치되는 topic 정보를 Map에 넣어 roomId로 찾을 수 있도록 한다.
    private Map<String, ChannelTopic> topics;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
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

    // 채팅방 입장: redis에 topic을 만들고 pub/sub 통신을 하기 위해 리스너를 설정한다.

    public void enterChatRoom(String roomId) {
        // 입장해야할 채팅방 (topic)을 얻어온다.
        ChannelTopic topic = topics.get(roomId);

        if(topic == null)
            topic = new ChannelTopic(roomId);

        // 해당 Topic으로 들어온 메세지는 어떻게 처리할 것인지에 대해 명세한 redisSubScriber를 Listener에 등록
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        topics.put(roomId, topic);
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }





}
