package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.pojo.database.MeetHr;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.ConditionImpl;

@Service
public class UserService {

    public Long getHrId(String phoneNumber) {
        SuidRich suid = BeeFactoryHelper.getSuidRich();
        MeetHr meetHr = suid.selectFirst(
            MeetHr.builder().hrPhoneNumber(phoneNumber).build(),
            new ConditionImpl().selectField(MeetHr.Fields.id)
        );

        return meetHr.getId();
    }

}
