package com.example.messagingstompwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class WebSocketEventListener {
    private final StringRedisTemplate stringRedisTemplate;
    String USER_FORMAT = "online_user_%s";
    String TOPIC_FORMAT = "online_topic_%s";

    public WebSocketEventListener(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * simpDestination: /topic/123
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) throws JsonProcessingException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        simpDestination = simpDestination.split("/")[2];
        String simpSubscriptionId = (String) message.getHeaders().get("simpSubscriptionId");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        if (!simpDestination.endsWith("public")) {
            MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
            String key = String.format(TOPIC_FORMAT, simpDestination);
            stringRedisTemplate.opsForSet().add(key, simpUser.getName());
            stringRedisTemplate.opsForHash().put(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId, simpDestination);
            notify(simpDestination, simpUser, key);
        }
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpSubscriptionId = (String) message.getHeaders().get("simpSubscriptionId");
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        String simpDestination = (String) stringRedisTemplate.opsForHash().get(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId);
        String key = String.format(TOPIC_FORMAT, simpDestination);
        stringRedisTemplate.opsForSet().remove(key, simpUser.getName());
        stringRedisTemplate.opsForHash().delete(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId);
        notify(simpDestination, simpUser, key);
    }

    private void notify(String simpDestination, MyPrincipal simpUser, String key) throws JsonProcessingException {
        stringRedisTemplate.convertAndSend("room-topic", new ObjectMapper().writeValueAsString(SendRoomMsg.builder().room(simpDestination).content(
                stringRedisTemplate.opsForSet().members(key)
        ).build()));
    }
}
