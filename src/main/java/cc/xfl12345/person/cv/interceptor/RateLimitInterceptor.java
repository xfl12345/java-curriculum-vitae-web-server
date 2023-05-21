package cc.xfl12345.person.cv.interceptor;

import cc.xfl12345.person.cv.appconst.DefaultSingleton;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.FieldNotNullChecker;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@FieldNameConstants
public class RateLimitInterceptor implements HandlerInterceptor {
    @Getter
    @Setter
    protected FieldNotNullChecker fieldNotNullChecker = DefaultSingleton.FIELD_NOT_NULL_CHECKER;

    @Setter
    protected ObjectMapper objectMapper;

    @Setter
    protected CacheManager cacheManager = null;

    @Setter
    protected RequestAnalyser requestAnalyser = null;

    // ipAddress -> bucket
    protected Cache<String, Bucket> captchaCheckRateLimitViaIpAddress;

    // loginId -> bucket
    protected Cache<String, Bucket> captchaCheckRateLimitViaLoginId;

    // ipAddress -> bucket
    protected Cache<String, Bucket> captchaGenerateRateLimitViaIpAddress;

    // loginId -> bucket
    protected Cache<String, Bucket> captchaGenerateRateLimitViaLoginId;

    @PostConstruct
    public void init() {
        fieldNotNullChecker.check(objectMapper, Fields.objectMapper);
        fieldNotNullChecker.check(cacheManager, Fields.cacheManager);
        fieldNotNullChecker.check(requestAnalyser, Fields.requestAnalyser);

        captchaGenerateRateLimitViaIpAddress = cacheManager.getCache(Fields.captchaGenerateRateLimitViaIpAddress);
        captchaGenerateRateLimitViaLoginId = cacheManager.getCache(Fields.captchaGenerateRateLimitViaLoginId);

        captchaCheckRateLimitViaIpAddress = cacheManager.getCache(Fields.captchaCheckRateLimitViaIpAddress);
        captchaCheckRateLimitViaLoginId = cacheManager.getCache(Fields.captchaCheckRateLimitViaLoginId);

        fieldNotNullChecker.check(captchaCheckRateLimitViaIpAddress, Fields.captchaCheckRateLimitViaIpAddress);
        fieldNotNullChecker.check(captchaCheckRateLimitViaLoginId, Fields.captchaCheckRateLimitViaLoginId);
        fieldNotNullChecker.check(captchaGenerateRateLimitViaIpAddress, Fields.captchaGenerateRateLimitViaIpAddress);
        fieldNotNullChecker.check(captchaGenerateRateLimitViaLoginId, Fields.captchaGenerateRateLimitViaLoginId);
    }

    protected <Key, Value> Value justGetCache(Key key, Cache<Key, Value> cache, Supplier<Value> factory) {
        Value obj = cache.get(key);
        if (obj == null) {
            cache.putIfAbsent(key, factory.get());
            obj = cache.get(key);
        }

        return obj;
    }

    protected long gcd(long a, long b) {
        long k;
        do {
            k = a % b;
            a = b;
            b = k;
        } while (k != 0);

        return a;
    }

    protected Bucket generateBucket(long frequenceInMinute) {
        long k = gcd(frequenceInMinute, 60);

        Refill refill = Refill.intervally(frequenceInMinute / k, Duration.ofSeconds(60 / k));
        Bandwidth limit = Bandwidth.classic(frequenceInMinute, refill);

        return Bucket.builder().addLimit(limit).build();
    }

    protected String getLoginId() {
        try {
            return StpUtil.getLoginId().toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        String requestPath = request.getServletPath();

        if (requestPath.startsWith("/captcha/generate")) {
            Bucket bucket = null;

            String loginId = getLoginId();
            if (loginId != null) {
                bucket = justGetCache(loginId, captchaGenerateRateLimitViaIpAddress, () -> generateBucket(30));
            } else {
                bucket = justGetCache(requestAnalyser.getIpAddress(request), captchaGenerateRateLimitViaLoginId, () -> generateBucket(20));
            }

            return checkBucket(response, bucket);

        } else if (requestPath.startsWith("/captcha/check")) {
            Bucket bucket = null;

            String loginId = getLoginId();
            if (loginId != null) {
                bucket = justGetCache(loginId, captchaCheckRateLimitViaLoginId, () -> generateBucket(30));
            } else {
                bucket = justGetCache(requestAnalyser.getIpAddress(request), captchaCheckRateLimitViaIpAddress, () -> generateBucket(20));
            }

            return checkBucket(response, bucket);
        }

        return true;
    }

    protected boolean checkBucket(@Nonnull HttpServletResponse response, Bucket bucket) throws IOException {
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION, JsonApiResult.FAILED_FREQUENCY_MAX);
            long nanosTime2Fefill = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
            responseData.setData(Map.of("coolDownRemainder", nanosTime2Fefill / 1000000));

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
