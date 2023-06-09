package cc.xfl12345.person.cv.pojo;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

import javax.cache.CacheManager;

public class AnyUserRequestRateLimitHelper {

    protected RequestAnalyser requestAnalyser;

    // ipAddress -> bucket
    protected RateLimitHelper limitViaIpAddress;

    // loginId -> bucket
    protected RateLimitHelper limitViaLoginId;

    public AnyUserRequestRateLimitHelper(
        @Nonnull String cacheNameSuffix,
        @Nonnull CacheManager cacheManager,
        @Nonnull RequestAnalyser requestAnalyser,
        SimpleBucketConfig ipAddressBucketConfig,
        SimpleBucketConfig loginIdBucketConfig) {
        String limitViaIpAddressCacheName = "RateLimitViaIpAddress4" + cacheNameSuffix;
        String limitViaLoginIdCacheName = "RateLimitViaLoginId4" + cacheNameSuffix;

        this.requestAnalyser = requestAnalyser;

        limitViaIpAddress = new RateLimitHelper(
            cacheManager.getCache(limitViaIpAddressCacheName),
            ipAddressBucketConfig
        );

        limitViaLoginId = new RateLimitHelper(
            cacheManager.getCache(limitViaLoginIdCacheName),
            loginIdBucketConfig
        );
    }

    protected String getLoginId() {
        try {
            return StpUtil.getLoginId().toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void addTokens(HttpServletRequest request) {
        String loginId = getLoginId();
        if (loginId != null) {
            limitViaLoginId.addTokens(loginId);
        } else {
            limitViaIpAddress.addTokens(requestAnalyser.getIpAddress(request));
        }
    }

    public boolean canConsume(HttpServletRequest request) {
        String loginId = getLoginId();
        if (loginId != null) {
            return limitViaLoginId.canConsume(loginId);
        } else {
            return limitViaIpAddress.canConsume(requestAnalyser.getIpAddress(request));
        }
    }


    public RateLimitHelper.ConsumeResult tryConsume(HttpServletRequest request) {
        String loginId = getLoginId();
        if (loginId != null) {
            return limitViaLoginId.tryConsume(loginId);
        } else {
            return limitViaIpAddress.tryConsume(requestAnalyser.getIpAddress(request));
        }
    }

}
