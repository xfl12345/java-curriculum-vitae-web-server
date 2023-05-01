package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.interceptor.ApiRequestInterceptor;
import cc.xfl12345.person.cv.interceptor.SecretDataRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcInterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // registry.addInterceptor(new SecretDataRequestInterceptor()).addPathPatterns("/static/secret/**");
    }
}
