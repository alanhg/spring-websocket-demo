package com.example.messagingstompwebsocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.util.HashMap;

/**
 * 用户登录退出操作
 */
@Component
public class MyWebSocketHandler implements WebSocketHandlerDecoratorFactory {

    private final StringRedisTemplate stringRedisTemplate;

    public MyWebSocketHandler(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return getWebSocketHandlerDecorator(handler);
    }

    private WebSocketHandlerDecorator getWebSocketHandlerDecorator(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {

            private void notify(MyPrincipal simpUser) throws JsonProcessingException {
                HashMap<String, Integer> hashMap = new HashMap<String, Integer>() {
                    {
                        put("activeUsers", stringRedisTemplate.opsForSet().members("online").size());
                    }
                };
                stringRedisTemplate.convertAndSend("room-topic", new ObjectMapper().writeValueAsString(SendRoomMsg.builder().uid(simpUser.getName()).room("monitor").content(
                        hashMap
                ).build()));
            }

            // 用户登录
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                MyPrincipal principal = (MyPrincipal) session.getPrincipal();
                // 将用户存入到redis在线用户中
                stringRedisTemplate.opsForSet().add("online", MyPrincipal.userKeyFn.apply(principal));
                notify(principal);
                super.afterConnectionEstablished(session);
            }

            // 用户退出
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                MyPrincipal principal = (MyPrincipal) session.getPrincipal();
                // 将用户从redis在线用户中删除
                stringRedisTemplate.opsForSet().remove("online", MyPrincipal.userKeyFn.apply(principal));
                notify(principal);
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }
}
