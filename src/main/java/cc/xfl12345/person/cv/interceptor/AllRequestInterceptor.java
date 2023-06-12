package cc.xfl12345.person.cv.interceptor;

import cc.xfl12345.person.cv.appconst.AppConst;
import cc.xfl12345.person.cv.appconst.DefaultSingleton;
import cc.xfl12345.person.cv.pojo.FieldNotNullChecker;
import cc.xfl12345.person.cv.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
public class AllRequestInterceptor implements HandlerInterceptor {

    @Getter
    @Setter
    protected FieldNotNullChecker fieldNotNullChecker = DefaultSingleton.FIELD_NOT_NULL_CHECKER;

    @Getter
    @Setter
    protected UserService userService;

    @PostConstruct
    public void init() {
        fieldNotNullChecker.check(userService, "userService");
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 已登录用户访问更新最后一次访问时间
        if (StpUtil.isLogin()) {
            String loginId = StpUtil.getLoginId().toString();
            if (!(AppConst.XFL_WEBUI_ADMIN_LOGIN_ID.equals(loginId) || AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID.equals(loginId))) {
                userService.justUpdateVisitTimeById(Long.parseLong(StpUtil.getLoginId().toString()), LocalDateTime.now());
            }
        }

        return true;
    }

}
