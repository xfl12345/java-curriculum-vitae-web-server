package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.appconst.ControllerConst;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@DependsOn(ControllerConst.dependsOnBean)
@Service("SmsService")
public class SmsService {

    public boolean sendSmsValidationCode(String phoneNumber) {


        return false;
    }

}
