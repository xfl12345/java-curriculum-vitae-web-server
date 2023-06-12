package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.AppConst;
import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.RateLimitHelper;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import cc.xfl12345.person.cv.pojo.SimpleBucketConfig;
import cc.xfl12345.person.cv.pojo.database.MeetHr;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cc.xfl12345.person.cv.service.SMS;
import cc.xfl12345.person.cv.service.UserService;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.cache.CacheManager;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
@Slf4j
public class LoginController {

    protected Environment springAppEnv;

    protected int verificationCodeLimitLength = 6;

    protected String adminPhoneNumber = "";

    protected String adminPassword = "";

    protected String smsWebSocketAccessKeySecret = "";

    protected RateLimitHelper loginRateLimitHelper;

    protected CacheManager cacheManager;

    protected RequestAnalyser requestAnalyser;

    protected SMS sms;

    protected UserService userService;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    public void setRequestAnalyser(RequestAnalyser requestAnalyser) {
        this.requestAnalyser = requestAnalyser;
    }

    @Autowired
    public void setSmsService(SMS SMS) {
        this.sms = SMS;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setSpringAppEnv(Environment springAppEnv) {
        this.springAppEnv = springAppEnv;
    }

    @PostConstruct
    public void init() {
        Random random = new Random();
        adminPhoneNumber = Objects.requireNonNull(springAppEnv.getProperty("app.webui.admin.phone-number"));
        adminPassword = Objects.requireNonNull(springAppEnv.getProperty("app.webui.admin.password"));
        smsWebSocketAccessKeySecret = Objects.requireNonNull(springAppEnv.getProperty("app.sms.xfl12345.access-key-secret"));
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());

        verificationCodeLimitLength = (adminPassword.length() * (int) (Math.ceil(random.nextDouble(2, 5))));
        loginRateLimitHelper = new RateLimitHelper(
            cacheManager.getCache("RateLimitViaIpAddress4Login"),
            SimpleBucketConfig.builder().bucketCapacity(5).refillToken(1).refillFrequency(Duration.ofMinutes(1)).build()
        );
    }

    public boolean checkPassword(String password, String inputText) {
        boolean correct = true;
        byte[] passwordInByte = password.getBytes(StandardCharsets.UTF_8);
        byte[] inputTextInByte = inputText.getBytes(StandardCharsets.UTF_8);
        // 比较谁更长。用最长的长度，扩容长度较小的数组。
        if (passwordInByte.length > inputTextInByte.length) {
            byte[] tmp = new byte[passwordInByte.length];
            System.arraycopy(inputTextInByte, 0, tmp, 0, inputTextInByte.length);
            inputTextInByte = tmp;
        } else {
            byte[] tmp = new byte[inputTextInByte.length];
            System.arraycopy(passwordInByte, 0, tmp, 0, passwordInByte.length);
            passwordInByte = tmp;
        }

        // 对比密码是否一致，使用 时间定长 的比较方法，防止试探性攻击。
        for (int i = 0; i < inputTextInByte.length; i++) {
            if ((inputTextInByte[i] ^ passwordInByte[i]) != 0) {
                correct = false;
            }
        }

        return correct;
    }

    @GetMapping("verification-code-limit-length")
    public int getVerificationCodeLimitLength() {
        return verificationCodeLimitLength;
    }

    protected void login(String loginId, boolean rememberMe) {
        if (rememberMe) {
            StpUtil.login(loginId, new SaLoginModel()
                .setIsWriteHeader(true)
                .setIsLastingCookie(true)
                .setTimeout(60 * 60 * 24 * 365)
            );
        } else {
            StpUtil.login(loginId);
        }
    }

