package com.example.messagingstompwebsocket;

import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
public class WebSocketEventListener {
    private final StringRedisTemplate stringRedisTemplate;

    public WebSocketEventListener(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * simpDestination: /topic/123
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        simpDestination = simpDestination.split("/")[2];
        String simpSubscriptionId = (String) message.getHeaders().get("simpSubscriptionId");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        if (!simpDestination.endsWith("public")) {
            MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
            String key = String.format("online_topic_%s", simpDestination);
            stringRedisTemplate.opsForSet().add(key, simpUser.getName());
            stringRedisTemplate.opsForHash().put(String.format("online_user_%s", simpSessionId), simpSubscriptionId, simpDestination);
        }
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpSubscriptionId = (String) message.getHeaders().get("simpSubscriptionId");
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        String simpDestination = (String) stringRedisTemplate.opsForHash().get(String.format("online_user_%s", simpSessionId), simpSubscriptionId);
        stringRedisTemplate.opsForSet().remove(String.format("online_topic_%s", simpDestination), simpUser.getName());
        stringRedisTemplate.opsForHash().delete(String.format("online_user_%s", simpSessionId), simpSubscriptionId);

    }
}
