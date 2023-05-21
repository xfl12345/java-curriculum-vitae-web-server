package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public RequestAnalyser requestAnalyser() {
        return new RequestAnalyser();
    }

}
