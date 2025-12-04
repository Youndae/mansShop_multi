package com.example.moduleapi.config.webSocket;

import com.example.moduleauthapi.service.JWTTokenProvider;
import com.example.moduleconfig.properties.CookieProperties;
import com.example.moduleconfig.properties.TokenProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JWTTokenProvider tokenProvider;

    private final TokenProperties tokenProperties;

    private final CookieProperties cookieProperties;

    public WebSocketConfig(JWTTokenProvider jwtTokenProvider,
                           TokenProperties tokenProperties,
                           CookieProperties cookieProperties) {
        this.tokenProvider = jwtTokenProvider;
        this.tokenProperties = tokenProperties;
        this.cookieProperties = cookieProperties;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(
                        new HandshakeInterceptor() {
                            @Override
                            public boolean beforeHandshake(ServerHttpRequest request,
                                                           ServerHttpResponse response,
                                                           WebSocketHandler wsHandler,
                                                           Map<String, Object> attributes) throws Exception {
                                if(request instanceof ServletServerHttpRequest) {
                                    HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

                                    Cookie[] cookies = servletRequest.getCookies();

                                    if(cookies != null) {
                                        for(Cookie cookie : cookies) {
                                            if(cookie.getName().equals(cookieProperties.getIno().getHeader()))
                                                attributes.put("ino", cookie.getValue());
                                        }
                                    }else
                                        log.warn("WebSocket Connection cookie is null");
                                }

                                return true;
                            }

                            @Override
                            public void afterHandshake(ServerHttpRequest request,
                                                       ServerHttpResponse response,
                                                       WebSocketHandler wsHandler,
                                                       Exception exception
                            ) {}
                        }
                )
                .withSockJS()
                .setSessionCookieNeeded(true)
                .setHeartbeatTime(25000)
                .setDisconnectDelay(30000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");

        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                new ChannelInterceptor() {
                    @Override
                    public Message<?> preSend(Message<?> message, MessageChannel channel) {
                        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                        if(accessor != null && accessor.getCommand() == StompCommand.CONNECT) {
                            String token = accessor.getFirstNativeHeader(tokenProperties.getAccess().getHeader());
                            String inoValue = (String) accessor.getSessionAttributes().get("ino");

                            if (token != null) {
                                String tokenValue = token.replace(tokenProperties.getPrefix(), "");
                                String userId = tokenProvider.verifyAccessToken(tokenValue, inoValue);

                                if(userId != null && !userId.equals("WRONG_TOKEN") && !userId.equals("TOKEN_EXPIRATION") && !userId.equals("TOKEN_STEALING")) {
                                    accessor.setUser(() -> userId);
                                    log.info("WebSocket Principal set : {}", userId);
                                }
                            }
                        }

                        return message;
                    }
                }
        );
    }
}
