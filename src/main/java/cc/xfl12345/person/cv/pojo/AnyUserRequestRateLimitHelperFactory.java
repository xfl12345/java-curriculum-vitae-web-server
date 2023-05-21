package cc.xfl12345.person.cv.pojo;

import jakarta.annotation.Nonnull;

import javax.cache.CacheManager;

public class AnyUserRequestRateLimitHelperFactory {

    protected RequestAnalyser requestAnalyser;

    protected CacheManager cacheManager;

    public AnyUserRequestRateLimitHelperFactory(
        @Nonnull CacheManager cacheManager, @Nonnull RequestAnalyser requestAnalyser) {

        this.requestAnalyser = requestAnalyser;
        this.cacheManager = cacheManager;

    }

    public AnyUserRequestRateLimitHelper generate(
        String cacheNamePrefix,
        long ipAddressFrequencyInMinute,
        long loginIdFrequencyInMinute) {

        return new AnyUserRequestRateLimitHelper(
            cacheNamePrefix,
            cacheManager,
            requestAnalyser,
            ipAddressFrequencyInMinute,
            loginIdFrequencyInMinute
        );
    }

}
