package cc.xfl12345.person.cv.pojo;

import lombok.Data;

@Data
public class SmsTask {

    private String createTime;

    private String phoneNumber;

    private String validationCode;

    private String smsContent;

}
