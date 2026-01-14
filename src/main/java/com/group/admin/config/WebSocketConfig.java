package com.group.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置
 * 用於跑馬燈、即時通知等功能
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 設定用戶端訂閱的目的地前綴
        // /topic - 廣播給所有訂閱者（如跑馬燈）
        // /queue - 點對點訊息（如個人通知）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 設定用戶端發送訊息的前綴
        config.setApplicationDestinationPrefixes("/app");
        
        // 設定點對點訊息的前綴
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 連線端點
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // 支援 SockJS fallback
        
        // 純 WebSocket 端點（不使用 SockJS）
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}
