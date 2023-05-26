package cc.xfl12345.person.cv.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * <a herf="https://github.com/dromara/Sa-Token/blob/dev/sa-token-demo/sa-token-demo-websocket-spring/src/main/java/com/pj/ws/WebSocketInterceptor.java">source code URL</a>
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    // 握手之前触发 (return true 才会握手成功 )
    @Override
    public boolean beforeHandshake(
        @Nonnull ServerHttpRequest request,
        @Nonnull ServerHttpResponse response,
        @Nonnull WebSocketHandler handler,
        @Nonnull Map<String, Object> attr) {

        // System.out.println("---- 握手之前触发 " + StpUtil.getTokenValue());

        // 未登录情况下拒绝握手
        if(!StpUtil.isLogin()) {
            // System.out.println("---- 未授权客户端，连接失败");
            response.setStatusCode(HttpStatusCode.valueOf(HttpStatus.FORBIDDEN.value()));
            return false;
        }

        // 标记 loginId，握手成功
        attr.put("loginId", StpUtil.getLoginId());
        return true;
    }

    // 握手之后触发
    @Override
    public void afterHandshake(
        @Nonnull ServerHttpRequest request,
        @Nonnull ServerHttpResponse response,
        @Nonnull WebSocketHandler wsHandler,
        Exception exception) {
        // System.out.println("---- 握手之后触发 ");
    }

}
