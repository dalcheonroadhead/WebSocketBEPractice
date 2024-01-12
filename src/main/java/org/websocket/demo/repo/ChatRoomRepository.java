package org.websocket.demo.repo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import org.websocket.demo.model.ChatRoom;

import java.util.*;

// 채팅방을 생성하고 정보를 조회하는 Repository
@Repository
public class ChatRoomRepository {

    // 채팅방 정보는 DB를 안 거치고 간단하게 저장할 것이므로, Map으로 저장해둔다.
    private Map<String, ChatRoom> chatRoomMap;


    // ChatRoomRepository가 빈 객체로 만들어진 이후에, 딱 한번 실행되는 함수이다.
    @PostConstruct
    private void init() {
        chatRoomMap = new LinkedHashMap<>();
    }

    // 채팅방 전체 조회
    public List<ChatRoom> findAllRoom() {
        // 저장고로부터 모든 채팅룸을 받아 List에 넣는다.
        List<ChatRoom> chatRooms = new ArrayList<>(chatRoomMap.values());
        // 채팅방을 생성 순서대로 반환하기 위해 한번 뒤집는다.
        Collections.reverse(chatRooms);
        return chatRooms;
    }

    // 방 번호에 맞는 녀석 하나만 찾기
    public  ChatRoom findRoomById(String id) {
        return chatRoomMap.get(id);
    }

    // 방 생성
    public ChatRoom createChatRoom(String name){
        // DTO에 적어놨던 채팅방 생성 로직을 써서 채팅방을 만든다.
        ChatRoom chatRoom = ChatRoom.create(name);
        // 해당 채팅방을 채팅방의 방 번호를 index로 저장고에 저장한다.
        chatRoomMap.put(chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

}
