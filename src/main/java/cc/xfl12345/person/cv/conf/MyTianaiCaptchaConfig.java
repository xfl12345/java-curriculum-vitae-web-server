package cc.xfl12345.person.cv.conf;

import cc.xfl12345.person.cv.pojo.CachedUrlResourceProvider;
import cc.xfl12345.person.cv.utility.ClassPathResourceUtils;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.generator.common.constant.SliderCaptchaConstant;
import cloud.tianai.captcha.generator.impl.StandardSliderImageCaptchaGenerator;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.impl.DefaultResourceStore;
import cloud.tianai.captcha.resource.impl.provider.ClassPathResourceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cloud.tianai.captcha.generator.impl.StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH;

@Configuration
@Slf4j
public class MyTianaiCaptchaConfig {
    @Bean
    public CachedUrlResourceProvider cachedUrlResourceProvider() {
        return new CachedUrlResourceProvider();
    }

    @Bean
    public ResourceStore resourceStore(CachedUrlResourceProvider cachedUrlResourceProvider) throws IOException {
        // 滑块验证码 模板 (系统内置)
        Map<String, Resource> template1 = new HashMap<>(4);
        template1.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/active.png")));
        template1.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/fixed.png")));
        template1.put(SliderCaptchaConstant.TEMPLATE_MATRIX_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/1/matrix.png")));
        Map<String, Resource> template2 = new HashMap<>(4);
        template2.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/active.png")));
        template2.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/fixed.png")));
        template2.put(SliderCaptchaConstant.TEMPLATE_MATRIX_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/2/matrix.png")));
        // 旋转验证码 模板 (系统内置)
        Map<String, Resource> template3 = new HashMap<>(4);
        template3.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/3/active.png")));
        template3.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/3/fixed.png")));
        template3.put(SliderCaptchaConstant.TEMPLATE_MATRIX_IMAGE_NAME, new Resource(ClassPathResourceProvider.NAME, StandardSliderImageCaptchaGenerator.DEFAULT_SLIDER_IMAGE_TEMPLATE_PATH.concat("/3/matrix.png")));

        DefaultResourceStore resourceStore = new DefaultResourceStore();

        // 1. 添加一些模板
        resourceStore.addTemplate(CaptchaTypeConstant.SLIDER, template1);
        resourceStore.addTemplate(CaptchaTypeConstant.SLIDER, template2);
        resourceStore.addTemplate(CaptchaTypeConstant.ROTATE, template3);

        String pictureClassPath = "cc/xfl12345/person/cv/picture/";

        Map<String, URL> urlMap = ClassPathResourceUtils.getURL(
            pictureClassPath
        ).values().parallelStream().reduce(new ConcurrentHashMap<>(), (mergedMap, map) -> {
            map.entrySet().parallelStream().forEach(item -> mergedMap.put(item.getKey(), item.getValue()));
            return mergedMap;
        });

        urlMap.remove(pictureClassPath);
        cachedUrlResourceProvider.putAllURL(urlMap);
        urlMap.keySet().forEach(url -> {
            log.info("TianaiCaptcha adding classpath resource:[" + url + ']');
            // 2. 添加自定义背景图片
            resourceStore.addResource(
                CaptchaTypeConstant.SLIDER,
                new Resource(CachedUrlResourceProvider.NAME, url)
            );
            resourceStore.addResource(
                CaptchaTypeConstant.ROTATE,
                new Resource(CachedUrlResourceProvider.NAME, url)
            );
        });

        return resourceStore;
    }

    @Bean
    public ImageCaptchaResourceManager imageCaptchaResourceManager(CachedUrlResourceProvider resourceProvider, ResourceStore resourceStore) {
        ImageCaptchaResourceManager manager = new DefaultImageCaptchaResourceManager(resourceStore);
        manager.registerResourceProvider(resourceProvider);
        return manager;
    }
}
