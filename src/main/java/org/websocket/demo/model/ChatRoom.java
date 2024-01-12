package org.websocket.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// 채팅방 DTO

/* 이전과 달라진 사항
*  1. 이제 stomp의 pub/sub 방식을 이용하므로, 구독자 관리가 알아서 된다. 따라서 chatRoom에서 따로 구독자들의 세션을 들고 있을 필요가 없다.
*  2. 또한 발송의 구현도 stomp의 pub/sub 방식이 사용되므로, 일일히 클라이언트에게 메세지를 발송하는 구현이 필요 없어진다. ->
* */

@Getter
@Setter
public class ChatRoom {

    // 채팅방의 방 번호와 이름
    private String roomId;
    private String name;

    // 위와 같이 원래 존재하던 역할들을 stomp가 대신 한다.
    // 따라서 chatRoom의 내용 또한 간소화 되었다.
    // 밑에 내용은 클래스 매소드로서 클래스 생성부터 존재하는 매소드이고, 하나의 chatRoom을 만들어서 반환한다.
    public static ChatRoom create(String name) {
       ChatRoom chatRoom = new ChatRoom();
       chatRoom.roomId = UUID.randomUUID().toString();
       chatRoom.name = name;

       return chatRoom;
    }
}
