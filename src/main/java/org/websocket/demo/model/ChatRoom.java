package org.websocket.demo.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;
import org.websocket.demo.service.ChatService;

import java.util.HashSet;
import java.util.Set;

// 채팅방 DTO
@Getter
public class ChatRoom {

    // 채팅방의 방 번호와 이름
    private String roomId;
    private String name;

    // 해당 채팅방에 입장한 클라이언트들의 세션 리스트
    private Set<WebSocketSession> sessions = new HashSet<>();

    // 커스터마이징한 생성자 -> 롬복 쓰면 안됨! 그거는 모든 멤버 변수에 대해서 매핑을 진행하기 때문에!!
    @Builder
    public ChatRoom (String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    // 채팅방에서 하는 행동에는 크게 입장하기와 대화하기 기능 두 개가 있다.
    // 따라서 클라이언트가 어떤 행동을 하느냐에 따라 분기처리를 해줘야 한다.
    // 그것을 위한 매소드이다.
    // 입장 시: 채팅룸의 session 정보를 클라이언트의 session 리스트에 추가
    // 채팅 시: 채팅룸에 메세지가 도착할 경우 채팅룸의 모든 session에 메시지 발송
    public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService){
        if(chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessage, chatService);
    }

    public <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
    }
}
