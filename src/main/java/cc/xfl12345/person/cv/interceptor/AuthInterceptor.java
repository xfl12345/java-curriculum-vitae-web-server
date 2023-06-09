package cc.xfl12345.person.cv.interceptor;

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

public class AuthInterceptor implements HandlerInterceptor {

    @Getter
    @Setter
    protected FieldNotNullChecker fieldNotNullChecker = DefaultSingleton.FIELD_NOT_NULL_CHECKER;

    @Getter
    @Setter
    protected ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        fieldNotNullChecker.check(objectMapper, "objectMapper");
    }

    protected void onForbidden(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        responseData.setApiResult(JsonApiResult.FAILED_NO_LOGIN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        try (Writer writer = response.getWriter()) {
            writer.append(objectMapper.writeValueAsString(responseData));
        }

    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
        throws Exception {
        if (StpUtil.isLogin()) {
            return true;
        }

        onForbidden(request, response, handler);
        return false;
    }
}
