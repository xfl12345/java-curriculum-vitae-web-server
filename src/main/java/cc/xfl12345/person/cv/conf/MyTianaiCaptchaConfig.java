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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    // public Set<String> getClassPathResource(String path) throws IOException {
    //     // Set<String> pictureRelatvieClassPathSet = ClassPathResourceUtils.getURL(
    //     //     pictureClassPath,
    //     //     null,
    //     //     null,
    //     //     true
    //     // ).values().parallelStream().reduce(new ConcurrentHashMap<>(), (mergedMap, map) -> {
    //     //     mergedMap.putAll(map);
    //     //     return mergedMap;
    //     // }).keySet();
    //     // pictureRelatvieClassPathSet.remove("");
    //
    //     Set<String> resourceSet = ClassPathResourceUtils.listPath2File(
    //         path,
    //         null,
    //         null,
    //         true
    //     ).values().parallelStream().reduce((mergedSet, set) -> {
    //         mergedSet.addAll(set);
    //         return mergedSet;
    //     }).orElse(Collections.emptySet());
    //     resourceSet.remove("");
    //
    //     return resourceSet;
    // }

    // public Map<String, URL> getResourceUrlMap(String path) throws IOException {
    //     return ClassPathResourceUtils.getURL(
    //         path
    //     ).values().parallelStream().reduce(new ConcurrentHashMap<>(), (mergedMap, map) -> {
    //         map.values().parallelStream().forEach(url -> mergedMap.put(url.toString(), url));
    //         return mergedMap;
    //     });
    // }

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

        // Map<String, URL> urlMap = ClassPathResourceUtils.getURL(
        //     pictureClassPath
        // ).values().parallelStream().reduce(new ConcurrentHashMap<>(), (mergedMap, map) -> {
        //     mergedMap.putAll(map);
        //     return mergedMap;
        // });
        // Map<String, URL> tmpUrlMap = new HashMap<>(urlMap.size());
        // for (Map.Entry<String, URL> kv: urlMap.entrySet()){
        //     FileObject fileObject = ramFileSystem.resolveFile(pictureClassPath + kv.getKey());
        //     fileObject.createFile();
        //     FileContent fileContent = fileObject.getContent();
        //     OutputStream outputStream = fileContent.getOutputStream();
        //     try (outputStream) {
        //         IOUtils.copy(kv.getValue(), outputStream);
        //     }
        //     URL ramFileURL = fileObject.getURL();
        //     tmpUrlMap.put(ramFileURL.toString(), ramFileURL);
        // }
        // urlMap = tmpUrlMap;

        Map<String, URL> urlMap = ClassPathResourceUtils.getURL(
            pictureClassPath
        ).values().parallelStream().reduce(new ConcurrentHashMap<>(), (mergedMap, map) -> {
            map.values().parallelStream().forEach(url -> mergedMap.put(url.toString(), url));
            return mergedMap;
        });
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
    @ConditionalOnMissingBean
    public ImageCaptchaResourceManager imageCaptchaResourceManager(CachedUrlResourceProvider resourceProvider, ResourceStore resourceStore) {
        ImageCaptchaResourceManager manager = new DefaultImageCaptchaResourceManager(resourceStore);
        manager.registerResourceProvider(resourceProvider);
        return manager;
    }
}
