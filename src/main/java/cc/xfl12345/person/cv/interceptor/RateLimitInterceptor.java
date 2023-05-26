package cc.xfl12345.person.cv.interceptor;

import cc.xfl12345.person.cv.appconst.DefaultSingleton;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.*;
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
import java.util.Map;

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
            SimpleBucketConfigUtils.createConfigJustInMinutes(20),
            SimpleBucketConfigUtils.createConfigJustInMinutes(30)
        );

        captchaGenerateRateHelper = anyUserRequestRateLimitHelperFactory.generate(
            "captchaGenerateRate",
            SimpleBucketConfigUtils.createConfigJustInMinutes(20),
            SimpleBucketConfigUtils.createConfigJustInMinutes(30)
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

        RateLimitHelper.ConsumeResult consumeResult = helper.tryConsume(request);

        if (consumeResult.isSuccess()) {
            return true;
        } else {
            JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
            responseData.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
            responseData.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
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
