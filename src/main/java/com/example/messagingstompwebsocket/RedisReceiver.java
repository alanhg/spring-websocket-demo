package com.example.messagingstompwebsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 处理订阅redis的消息
 */
@Component
public class RedisReceiver {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public RedisReceiver(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * 处理一对一消息
     *
     * @param message 消息队列中的消息
     */
    public void sendMsg(String message) throws IOException {
        SendMsg msg = new ObjectMapper().readValue(message, SendMsg.class);
        simpMessagingTemplate.convertAndSendToUser(msg.getToUid(), "msg", msg);
    }

    /**
     * 处理广播消息
     *
     * @param message
     */
    public void sendAllMsg(String message) {
        // 获取Topic名称即可解决
        simpMessagingTemplate.convertAndSend("/topic/public", message);
    }
}
