package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelperFactory;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.cache.CacheManager;

@Configuration
public class AppConfig {

    @Bean
    public RequestAnalyser requestAnalyser() {
        return new RequestAnalyser();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @DependsOn("bucket4jCacheResolver")
    @Bean
    public AnyUserRequestRateLimitHelperFactory anyUserRequestRateLimitHelperFactory(
        CacheManager cacheManager,
        RequestAnalyser requestAnalyser
    ) {
        return new AnyUserRequestRateLimitHelperFactory(cacheManager, requestAnalyser);
    }

}
