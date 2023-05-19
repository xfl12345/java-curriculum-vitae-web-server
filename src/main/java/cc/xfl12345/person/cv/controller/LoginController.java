package cc.xfl12345.person.cv.controller;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Random;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
@Slf4j
public class LoginController {

    protected Environment springAppEnv;

    protected CaptchaController captchaController;

    protected int validationCodeLimitLength = 6;

    @Autowired
    public void setSpringAppEnv(Environment springAppEnv) {
        this.springAppEnv = springAppEnv;
    }

    @Autowired
    public void setCaptchaController(CaptchaController captchaController) {
        this.captchaController = captchaController;
    }

    protected String getAdminPhoneNumber() {
        return springAppEnv.getProperty("app.webui.admin.phone-number");
    }

    protected String getAdminPassword() {
        return springAppEnv.getProperty("app.webui.admin.password");
    }

    @PostConstruct
    public void init() {
        Random random = new Random();
        validationCodeLimitLength = (Objects.requireNonNull(getAdminPassword())
            .length() * (int) (Math.ceil(random.nextDouble(2, 5))));
    }

    @GetMapping("validation-code-limit-length")
    public int getValidationCodeLimitLength() {
        return validationCodeLimitLength;
    }

    @PostMapping("login")
    public SaResult login(String phoneNumber, String validationCode) {
        log.info("phoneNumber:[%s], validationCode:[%s]".formatted(phoneNumber, validationCode));
        if (!StpUtil.isLogin()) {
            if (phoneNumber.equals(getAdminPhoneNumber())) {
                if (validationCode.equals(getAdminPassword())) {
                    StpUtil.login("admin");
                    return SaResult.ok("登录成功");
                }
            } else {
                // TODO 完善鉴权
                StpUtil.login("666");
                return SaResult.ok("登录成功");
            }
        }

        return SaResult.error("登录失败");
    }

    @PostMapping("logout")
    public boolean logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }

        return true;
    }

    public boolean kickout() {
        StpUtil.logout("666");
        return true;
    }

    @GetMapping("login/status")
    public boolean status() {
        return StpUtil.isLogin();
    }

    public void getCaptcha() {

    }

}
