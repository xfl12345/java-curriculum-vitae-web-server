package cc.xfl12345.person.cv.conf;

import org.apache.catalina.core.StandardContext;
import org.apache.coyote.AbstractProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        // 预留 5 秒给 StandardContext 正常关闭
        return context -> {
            if (context instanceof StandardContext standardContext) {
                standardContext.setUnloadDelay(5000);
            }
        };
    }

    @Bean
    public TomcatConnectorCustomizer tomcatConnectorCustomizer() {
        // 连接超时设置为 20秒
        return connector -> {
            if (connector.getProtocolHandler() instanceof AbstractProtocol<?> abstractProtocol) {
                abstractProtocol.setConnectionTimeout(20000);
            }
        };
    }
}
