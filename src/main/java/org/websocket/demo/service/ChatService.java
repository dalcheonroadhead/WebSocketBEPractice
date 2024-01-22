package org.websocket.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.websocket.demo.model.ChatMessage;
import org.websocket.demo.repo.ChatRoomRepository;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    // destination 정보에서 roomId 추출

        // lastIndexOf() 메서드는 지정된 문자 또는 문자열의 하위 문자열이 마지막으로 나타나는 위치를 변환
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');

        // lastIndexOf는 만약 우리가 찾는 문자가 문자열 내에 없으면 -1을 뱉는다.
        if(lastIndex != -1) {
            // 만약 /이 있다면 그것 이후부터 잘라서 온다.
            // 그러니까 destination이 chat/room/{방번호} 임으로 여기서 {방 번호}만 떼서 오는 것이다.
            return destination.substring(lastIndex +1);
        }else {
            return "";
        }
    }

    // 채팅방에 메세지 발송
    public void sendChatMessage(ChatMessage chatMessage) {
        chatMessage.setUserCount(chatRoomRepository.getUserCount(chatMessage.getRoomId()));

        if(ChatMessage.MessageType.ENTER.equals(chatMessage.getType())){
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에 입장했습니다.");
            chatMessage.setSender("[알림]");
        } else if (ChatMessage.MessageType.QUIT.equals(chatMessage.getType())){
            chatMessage.setMessage(chatMessage.getSender() + "님이 방에서 나갔습니다.");
            chatMessage.setSender("[알림]");
        }

        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

}
