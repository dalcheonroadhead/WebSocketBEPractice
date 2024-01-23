package org.websocket.demo.config;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

// 로컬 환경에서만 실행합니다.
@Profile("local")
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() {
        redisServer = RedisServer.builder()
                .port(redisPort)
                .setting("maxmemory 128M")
                .build();
    }

    @PreDestroy
    public void stopRedis() {
        if(redisServer != null){
            redisServer.stop();
        }
    }
}
