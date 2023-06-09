package cc.xfl12345.person.cv.interceptor;

import cc.xfl12345.person.cv.appconst.AppConst;
import cc.xfl12345.person.cv.appconst.DefaultSingleton;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.FieldNotNullChecker;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class AdminAuthInterceptor extends AuthInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
        throws Exception {
        if (StpUtil.isLogin() && AppConst.XFL_WEBUI_ADMIN_LOGIN_ID.equals(StpUtil.getLoginId().toString())) {
            return true;
        }

        onForbidden(request, response, handler);
        return false;
    }

}
