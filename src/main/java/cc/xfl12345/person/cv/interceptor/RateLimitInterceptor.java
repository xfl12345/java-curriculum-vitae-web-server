package cc.xfl12345.person.cv.interceptor;

import cc.xfl12345.person.cv.appconst.DefaultSingleton;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelper;
import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelperFactory;
import cc.xfl12345.person.cv.pojo.FieldNotNullChecker;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Slf4j
@FieldNameConstants
public class RateLimitInterceptor implements HandlerInterceptor {
    @Getter
    @Setter
    protected FieldNotNullChecker fieldNotNullChecker = DefaultSingleton.FIELD_NOT_NULL_CHECKER;

    @Setter
    @Getter
    protected ObjectMapper objectMapper;

    @Getter
    @Setter
    protected AnyUserRequestRateLimitHelperFactory anyUserRequestRateLimitHelperFactory;

    protected AnyUserRequestRateLimitHelper captchaCheckRateHelper;

    protected AnyUserRequestRateLimitHelper captchaGenerateRateHelper;

    @PostConstruct
    public void init() {
        fieldNotNullChecker.check(objectMapper, Fields.objectMapper);
        fieldNotNullChecker.check(anyUserRequestRateLimitHelperFactory, Fields.anyUserRequestRateLimitHelperFactory);

        captchaCheckRateHelper = anyUserRequestRateLimitHelperFactory.generate(
            "captchaCheckRate",
            20,
            30
        );

        captchaGenerateRateHelper = anyUserRequestRateLimitHelperFactory.generate(
            "captchaGenerateRate",
            20,
            30
        );
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        String requestPath = request.getServletPath();

        if (requestPath.startsWith("/captcha/generate")) {
            return checkBucket(request, response, captchaGenerateRateHelper);
        } else if (requestPath.startsWith("/captcha/check")) {
            return checkBucket(request, response, captchaCheckRateHelper);
        }

        return true;
    }

    protected boolean checkBucket(
        @Nonnull HttpServletRequest request,
        @Nonnull HttpServletResponse response,
        AnyUserRequestRateLimitHelper helper) throws IOException {

        JsonApiResponseData responseData = helper.tryConsume(request);

        if (responseData.getCode() == JsonApiResult.SUCCEED.getNum()) {
            return true;
        } else {
            if (responseData.getCode() == JsonApiResult.FAILED_FREQUENCY_MAX.getNum()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            } else {
                response.setStatus(HttpStatus.FORBIDDEN.value());
            }

            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (Writer writer = response.getWriter()) {
                writer.append(objectMapper.writeValueAsString(responseData));
            }

            return false;
        }

    }


    @Override
    public void postHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
