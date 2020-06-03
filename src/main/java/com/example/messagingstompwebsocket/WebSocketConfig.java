package com.example.messagingstompwebsocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final MyWebSocketHandler myWebSocketHandler;
    private MyHandshakeHandler myHandshakeHandler;


    public WebSocketConfig(MyWebSocketHandler myWebSocketHandler) {
        this.myWebSocketHandler = myWebSocketHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用/user /topic两个消息前缀,消息发送的前缀，也是前端订阅的前缀
        config.enableSimpleBroker("/user", "/topic");

        // 当使用convertAndSendToUser发送消息时，前端订阅用/user开头。即一对一发送消息，使用/user为前缀订阅
        config.setUserDestinationPrefix("/user");

        // 前端向服务端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 客户端和服务端进行连接的endpoint
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 注册登陆退出
        registry.addDecoratorFactory(myWebSocketHandler);
    }
}
