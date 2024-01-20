package org.websocket.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// 채팅방 DTO


// Redis에 저장되는 객체들은 Serializable이 가능해야 하므로, Serializable을 참조하도록 선언하고
// serialVersionUID를 세팅해준다.

@Getter
@Setter
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    // 채팅방의 방 번호와 이름
    private String roomId;
    private String name;


    // 밑에 내용은 클래스 매소드로서 클래스 생성부터 존재하는 매소드이고, 하나의 chatRoom을 만들어서 반환한다.
    public static ChatRoom create(String name) {
       ChatRoom chatRoom = new ChatRoom();
       chatRoom.roomId = UUID.randomUUID().toString();
       chatRoom.name = name;

       return chatRoom;
    }
}
