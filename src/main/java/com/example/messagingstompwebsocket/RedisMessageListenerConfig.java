package com.example.messagingstompwebsocket;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisMessageListenerConfig {

    private final RedisReceiver redisReceiver;

    public RedisMessageListenerConfig(RedisReceiver redisReceiver) {
        this.redisReceiver = redisReceiver;
    }

    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter(), new ChannelTopic("im-topic"));
        redisMessageListenerContainer.addMessageListener(messageAllListenerAdapter(), new ChannelTopic("public-topic"));
        redisMessageListenerContainer.addMessageListener(messageRoomListenerAdapter(), new ChannelTopic("room-topic"));
        return redisMessageListenerContainer;
    }

    @Bean
    public MessageListener messageListenerAdapter() {
        return new MessageListenerAdapter(redisReceiver, "sendMsg");
    }

    @Bean
    public MessageListener messageAllListenerAdapter() {
        return new MessageListenerAdapter(redisReceiver, "sendPublicMsg");
    }

    @Bean
    public MessageListener messageRoomListenerAdapter() {
        return new MessageListenerAdapter(redisReceiver, "sendRoomMsg");
    }

}
