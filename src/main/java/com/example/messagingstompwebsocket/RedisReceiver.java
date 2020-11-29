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
     */
    public void sendMsg(String message) throws IOException {
        SendMsg msg = new ObjectMapper().readValue(message, SendMsg.class);
        simpMessagingTemplate.convertAndSendToUser(msg.getToUid(), "msg", msg);
    }

    /**
     * 处理广播消息
     */
    public void sendPublicMsg(String message) {
        simpMessagingTemplate.convertAndSend("/topic/public", message);
    }

    /**
     * 处理某频道消息
     */
    public void sendRoomMsg(String message) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SendRoomMsg sendRoomMsg = objectMapper.readValue(message, SendRoomMsg.class);
        simpMessagingTemplate.convertAndSend(String.format("/topic/%s", sendRoomMsg.getRoom()), sendRoomMsg.getContent());
    }
}
