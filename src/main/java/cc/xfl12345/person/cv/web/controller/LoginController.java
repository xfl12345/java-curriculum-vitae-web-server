package cc.xfl12345.person.cv.web.controller;

import cc.xfl12345.person.cv.web.ControllerConst;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@DependsOn(ControllerConst.dependsOnBean)
@RestController
public class LoginController {

    @PostMapping("login")
    public boolean login() {
        if (!StpUtil.isLogin()) {
            StpUtil.login("666");
        }

        return true;
    }

    @PostMapping("logout")
    public boolean logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout("666");
        }

        return true;
    }

    public boolean kickout() {
        StpUtil.logout("666");
        return true;
    }

    public boolean isLogin() {
        return StpUtil.isLogin();
    }

    public void getCaptcha() {

    }

}
