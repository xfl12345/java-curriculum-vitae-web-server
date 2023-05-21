package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import cc.xfl12345.person.cv.pojo.XflSmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@DependsOn(ControllerConst.dependsOnBean)
@Service("SmsService")
public class SmsService {

    protected XflSmsConfig xflSmsConfig;

    @Autowired
    public void setMySmsConfig(XflSmsConfig xflSmsConfig) {
        this.xflSmsConfig = xflSmsConfig;
    }

    public boolean sendSmsValidationCode(String phoneNumber) {


        return true;
    }

}
