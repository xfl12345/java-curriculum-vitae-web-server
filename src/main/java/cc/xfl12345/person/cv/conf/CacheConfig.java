package cc.xfl12345.person.cv.conf;


import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnBean(CachingProvider.class)
    public CacheManager cacheManager(CachingProvider cachingProvider) {
        return cachingProvider.getCacheManager();
    }

}
