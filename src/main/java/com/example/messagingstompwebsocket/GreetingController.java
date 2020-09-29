package com.example.messagingstompwebsocket;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.Set;

@RestController
public class GreetingController {

    private final StringRedisTemplate stringRedisTemplate;

    public GreetingController(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 发送消息，Web发来，直接回复
     *
     * @param message
     * @return
     */
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println(headerAccessor.getUser().getName());
        return new Greeting("Response: Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

    /**
     * 该API给其它服务call，然后进而广播消息
     * 广播时，将消息发送大redis sys-topic主题中
     *
     * @param command
     * @return
     */
    @GetMapping("/broadcast")
    public String sendBroadcastCommand(@RequestParam String command) {
        stringRedisTemplate.convertAndSend("sys-topic", command);
        return command;
    }

    /**
     * 前台发送消息,一对一
     * Principal为连接websocket校验时返回的，可以直接在参数中使用
     *
     * @param msg
     * @param principal
     * @return
     */
    @MessageMapping("/send2user")
    public String send2user(@Validated SendMsg msg, Principal principal) {
        String uid = principal.getName();
        // 当前发送信息的uid
        msg.setUid(uid);
        //获取在线的用户列表
        Set<String> onlineUsers = stringRedisTemplate.opsForSet().members("online");
        //判断发送的用户是否在线
        if (onlineUsers.contains(msg.getToUid())) {
            // 如果用户在线，则将消息发送到redis消息队列im-topic主题中，所有连接同一个redis的应用并订阅im-topic主题都会收到这条消息。
            // 然后都使用SimpMessagingTemplate发送消息到指定的订阅中
            // 接收消息发送消息的类 RedisReceiver
            stringRedisTemplate.convertAndSend("im-topic", msg);
        } else {
            //用户不在线，保存消息记录，用户上线后拉取，这里不做实现
        }
        return "success";
    }

    /**
     * 向订阅某个Topic的用户群发消息
     */
    @GetMapping("/sendToTopic")
    public void sendToTopic(@RequestParam String topic, @RequestParam String content) {

    }

}
