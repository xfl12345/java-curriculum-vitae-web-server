package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelperFactory;
import cc.xfl12345.person.cv.pojo.XflSmsConfig;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;

@Configuration
public class AppConfig {

    @Bean
    public RequestAnalyser requestAnalyser() {
        return new RequestAnalyser();
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public AnyUserRequestRateLimitHelperFactory anyUserRequestRateLimitHelperFactory(
        CacheManager cacheManager,
        RequestAnalyser requestAnalyser
    ) {
        return new AnyUserRequestRateLimitHelperFactory(cacheManager, requestAnalyser);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "app.sms.xfl12345", name = "enabled")
    @ConfigurationProperties(prefix = "app.sms.xfl12345")
    public XflSmsConfig mySmsConfig() {
        return new XflSmsConfig();
    }


}
