package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.interceptor.RateLimitInterceptor;
import cc.xfl12345.person.cv.interceptor.SecretDataRequestInterceptor;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.cache.CacheManager;

@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @DependsOn("bucket4jCacheResolver")
    @Bean
    public RateLimitInterceptor rateLimitInterceptor(
        ObjectMapper objectMapper,
        CacheManager cacheManager,
        RequestAnalyser requestAnalyser) {

        RateLimitInterceptor interceptor = new RateLimitInterceptor();
        interceptor.setObjectMapper(objectMapper);
        interceptor.setCacheManager(cacheManager);
        interceptor.setRequestAnalyser(requestAnalyser);

        return interceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 访问频率限制 拦截器 （防止人机验证被爆）
        registry.addInterceptor(applicationContext.getBean(RateLimitInterceptor.class)).addPathPatterns("/**");
        // 注册 隐私数据 拦截器 （要求用户登录）
        registry.addInterceptor(new SecretDataRequestInterceptor()).addPathPatterns("/static/secret/**");
    }


}
