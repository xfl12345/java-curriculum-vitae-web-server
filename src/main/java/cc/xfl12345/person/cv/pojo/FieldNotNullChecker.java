package cc.xfl12345.person.cv.pojo;

import cc.xfl12345.person.cv.appconst.AppConst;
import lombok.Getter;
import lombok.Setter;

public class FieldNotNullChecker {
    @Getter
    @Setter
    protected String messageTemplateFieldCanNotBeNull = AppConst.MESSAGE_TEMPLATE_FIELD_CAN_NOT_BE_NULL;

    public void check(Object fieldValue, String fieldName) {
        if (fieldValue == null) {
            throw new IllegalArgumentException(messageTemplateFieldCanNotBeNull.formatted(fieldName));
        }
    }
}
