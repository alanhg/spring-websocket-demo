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
    private final MyHandshakeHandler myHandshakeHandler;


    public WebSocketConfig(MyWebSocketHandler myWebSocketHandler, MyHandshakeHandler myHandshakeHandler) {
        this.myWebSocketHandler = myWebSocketHandler;
        this.myHandshakeHandler = myHandshakeHandler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 向客户端发消息: 启用/user /topic两个消息前缀
        config.enableSimpleBroker("/user", "/topic");
        // 向客户端发消息: 一对一的消息
        config.setUserDestinationPrefix("/user");

        // 客户端向服务端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 客户端和服务端进行连接的endpoint
        registry.addEndpoint("/websocket")
                .setHandshakeHandler(myHandshakeHandler) // 设置连接校验
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 注册登陆退出
        registry.addDecoratorFactory(myWebSocketHandler);
    }
}
