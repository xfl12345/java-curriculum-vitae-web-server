package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.pojo.WebResourceMapping;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Configuration
@ConfigurationProperties(prefix = "app.webui")
@Slf4j
public class WebResourceConfig implements WebMvcConfigurer {

    @Getter
    @Setter
    protected List<WebResourceMapping> springResources = Collections.emptyList();

    @Getter
    @Setter
    protected PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public void justMapResource(ResourceHandlerRegistry registry, String pathPattern, Resource resource) throws IOException {
        registry.addResourceHandler(pathPattern).addResourceLocations(resource);
        log.info(String.format("Mapping request: [%s] <---> [%s], resource type=[%s]", pathPattern, resource.getURL(), resource.getClass().getCanonicalName()));
    }

    public void justMapResource(ResourceHandlerRegistry registry, List<String> pathPatternList, Resource resource) throws IOException {
        String[] pathPatternArray = pathPatternList.toArray(String[]::new);
        registry.addResourceHandler(pathPatternArray).addResourceLocations(resource);
        log.info(String.format("Mapping request: %s <---> [%s], resource type=[%s]", pathPatternList, resource.getURL(), resource.getClass().getCanonicalName()));
    }

    public void addWebResourceMapping2Registry(ResourceHandlerRegistry registry, WebResourceMapping mapping) throws IOException {
        PathMatcher pathMatcher = resourcePatternResolver.getPathMatcher();
        String pathPattern = Objects.requireNonNull(mapping.getPathPattern());
        String resourceLocation = Objects.requireNonNull(mapping.getResourceLocation());
        // 支持相对引用
        Resource resource = resourceLocation.startsWith("./") ? new FileSystemResource(resourceLocation) : resourcePatternResolver.getResource(resourceLocation);
        // 如果拦截的是根路径，需要遍历路径下所有文件（因为会导致其它路径无法访问）。
        if ("/**".equals(pathPattern) || "/".equals(pathPattern)) {
            FileSystemResource fileSystemResource = new FileSystemResource(resourceLocation);
            if (fileSystemResource.exists()) {
                String resourceLocationPattern = fileSystemResource.getURL() + "**";
                Resource[] resources = resourcePatternResolver.getResources(resourceLocationPattern);
                List<String> pathPatternList = new ArrayList<>(resources.length);
                for (Resource item : resources) {
                    String currentPathPattern = pathMatcher.extractPathWithinPattern(resourceLocationPattern, item.getURL().toString());
                    if (currentPathPattern.length() == 0 || currentPathPattern.charAt(0) != '/') {
                        currentPathPattern = "/" + currentPathPattern;
                    }
                    pathPatternList.add(currentPathPattern);
                }

                justMapResource(registry, pathPatternList, resource);
            } else {
                log.info(String.format("Skip mapping request: [%s] <---> [%s], resource type=[%s]. Because of the resource do not exist!", pathPattern, resource.getURL(), resource.getClass().getCanonicalName()));
            }
        } else {
            justMapResource(registry, pathPattern, resource);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            for (WebResourceMapping mapping : springResources) {
                addWebResourceMapping2Registry(registry, mapping);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
