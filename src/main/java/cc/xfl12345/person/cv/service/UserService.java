package cc.xfl12345.person.cv.service;

import cc.xfl12345.person.cv.pojo.database.MeetHr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.IncludeType;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.SuidRich;
import org.teasoft.bee.osql.transaction.Transaction;
import org.teasoft.honey.osql.core.BeeFactoryHelper;
import org.teasoft.honey.osql.core.ConditionImpl;
import org.teasoft.honey.osql.core.SessionFactory;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UserService {

    public Long getHrIdByPhoneNumber(String phoneNumber) {
        SuidRich suid = BeeFactoryHelper.getSuidRich();
        MeetHr meetHr = suid.selectFirst(
            MeetHr.builder().hrPhoneNumber(phoneNumber).build(),
            new ConditionImpl().selectField(MeetHr.Fields.id)
        );

        return meetHr != null ? meetHr.getId() : null;
    }

    public List<MeetHr> getHrInfoByPhoneNumber(String phoneNumber) {
        return BeeFactoryHelper.getSuidRich().select(
            MeetHr.builder().hrPhoneNumber(phoneNumber).build()
        );
    }

    public List<MeetHr> getAllHrInfo() {
        return BeeFactoryHelper.getSuidRich().select(MeetHr.builder().build());
    }

    public long getHrInfoCount() {
        return BeeFactoryHelper.getSuidRich().count(MeetHr.builder().build());
    }

    public MeetHr getHrInfoById(Long id) {
        return BeeFactoryHelper.getSuidRich().selectById(MeetHr.class, id);
    }

    public boolean deleteHrInfoById(Long id) {
        return BeeFactoryHelper.getSuidRich().deleteById(MeetHr.class, id) == 1;
    }

    public boolean updateHrInfoById(MeetHr meetHr) {
        return BeeFactoryHelper.getSuidRich().updateById(meetHr, new ConditionImpl().setIncludeType(IncludeType.EXCLUDE_BOTH)) == 1;
    }

    public boolean addHrInfo(MeetHr meetHr) {
        return BeeFactoryHelper.getSuidRich().insert(meetHr) == 1;
    }

    public MeetHr getHrInfoAndUpdateVisitTime(String phoneNumber, Date visitTime) {
        MeetHr meetHr = null;
        Transaction transaction = SessionFactory.getTransaction();
        try {
            transaction.begin();
            SuidRich suid = BeeFactoryHelper.getSuidRich();
            meetHr = suid.selectFirst(
                new MeetHr(),
                new ConditionImpl().opOn(MeetHr.Fields.hrPhoneNumber, Op.eq, phoneNumber)
            );
            if (meetHr != null) {
                if (meetHr.getFirstVisitTime() == null) {
                    meetHr.setFirstVisitTime(visitTime);
                }
                meetHr.setLastVisitTime(visitTime);
                suid.updateById(meetHr, new ConditionImpl());
            }
            transaction.commit();
        } catch (Exception e) {
            log.error("Update failed.", e);
            transaction.rollback();
        }

        return meetHr;
    }

    public boolean justUpdateVisitTimeByPhoneNumber(String phoneNumber, Date visitTime) {
        return justUpdateVisitTimeByCondition(new ConditionImpl()
            .opOn(MeetHr.Fields.hrPhoneNumber, Op.eq, phoneNumber)
            .selectField(
                MeetHr.Fields.id,
                MeetHr.Fields.hrPhoneNumber,
                MeetHr.Fields.firstVisitTime
            ), visitTime
        );
    }

    public boolean justUpdateVisitTimeById(Long id, Date visitTime) {
        return justUpdateVisitTimeByCondition(new ConditionImpl()
            .opOn(MeetHr.Fields.id, Op.eq, id)
            .selectField(
                MeetHr.Fields.id,
                MeetHr.Fields.hrPhoneNumber,
                MeetHr.Fields.firstVisitTime
            ), visitTime
        );
    }

    protected boolean justUpdateVisitTimeByCondition(Condition condition, Date visitTime) {
        MeetHr meetHr = null;
        Transaction transaction = SessionFactory.getTransaction();
        try {
            transaction.begin();
            SuidRich suid = BeeFactoryHelper.getSuidRich();
            meetHr = suid.selectFirst(new MeetHr(), condition);
            if (meetHr != null) {
                if (meetHr.getFirstVisitTime() == null) {
                    meetHr.setFirstVisitTime(visitTime);
                }
                meetHr.setLastVisitTime(visitTime);
                suid.updateById(meetHr, new ConditionImpl());
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            log.error("Update failed.", e);
            transaction.rollback();
        }

        return false;
    }


}
