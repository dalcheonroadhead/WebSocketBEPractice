package org.websocket.demo.repo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.websocket.demo.model.ChatRoom;


import java.util.*;

// 채팅방을 생성하고 특정 채팅방 정보를 조회하는 Repository
// 생성된 채팅방은 초기화 되지 않도록 생성 시 Redis Hash에 저장하도록 처리
// 방 정보를 조회할 때는 Redis Hash에 저장된 데이터를 불러오도록 메서드 내용을 수정
// 채팅방 입장 시에는 채팅방 ID로 Redis Topic을 조회하여 pub/sub 메세지 리스너와 연동

@RequiredArgsConstructor
@Service
public class ChatRoomRepository {

    // Redis Cache를 들여다 보기 위한 CacheKey
    private static final String CHAT_ROOMS = "CHAT_ROOM"; // 채팅룸을 저장한 Cache 열어보는 KEY
    private static final String USER_COUNT = "USER_COUNT"; // 채팅룸마다 입장한 클라이언트 수를 저장한 Cache 열어보는 KEY
    private static final String ENTER_INFO = "ENTER_INFO"; // 채팅룸에 입장한 클라이언트의 SessionID, 현재 입장한 채팅룸의 roomId를 맵핑한 정보를 저장한 Cache 열어보는 KEY

    // 의존 객체를 주입하는 어노테이션, 이름 -> 타입 -> @Qualifier 순으로 찾는다.
    // + @Qualifier는 주입 받을 빈 객체를 특정하는 어노테이션이다.
    // @Resource(name ="")과 같은 의미이다. 근데 @Resource(name="")하면 저 우선순위랑 상관없이 이름으로 바로 찾는다.
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;

    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;



    // 모든 채팅방 조회
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성: 생성 후 서버간 채팅방 공유를 위해 redis HK가 chat_room인 hashmap에 저장한다.
    public ChatRoom createChatRoom(String name){
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    // 유저가 입장한 채팅방ID와 유저 세션ID 맵핑 정보를 저장
    public void setUserEnterInfo(String sessionId, String roomId){
        hashOpsEnterInfo.put(ENTER_INFO,sessionId,roomId);
    }

    // 특정 유저 세션이 입장해 있는 채팅방 ID 조회
    public String getUserEnterRoomId(String sessionId){
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    // 유저 세션정보와 맵핑된 채팅방 ID 삭제
    public void removeUserEnterInfo(String sessionId){
        hashOpsEnterInfo.delete(ENTER_INFO,sessionId);
    }

    //------------------------------------------------------------------------------------------------------
    // valueOperations에 대해서 자세한 내용이 나오지 않아서 일단 이런 식으로 사용자 인원 수를 센다는 것을 인지한 뒤에,
    // Redis 수업을 들으며 익히자.
    // 특정 채팅방의 유저 수를 조회
    public long getUserCount(String roomId) {
        return  Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT+"_"+roomId)).orElse("0"));
    }

    // 채팅방에 입장한 유저수 +1
    public long plusUserCount(String roomId){
        return Optional.ofNullable(valueOps.increment(USER_COUNT+"_"+roomId)).orElse(0L);
    }

    // 채팅방에 입장한 유저 수 -1
    public long minusUserCount(String roomId){
        return Optional.ofNullable(valueOps.decrement(USER_COUNT+"_"+roomId)).filter(count -> count >0).orElse(0L);
    }


}
