package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.interceptor.WebSocketInterceptor;
import cc.xfl12345.person.cv.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/**
 * <a herf="https://github.com/dromara/Sa-Token/blob/dev/sa-token-demo/sa-token-demo-websocket-spring/src/main/java/com/pj/ws/WebSocketConfig.java">source code URL</a>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public SmsService smsService;

    @Autowired
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
    }

    // 注册 WebSocket 处理器
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
            // WebSocket 连接处理器
            .addHandler(smsService, "/sms/ws-connect")
            // WebSocket 拦截器
            .addInterceptors(new WebSocketInterceptor())
            .setAllowedOrigins("*");
    }

}
