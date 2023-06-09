package cc.xfl12345.person.cv.pojo;

import lombok.Data;

@Data
public class XflSmsConfig {
    private String accessKeySecret;

    private String signName;

    private int validationCodeLength;

    private long expirationInMinute;

    private String template;
}
