package org.websocket.demo.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;



// 채팅 메세지를 주고받기 위한 DTO
@Getter
@Setter
public class ChatMessage {

    public ChatMessage() {}

    @Builder
    public ChatMessage (MessageType type, String roomId, String sender, String message, long userCount) {
        this.type = type;
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.userCount = userCount;
    }

    // 메시지 타입: 입장, 퇴장, 채팅 -> 서버에서 클라이언트가 어떤 행동할 때마다 메세지가 발행됨.
    // 그 행동이 무엇인지 기술한 것
    public enum MessageType {
        ENTER, QUIT ,TALK
    }



    private MessageType type;   // 메세지 타입
    private String roomId;      // 방 번호
    private String sender;      // 메세지 보낸 사람
    private String message;     // 메세지
    private long userCount;     // 채팅방의 인원 수, 채팅방이 메세지를 수신할 때마다 인원수 갱신되도록 할 것임.

}
