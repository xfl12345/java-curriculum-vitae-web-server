package cc.xfl12345.person.cv.service;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public String getLoginId() {
        return String.valueOf(StpUtil.getLoginId());
    }

}
