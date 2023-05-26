package cc.xfl12345.person.cv.pojo;

import jakarta.annotation.Nonnull;

import javax.cache.CacheManager;

public class AnyUserRequestRateLimitHelperFactory {

    protected RequestAnalyser requestAnalyser;

    protected CacheManager cacheManager;

    public RequestAnalyser getRequestAnalyser() {
        return requestAnalyser;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public AnyUserRequestRateLimitHelperFactory(
        @Nonnull CacheManager cacheManager, @Nonnull RequestAnalyser requestAnalyser) {

        this.requestAnalyser = requestAnalyser;
        this.cacheManager = cacheManager;

    }

    public AnyUserRequestRateLimitHelper generate(
        String cacheNamePrefix,
        SimpleBucketConfig ipAddressBucketConfig,
        SimpleBucketConfig loginIdBucketConfig) {

        return new AnyUserRequestRateLimitHelper(
            cacheNamePrefix,
            cacheManager,
            requestAnalyser,
            ipAddressBucketConfig,
            loginIdBucketConfig
        );
    }

}
