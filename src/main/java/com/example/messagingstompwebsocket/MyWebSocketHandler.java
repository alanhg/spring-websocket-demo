package com.example.messagingstompwebsocket;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

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
        return new WebSocketHandlerDecorator(handler) {
            // 用户登录
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                String uid = session.getPrincipal().getName();
                // 将用户存入到redis在线用户中
                stringRedisTemplate.opsForSet().add("online", uid);
                super.afterConnectionEstablished(session);
            }

            // 用户退出
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                System.out.println(session.getUri());
                String uid = session.getPrincipal().getName();
                // 将用户从redis在线用户中删除
                stringRedisTemplate.opsForSet().remove("online", uid);
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }
}
