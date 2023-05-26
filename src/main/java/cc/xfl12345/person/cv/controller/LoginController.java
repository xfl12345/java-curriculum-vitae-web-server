package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.AppConst;
import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.RateLimitHelper;
import cc.xfl12345.person.cv.pojo.RequestAnalyser;
import cc.xfl12345.person.cv.pojo.SimpleBucketConfigUtils;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import cc.xfl12345.person.cv.service.SmsService;
import cc.xfl12345.person.cv.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.cache.CacheManager;
import java.nio.charset.StandardCharsets;
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

    protected SmsService smsService;

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
    public void setSmsService(SmsService smsService) {
        this.smsService = smsService;
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

        verificationCodeLimitLength = (adminPassword.length() * (int) (Math.ceil(random.nextDouble(2, 5))));
        loginRateLimitHelper = new RateLimitHelper(
            cacheManager.getCache("loginRateLimitViaIpAddress"),
            SimpleBucketConfigUtils.createConfigJustInMinutes(12)
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

    @PostMapping("login")
    public JsonApiResponseData login(HttpServletRequest request, String phoneNumber, String verificationCode) {
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        if (!StpUtil.isLogin()) {
            log.info("phoneNumber:[%s], verificationCode:[%s]".formatted(phoneNumber, verificationCode));
            RateLimitHelper.ConsumeResult consumeResult = loginRateLimitHelper.tryConsume(requestAnalyser.getIpAddress(request));
            if (consumeResult.isSuccess()) {
                if (phoneNumber.equals(adminPhoneNumber)) {
                    if (checkPassword(adminPassword, verificationCode)) {
                        StpUtil.login("admin");
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                        responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                    } else {
                        responseData.setApiResult(JsonApiResult.FAILED);
                    }
                } else {
                    String password = smsService.getSmsValidationCodeCache().get(phoneNumber);
                    if (password != null && checkPassword(password, verificationCode)) {
                        Long hrId = userService.getHrId(phoneNumber);
                        StpUtil.login(hrId.toString());
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                        responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
                    } else {
                        responseData.setApiResult(JsonApiResult.FAILED);
                    }
                }
            } else {
                responseData.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
                responseData.setData(Map.of(JsonApiConst.COOL_DOWN_REMAINDER_FIELD, consumeResult.getCoolDownRemainder()));
            }
        } else {
            responseData.setApiResult(JsonApiResult.FAILED_ALREADY_LOGIN);
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
        if (StpUtil.isLogin() && "admin".equals(StpUtil.getLoginId())) {
            try {
                String token = StpUtil.getTokenValueByLoginId(loginId);
                if (token != null) {
                    StpUtil.logout(loginId);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
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

    @PostMapping("sms-server-login")
    public JsonApiResponseData smsServerWebSocketLogin(HttpServletRequest request, String accessKeySecret) {
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        RateLimitHelper.ConsumeResult consumeResult = loginRateLimitHelper.tryConsume(requestAnalyser.getIpAddress(request));
        if (consumeResult.isSuccess()) {
            if (checkPassword(smsWebSocketAccessKeySecret, accessKeySecret)) {
                if (StpUtil.isLogin()) {
                    Object historyLoginId = StpUtil.getLoginIdByToken(StpUtil.getTokenValue());
                    if (historyLoginId != null && historyLoginId.equals(AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID)) {
                        responseData.setApiResult(JsonApiResult.SUCCEED);
                    } else {
                        responseData.setApiResult(JsonApiResult.FAILED_ALREADY_LOGIN);
                    }
                } else {
                    StpUtil.login(AppConst.XFL_SMS_WEB_SOCKET_SERIVE_LOGIN_ID);
                    responseData.setApiResult(JsonApiResult.SUCCEED);
                }

                responseData.setData(Map.of(JsonApiConst.LOGIN_TOKEN_FIELD, StpUtil.getTokenValue()));
            } else {
                responseData.setApiResult(JsonApiResult.FAILED);
            }
        }

        return responseData;
    }

}
