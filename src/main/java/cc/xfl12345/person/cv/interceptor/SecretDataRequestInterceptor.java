package cc.xfl12345.person.cv.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

public class SecretDataRequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
        throws Exception {
        boolean isSignedIn = StpUtil.isLogin();
        if (isSignedIn) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);

        return false;
    }
}
