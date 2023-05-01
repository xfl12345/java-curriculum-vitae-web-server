package cc.xfl12345.person.cv.pojo;

import cloud.tianai.captcha.resource.AbstractResourceProvider;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedUrlResourceProvider extends AbstractResourceProvider {
    public static final String NAME = "CachedURL";

    protected Map<String, URL> urlMap = null;

    @PostConstruct
    public void init() {
        urlMap = new ConcurrentHashMap<>();
    }

    public Map<String, URL> getUrlMap() {
        return Collections.unmodifiableMap(urlMap);
    }

    public URL putURL(String key, URL value) {
        return urlMap.put(key, value);
    }

    public URL removeURL(String key) {
        return urlMap.remove(key);
    }

    public void putAllURL(Map<? extends String, ? extends URL> m) {
        urlMap.putAll(m);
    }

    public void clearURL() {
        urlMap.clear();
    }

    @Override
    public InputStream doGetResourceInputStream(Resource data) {
        URL url = urlMap.get(data.getData());
        if (url != null) {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    @Override
    public boolean supported(String type) {
        return NAME.equals(type);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
