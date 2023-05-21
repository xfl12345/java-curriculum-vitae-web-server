package cc.xfl12345.person.cv.conf;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@ConditionalOnProperty(prefix = "app.webui", name = "resource-location")
@ConfigurationProperties(prefix = "app.webui")
@Slf4j
public class WebResourceConfig implements WebMvcConfigurer {
    @Getter
    @Setter
    protected String servletPath = "static/";

    @Getter
    @Setter
    protected String resourceLocation;

    protected Resource resource;

    public String getPathPattern() {
        return "/" + servletPath + "**";
    }

    @PostConstruct
    public void init() throws IOException {
        if (resourceLocation.startsWith("classpath")) {
            resource = new ClassPathResource(resourceLocation);
        } else {
            if (!"".equals(resourceLocation)) {
                try {
                    resource = new UrlResource(new URL(resourceLocation));
                } catch (MalformedURLException e) {
                    log.info("[" + resourceLocation + "] is not a URL link. [java.net.URL]: " + e.getMessage());
                }
            }
            if (resource == null) {
                resource = new FileSystemResource(resourceLocation);
            }
        }

        log.info("Mapping request: [" + getPathPattern() + "] <---> [" + resource.getURL() + "]");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(getPathPattern()).addResourceLocations(resource);
    }
}
