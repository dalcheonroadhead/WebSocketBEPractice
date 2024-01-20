package org.websocket.demo.model;

import lombok.Getter;
import lombok.Setter;



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
