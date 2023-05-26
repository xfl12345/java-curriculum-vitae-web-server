package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.appconst.AppConst;
import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.SmsTask;
import cc.xfl12345.person.cv.pojo.XflSmsConfig;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.*;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheWriter;
import javax.cache.spi.CachingProvider;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@DependsOn(ControllerConst.dependsOnBean)
@Service("SmsService")
public class SmsService extends TextWebSocketHandler {

    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected XflSmsConfig xflSmsConfig;

    protected String namedSmsTemplate = "%s";

    @Autowired
    public void setMySmsConfig(XflSmsConfig xflSmsConfig) {
        this.xflSmsConfig = xflSmsConfig;
    }

    protected CacheManager cacheManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected Cache<String, String> smsValidationCodeCache;

    public Cache<String, String> getSmsValidationCodeCache() {
        return smsValidationCodeCache;
    }

    @PostConstruct
    public void init() {
        namedSmsTemplate = String.format("【%s】", xflSmsConfig.getSignName()) + String.format(xflSmsConfig.getTemplate(), "%s", xflSmsConfig.getVerificationCodeLength(),  xflSmsConfig.getExpirationInMinute());

        CacheConfigurationBuilder<String, String> cacheConfigurationBuilder = CacheConfigurationBuilder
            .newCacheConfigurationBuilder(
                String.class,
                String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(50, MemoryUnit.MB)
            )
            .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(xflSmsConfig.getExpirationInMinute())));

        Configuration<String, String> configuration =
            Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfigurationBuilder);

        smsValidationCodeCache = cacheManager.createCache("smsVerificationCode", configuration);
    }

    public String generateVerificationCode(int codeLength) {
        StringBuilder verificationCodeBuffer = new StringBuilder(codeLength);
        Random random = new Random(System.currentTimeMillis());
        byte[] commonHash = BigInteger.valueOf(
            // 取低 16 位
            (long) ((short) random.hashCode()) |
                // 取低 16 位 复制到 17~32 位
                (((long) ((short) verificationCodeBuffer.hashCode())) << 16) |
                // 取高 16 位 复制到 33~48 位
                (((long) ((short) (verificationCodeBuffer.hashCode() >> 16))) << 32) |
                // 取高 16 位 复制到 49~64 位
                (((long) ((short) (random.hashCode() >> 16))) << 48)
        ).toByteArray();
        long hash = 1315423911 + random.nextLong();
        byte takeNum;
        // 以下哈希算法我自己都不清楚发生碰撞的概率，但只求尽可能地难以被预测
        for (int i = 0, finishCodeCount = 0; finishCodeCount < codeLength; finishCodeCount++) {
            if (random.nextBoolean()) {// Justin Sobel写的一个 位操作 的哈希函数
                hash ^= ((hash << 5) + commonHash[i] + (hash >> 2));
            } else {// 变体算法
                hash ^= ((hash << 4) + commonHash[i] + (hash >> 1));
            }
            // 瞎写
            takeNum = (byte) ((hash & 0xFF)
                - ((hash & 0xFF00) >> 8)
                + ((hash & 0xFF0000) >> 16)
                - ((hash & 0xFF000000L) >> 24));
            String str = String.valueOf(Math.abs(takeNum % 9));
            verificationCodeBuffer.append(str);
            if (i < commonHash.length - 1)
                i++;
            else
                i = 0;
        }

        return verificationCodeBuffer.toString();
    }

    public boolean sendSmsValidationCode(String phoneNumber) throws IOException {
        Iterator<WebSocketSession> iterator = webSocketSessionMaps.values().iterator();
        if (iterator.hasNext()) {
            String code = generateVerificationCode(xflSmsConfig.getVerificationCodeLength());

            SmsTask smsTask = new SmsTask();
            smsTask.setPhoneNumber(phoneNumber);
            smsTask.setSmsContent(String.format(namedSmsTemplate, code));

            WebSocketSession webSocketSession = iterator.next();
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(smsTask)));

            // 发完了才开始倒计时
            smsValidationCodeCache.put(phoneNumber, code);

            return true;
        }

        return false;
    }


    /**
     * 固定前缀
     */
    private static final String CLOUD_SMS_SERVER_ID = "cloud_sms_server_id_";

    /**
     * 存放Session集合，方便推送消息
     */
    protected Map<String, WebSocketSession> webSocketSessionMaps = new ConcurrentHashMap<>();

    // 监听：连接开启
    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        String loginId = session.getAttributes().get("loginId").toString();
        // 仅限特殊用户身份连接
        boolean isAuthUser = AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID.equals(loginId);
        if (webSocketSessionMaps.size() > 0 || !isAuthUser) {
            JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
            if (isAuthUser) {
                responseData.setApiResult(JsonApiResult.FAILED);
                responseData.setMessage("忙线");
            } else {
                responseData.setApiResult(JsonApiResult.FAILED_FORBIDDEN);
            }
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseData)));
            session.close();
        } else {
            // put到集合，方便后续操作
            webSocketSessionMaps.put(CLOUD_SMS_SERVER_ID + loginId, session);
            // 给个提示
            String tips = "Web-Socket 连接成功，sid=" + session.getId() + "，loginId=" + loginId;
            log.debug(tips);
        }

    }


    // 监听：连接关闭
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        if (status.equalsCode(CloseStatus.NORMAL)) {
            // 从集合移除
            String loginId = session.getAttributes().get("loginId").toString();
            webSocketSessionMaps.remove(CLOUD_SMS_SERVER_ID + loginId);
            // StpUtil.logout(loginId);

            // 给个提示
            String tips = "Web-Socket 连接关闭，sid=" + session.getId() + "，loginId=" + loginId;
            log.debug(tips);
        }
    }

    // 收到消息
    @Override
    public void handleTextMessage(WebSocketSession session, @Nonnull TextMessage message) throws IOException {
        log.debug("sid为：" + session.getId() + "，发来：" + message);
    }

}
