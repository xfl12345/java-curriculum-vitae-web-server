package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.*;
import cc.xfl12345.person.cv.pojo.database.MeetHr;
import cc.xfl12345.person.cv.pojo.request.BaseRequestObject;
import cc.xfl12345.person.cv.pojo.request.payload.PhoneNumberData;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
import javax.cache.configuration.Configuration;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@DependsOn(ControllerConst.dependsOnBean)
@Service("SmsService")
public class SMS extends TextWebSocketHandler {

    public enum SendValidationCodeResult {
        SUCCESS,
        FAILED,
        NOT_AVAILABLE,
        ALL_ERROR
    }

    protected ObjectMapper objectMapper;

    protected XflSmsConfig xflSmsConfig;

    protected String namedSmsTemplate = "%s";

    protected CacheManager cacheManager;

    protected Cache<String, String> smsValidationCodeCache;

    protected UserService userService;

    private AnyUserRequestRateLimitHelperFactory rateLimitHelperFactory;

    private AnyUserRequestRateLimitHelper pullSmsValidationCodeRateLimitHelper = null;

    /**
     * 存放Session集合，方便推送消息
     */
    protected Map<String, WebSocketSession> webSocketSessionMaps = new ConcurrentHashMap<>();

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setMySmsConfig(XflSmsConfig xflSmsConfig) {
        this.xflSmsConfig = xflSmsConfig;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Cache<String, String> getSmsValidationCodeCache() {
        return smsValidationCodeCache;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRateLimitHelperFactory(AnyUserRequestRateLimitHelperFactory rateLimitHelperFactory) {
        this.rateLimitHelperFactory = rateLimitHelperFactory;
    }

    public Map<String, WebSocketSession> getWebSocketSessionMaps() {
        return webSocketSessionMaps;
    }

    @PostConstruct
    public void init() {
        pullSmsValidationCodeRateLimitHelper = rateLimitHelperFactory.generate(
            "PullSmsValidationCode",
            SimpleBucketConfigUtils.createConfigJustInMinutes(1),
            SimpleBucketConfigUtils.createConfigJustInMinutes(1)
        );

        namedSmsTemplate = String.format("【%s】", xflSmsConfig.getSignName()) + String.format(xflSmsConfig.getTemplate(), "%s", xflSmsConfig.getValidationCodeLength(), xflSmsConfig.getExpirationInMinute());

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

    @PreDestroy
    public void destroy() {
        for (String loginId : webSocketSessionMaps.keySet()) {
            try {
                WebSocketSession session = webSocketSessionMaps.get(loginId);
                session.close();
            } catch (IOException e) {
                log.error(String.format("Closing websocket of loginId [%s] failed due to ", loginId), e);
            }
        }
    }


    public boolean justAddValidationCode2Cache(String phoneNumber, String code) {
        smsValidationCodeCache.put(phoneNumber, code);

        return true;
    }

    public String justGetValidationCodeAndPutIntoCache(String phoneNumber) {
        String code = generateValidationCode();
        smsValidationCodeCache.put(phoneNumber, code);

        return code;
    }

    public String generateValidationCode(int codeLength) {
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

    public String generateValidationCode() {
        return generateValidationCode(xflSmsConfig.getValidationCodeLength());
    }

    public SendValidationCodeResult sendValidationCode(String phoneNumber) {
        Iterator<WebSocketSession> iterator = webSocketSessionMaps.values().iterator();
        if (iterator.hasNext()) {
            String code = generateValidationCode();

            SmsTask smsTask = new SmsTask();
            smsTask.setCreateTime(ZonedDateTime.now(TimeZone.getDefault().toZoneId()).format(DateTimeFormatter.ISO_INSTANT));
            smsTask.setPhoneNumber(phoneNumber);
            smsTask.setValidationCode(code);
            smsTask.setSmsContent(String.format(namedSmsTemplate, code));

            BaseRequestObject requestObject = new BaseRequestObject();
            requestObject.operation = "sendSms";
            requestObject.data = smsTask;

            WebSocketMessage message = new WebSocketMessage();
            message.setMessageType(WebSocketMessage.Type.request);
            message.setPayload(requestObject);

            while (iterator.hasNext()) {
                WebSocketSession webSocketSession = iterator.next();
                try {
                    webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                    // 发完了才开始倒计时
                    smsValidationCodeCache.put(phoneNumber, code);

                    return SendValidationCodeResult.SUCCESS;
                } catch (IOException e) {
                    log.error("Sending validation code to SMS server failed due to ", e);
                }
            }

            return SendValidationCodeResult.ALL_ERROR;
        } else {
            return SendValidationCodeResult.NOT_AVAILABLE;
        }
    }

    public JsonApiResponseData sendValidationCode(HttpServletRequest request, PhoneNumberData phoneNumberData) {
        LocalDateTime date = LocalDateTime.now();
        JsonApiResponseData responseDataPayload = new JsonApiResponseData(JsonApiConst.VERSION, JsonApiResult.FAILED_NOT_SUPPORT);
        if (phoneNumberData.phoneNumber != null) {
            String phoneNumber = phoneNumberData.phoneNumber;
            // 判断是否是受邀面试官
            MeetHr meetHr = userService.getHrInfoAndUpdateVisitTime(phoneNumber, date);
            if (meetHr != null) {
                RateLimitHelper.ConsumeResult consumeResult = pullSmsValidationCodeRateLimitHelper.tryConsume(request);
                if (consumeResult.isSuccess()) {
                    SendValidationCodeResult sendResult = sendValidationCode(phoneNumber);
                    switch (sendResult) {
                        case SUCCESS -> {
                            responseDataPayload.setApiResult(JsonApiResult.SUCCEED);
                            responseDataPayload.setData(Map.of(
                                JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()
                            ));
                        }
                        case FAILED, NOT_AVAILABLE, ALL_ERROR -> {
                            responseDataPayload.setApiResult(
                                SendValidationCodeResult.ALL_ERROR.equals(sendResult)
                                    ? JsonApiResult.OTHER_FAILED
                                    : JsonApiResult.FAILED
                            );
                            responseDataPayload.setMessage("后台短信服务不可用，请联系站长修复。");
                            pullSmsValidationCodeRateLimitHelper.addTokens(request);
                        }
                    }
                } else {
                    responseDataPayload.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
                    responseDataPayload.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));
                }
            } else {
                responseDataPayload.setApiResult(JsonApiResult.FAILED_FORBIDDEN);
                responseDataPayload.setMessage("您好，您的权限不足，可联系站长成为受邀面试官。");
            }
        } else {
            responseDataPayload.setApiResult(JsonApiResult.FAILED_REQUEST_FORMAT_ERROR);
        }

        return responseDataPayload;
    }

    public boolean closeSessionByLoginId(String loginId) {
        WebSocketSession session = webSocketSessionMaps.get(loginId);
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                // 无论如何都要注销
                StpUtil.logout(loginId);
            }

            return true;
        }

        return false;
    }

