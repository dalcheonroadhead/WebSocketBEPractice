package org.websocket.demo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.websocket.demo.pubsub.RedisSubscriber;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    // 1) 단일 TOPIC 사용을 위한 Bean 설정
    // ChannelTopic은 Redis에 존재하는 Channel(Topic) 과 맵핑 되는 객체
    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    // 2) redis 내장 pub/sub 기능을 사용하기 위해 Message Listener 설정을 추가
    // ++ redis 로 발행(publish)된 메세지를 처리하기 위한 리스너 설정
    @Bean
    public RedisMessageListenerContainer redisMessageListener(RedisConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter, ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, channelTopic);
        return  container;
    }

    // 3) 실제 메세지를 처리하는 subScriber 설정 추가
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisSubscriber subscriber){
        return new MessageListenerAdapter(subscriber, "sendMessage");
    }


    // 4) 우리가 만든 어플리케이션에서 사용할 RedisTemplate 설정, 직렬화 어케 할 건지, RestTemplate 연결에는 어떤 팩토리를 쓰는지
    // RedisTemplate: Redis 데이터 베이스와 상호작용하기위한 설정을 담은 클래스

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));

        return redisTemplate;
    }
}