    @PostMapping("login")
    public JsonApiResponseData login(HttpServletRequest request, String phoneNumber, String verificationCode, @Nullable Boolean rememberMe) {
        LocalDateTime date = LocalDateTime.now();
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        if (!StpUtil.isLogin()) {
            log.info("phoneNumber:[%s], verificationCode:[%s]".formatted(phoneNumber, verificationCode));
            RateLimitHelper.ConsumeResult consumeResult = loginRateLimitHelper.tryConsume(requestAnalyser.getIpAddress(request));
            if (consumeResult.isSuccess()) {
                responseData.setApiResult(JsonApiResult.FAILED);
                if (phoneNumber.equals(adminPhoneNumber)) {
                    String password = sms.getSmsValidationCodeCache().get(phoneNumber);
                    // 管理员账户 支持特权密码登录，也支持动态短信验证码登录
                    if (checkPassword(adminPassword, verificationCode) || (password != null && checkPassword(password, verificationCode))) {
                        login(AppConst.XFL_WEBUI_ADMIN_LOGIN_ID, rememberMe == null || rememberMe);
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                        responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                    }
                } else {
                    String password = sms.getSmsValidationCodeCache().get(phoneNumber);
                    if (password != null && checkPassword(password, verificationCode)) {
                        MeetHr meetHr = userService.getHrInfoAndUpdateVisitTime(phoneNumber, date);
                        if (meetHr != null) {
                            login(meetHr.getId().toString(), rememberMe == null || rememberMe);
                            responseData.setApiResult(JsonApiResult.SUCCEED);
                            responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                        }
                    }
                }
            } else {
                responseData.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
                responseData.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));
            }
        } else {
            if (AppConst.XFL_WEBUI_ADMIN_LOGIN_ID.equals(StpUtil.getLoginId().toString())) {
                responseData.setApiResult(JsonApiResult.SUCCEED);
            } else {
                MeetHr meetHr = userService.getHrInfoAndUpdateVisitTime(phoneNumber, date);
                if (meetHr != null) {
                    String targetLoginId = meetHr.getId().toString();
                    String loginId = StpUtil.getLoginId().toString();
                    if (targetLoginId.equals(loginId)) {
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                    } else {
                        responseData.setApiResult(JsonApiResult.FAILED_LOGOUT_IS_NEEDED_BEFORE_LOGIN);
                    }
                } else {
                    StpUtil.logout();
                    responseData.setApiResult(JsonApiResult.FAILED_FORBIDDEN_ACCOUNT);
                }
            }
        }

        return responseData;
    }

    @PostMapping("logout")
    public boolean logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
            return true;
        }

        return false;
    }

    @RequestMapping(path = "kickout", method = {RequestMethod.GET, RequestMethod.POST})
    public boolean kickout(String loginId) {
        if (StpUtil.isLogin() && AppConst.XFL_WEBUI_ADMIN_LOGIN_ID.equals(StpUtil.getLoginId().toString())) {
            String token = StpUtil.getTokenValueByLoginId(loginId);
            if (token != null) {
                if (AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID.equals(loginId)) {
                    sms.closeSessionByLoginId(loginId);
                }
                StpUtil.logout(loginId);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @GetMapping("login/status")
    public boolean status() {
        return StpUtil.isLogin();
    }

    @GetMapping("sms/ws-status")
    public boolean smsWebSocketStatus() {
        return sms.getWebSocketSessionMaps().size() > 0;
    }

    @PostMapping("sms/ws-login")
    public JsonApiResponseData smsServerWebSocketLogin(HttpServletRequest request, String accessKeySecret) {
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        RateLimitHelper.ConsumeResult consumeResult = loginRateLimitHelper.tryConsume(requestAnalyser.getIpAddress(request));
        // 不管有无设置临时过期，都不允许有临时过期问题
        StpUtil.updateLastActivityToNow();
        if (consumeResult.isSuccess()) {
            if (checkPassword(smsWebSocketAccessKeySecret, accessKeySecret)) {
                if (StpUtil.isLogin()) {
                    if (AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID.equals(StpUtil.getLoginId())) {
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                    } else {
                        responseData.setApiResult(JsonApiResult.FAILED_LOGOUT_IS_NEEDED_BEFORE_LOGIN);
                    }

                    responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                } else {
                    Object historyToken = StpUtil.getTokenValueByLoginId(AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID);
                    if (historyToken != null) {
                        responseData.setApiResult(JsonApiResult.FAILED_ALREADY_LOGIN_BY_OTHER);
                    } else {
                        login(AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID, true);
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                        responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                    }
                }
            } else {
                responseData.setApiResult(JsonApiResult.FAILED);
            }
        }

        return responseData;
    }

    @GetMapping("verification-code/generate")
    public String generateVerificationCode(String phoneNumber) {
        return sms.justGetValidationCodeAndPutIntoCache(phoneNumber);
    }

    @GetMapping("verification-code")
    public String getVerificationCode(String phoneNumber) {
        return sms.getSmsValidationCodeCache().get(phoneNumber);
    }

}
