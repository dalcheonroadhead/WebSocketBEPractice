package org.websocket.demo.model;

import lombok.Getter;
import lombok.Setter;

/*
* 클라이언트들은 서버에 접속하면 개별의 WebSocket Session을 가지게 된다.
* 따라서 채팅방에 입장 시 클라이언트들의 WebSocket Session 정보를 채팅방에 맵핑 시켜서 보관하고 있으면,
* 서버에 전달된 메시지를 특정 방에 매핑된  WebSocket 세션 전부에게 보낼 수 있음. -> 이것이 메세지를 구현한 모습
* 이것으로 개별 채팅방을 구현할 수 있다.
* */

// 채팅 메세지를 주고받기 위한 DTO
@Getter
@Setter
public class ChatMessage {

    // 메시지 타입: 입장, 채팅 -> 서버에서 클라이언트의 행동에 따라 클라이언트에게 전달할 메세지들을 선언한 것
    public enum MessageType {
        ENTER, TALK
    }


    private MessageType type;   // 메세지 타입
    private String roomId;      // 방 번호
    private String sender;      // 메세지 보낸 사람
    private String message;     // 메세지

}
