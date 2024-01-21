package org.websocket.demo.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.websocket.demo.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;



@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    // Redis에서 메세지가 발행(publish)되면 대기하고 있던 Redis Subscriber가 해당 메세지를 받아서 처리한다.

    public void sendMessage (String publishMessage) {
        try {
            // 발행된 메세지를 chatMessage DTO에 맞게 객체 매핑
            ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);

            // 채팅방을 구독한 클라이언트에게 메세지 발송
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getRoomId(), chatMessage);
        }catch (Exception e){
            log.error("Exception {}", e);
        }
    }
}