    // 监听：连接开启
    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws Exception {
        String loginId = session.getAttributes().get("loginId").toString();
        // put到集合，方便后续操作
        WebSocketSession oldSession = webSocketSessionMaps.put(loginId, session);
        try {
            if (oldSession != null) {
                oldSession.close();
            }
        } catch (Exception e) {
            // ignore
        }
        // 给个提示
        String tips = "Web-Socket 连接成功，sid=" + session.getId() + "，loginId=" + loginId;
        log.debug(tips);
        log.debug("当前 SMS hashcode=" + this.hashCode());
    }


    // 监听：连接关闭
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) throws Exception {
        // 从集合移除
        String loginId = session.getAttributes().get("loginId").toString();
        webSocketSessionMaps.remove(loginId);

        if (status.equalsCode(CloseStatus.NORMAL) || status.equalsCode(CloseStatus.GOING_AWAY)) {
            StpUtil.logout(loginId);
        }

        // 给个提示
        String tips = "Web-Socket 连接关闭，sid=" + session.getId() + "，loginId=" + loginId + ", statusCode=" + status.getCode();
        log.debug(tips);
    }

    // 收到消息
    @Override
    public void handleTextMessage(WebSocketSession session, @Nonnull TextMessage message) throws IOException {
        log.debug("sid为：" + session.getId() + "，发来：" + message);
    }

}
