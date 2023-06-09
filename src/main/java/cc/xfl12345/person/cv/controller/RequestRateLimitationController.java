package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.AppConst;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.CacheManager;

@RestController
public class RequestRateLimitationController {

    protected CacheManager cacheManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("rate-limit/clear-all")
    public boolean clearAllCache() {
        if (StpUtil.isLogin() && AppConst.XFL_WEBUI_ADMIN_LOGIN_ID.equals(StpUtil.getLoginId().toString())) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                if (cacheName.startsWith("RateLimit")) {
                    cacheManager.getCache(cacheName).clear();
                }
            });

            return true;
        }

        return false;
    }

}
