package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.interceptor.AdminAuthInterceptor;
import cc.xfl12345.person.cv.interceptor.AllRequestInterceptor;
import cc.xfl12345.person.cv.interceptor.AuthInterceptor;
import cc.xfl12345.person.cv.interceptor.RateLimitInterceptor;
import cc.xfl12345.person.cv.pojo.AnyUserRequestRateLimitHelperFactory;
import cc.xfl12345.person.cv.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Bean
    public RateLimitInterceptor rateLimitInterceptor(
        ObjectMapper objectMapper,
        AnyUserRequestRateLimitHelperFactory factory) {

        RateLimitInterceptor interceptor = new RateLimitInterceptor();
        interceptor.setObjectMapper(objectMapper);
        interceptor.setAnyUserRequestRateLimitHelperFactory(factory);

        return interceptor;
    }

    @Bean
    public AuthInterceptor authInterceptor(ObjectMapper objectMapper) {
        AuthInterceptor interceptor = new AuthInterceptor();
        interceptor.setObjectMapper(objectMapper);

        return interceptor;
    }

    @Bean
    public AdminAuthInterceptor adminAuthInterceptor(ObjectMapper objectMapper) {
        AdminAuthInterceptor interceptor = new AdminAuthInterceptor();
        interceptor.setObjectMapper(objectMapper);

        return interceptor;
    }

    @Bean
    public AllRequestInterceptor allRequestInterceptor(UserService userService) {
        AllRequestInterceptor interceptor = new AllRequestInterceptor();
        interceptor.setUserService(userService);

        return interceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(applicationContext.getBean(AllRequestInterceptor.class)).addPathPatterns("/**");
        // 注册 访问频率限制 拦截器 （防止人机验证被爆）
        registry.addInterceptor(applicationContext.getBean(RateLimitInterceptor.class)).addPathPatterns("/**");
        // 注册 隐私数据 拦截器 （要求用户登录）
        registry.addInterceptor(applicationContext.getBean("authInterceptor", AuthInterceptor.class)).addPathPatterns("/static/secret/**");
        // 管理员级别权限拦截
        registry.addInterceptor(applicationContext.getBean(AdminAuthInterceptor.class)).addPathPatterns("/app/**");
        registry.addInterceptor(applicationContext.getBean(AdminAuthInterceptor.class)).addPathPatterns("/db/**");
    }


}
