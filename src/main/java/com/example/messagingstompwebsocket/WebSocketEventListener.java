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

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        stringRedisTemplate.opsForSet().add(String.format("online_room_%s", (simpDestination.split("/")[2]), simpUser.getName()));
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpDestination = (String) message.getHeaders().get("simpDestination");
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        stringRedisTemplate.opsForSet().remove(String.format("online_room_%s", (simpDestination.split("/")[2]), simpUser.getName()));
    }
}
