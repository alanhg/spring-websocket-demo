package com.example.messagingstompwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 之所以还存储user会话中频道ID与频道名称的映射关系，因为在取消订阅时，拿不到频道名称，
 * 只可以拿到频道ID，因此需要多这样一个关系，确保根据ID还可以找回频道名称，进而广播告诉同频道的其它用户
 */
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
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        if (simpDestination.endsWith("public")) {
            return;
        }
        if (simpDestination.endsWith("monitor")) {
            return;
        }
        String topicKey = String.format(TOPIC_FORMAT, simpDestination);
        stringRedisTemplate.opsForSet().add(topicKey, MyPrincipal.userKeyFn.apply(simpUser));
        stringRedisTemplate.opsForHash().put(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId, simpDestination);
        stringRedisTemplate.expire(String.format(USER_FORMAT, simpSessionId), 1, TimeUnit.DAYS);
        stringRedisTemplate.expire(topicKey, 1, TimeUnit.DAYS);
        notify(simpDestination, simpUser, topicKey);
        notifyMonitor(simpUser);
    }


    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) throws JsonProcessingException {
        GenericMessage message = (GenericMessage) event.getMessage();
        String simpSubscriptionId = (String) message.getHeaders().get("simpSubscriptionId");
        MyPrincipal simpUser = (MyPrincipal) message.getHeaders().get("simpUser");
        String simpSessionId = (String) message.getHeaders().get("simpSessionId");
        String simpDestination = (String) stringRedisTemplate.opsForHash().get(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId);
        if (simpDestination.endsWith("public")) {
            return;
        }
        if (simpDestination.endsWith("monitor")) {
            return;
        }
        String key = String.format(TOPIC_FORMAT, simpDestination);
        stringRedisTemplate.opsForSet().remove(key, MyPrincipal.userKeyFn.apply(simpUser));
        stringRedisTemplate.opsForHash().delete(String.format(USER_FORMAT, simpSessionId), simpSubscriptionId);
        notify(simpDestination, simpUser, key);
        notifyMonitor(simpUser);
    }

    private void notify(String simpDestination, MyPrincipal simpUser, String key) throws JsonProcessingException {
        stringRedisTemplate.convertAndSend("room-topic", new ObjectMapper().writeValueAsString(SendRoomMsg.builder().uid(simpUser.getName()).room(simpDestination).content(
                stringRedisTemplate.opsForSet().members(key)
        ).build()));
    }

    private void notifyMonitor(MyPrincipal simpUser) throws JsonProcessingException {
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>() {
            {
                put("activeRooms", stringRedisTemplate.keys("online_topic_*").size());
            }
        };
        stringRedisTemplate.convertAndSend("room-topic", new ObjectMapper().writeValueAsString(SendRoomMsg.builder().uid(simpUser.getName()).room("monitor").content(
                hashMap
        ).build()));
    }
}
