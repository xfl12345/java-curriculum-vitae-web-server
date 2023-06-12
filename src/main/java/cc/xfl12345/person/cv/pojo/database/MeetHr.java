package cc.xfl12345.person.cv.pojo.database;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 表名：meet_hr
 * 表注释：全局ID记录表
*/
@lombok.Data
@lombok.experimental.SuperBuilder
@lombok.NoArgsConstructor
@lombok.experimental.FieldNameConstants
@io.swagger.annotations.ApiModel("全局ID记录表")
@javax.persistence.Table(name = "meet_hr")
@javax.persistence.Entity
public class MeetHr implements cc.xfl12345.person.cv.pojo.OpenCloneable, Serializable {
    /**
     * ID
     */
    @javax.persistence.Id
    @javax.persistence.Column(name = "id", nullable = false)
    @javax.persistence.GeneratedValue(generator = "JDBC")
    @io.swagger.annotations.ApiModelProperty("ID")
    private Long id;

    /**
     * 创建时间
     */
    @javax.persistence.Column(name = "create_time", nullable = true)
    @io.swagger.annotations.ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 首次访问时间
     */
    @javax.persistence.Column(name = "first_visit_time", nullable = true)
    @io.swagger.annotations.ApiModelProperty("首次访问时间")
    private LocalDateTime firstVisitTime;

    /**
     * 上次访问时间
     */
    @javax.persistence.Column(name = "last_visit_time", nullable = true)
    @io.swagger.annotations.ApiModelProperty("上次访问时间")
    private LocalDateTime lastVisitTime;

    /**
     * 面试官姓名
     */
    @javax.persistence.Column(name = "hr_name", nullable = true, length = 32)
    @io.swagger.annotations.ApiModelProperty("面试官姓名")
    private String hrName;

    /**
     * 手机号
     */
    @javax.persistence.Column(name = "hr_phone_number", nullable = true)
    @io.swagger.annotations.ApiModelProperty("手机号")
    private String hrPhoneNumber;

    /**
     * 面试官在公司职位
     */
    @javax.persistence.Column(name = "hr_job", nullable = true, length = 16)
    @io.swagger.annotations.ApiModelProperty("面试官在公司职位")
    private String hrJob;

    /**
     * 应聘的岗位
     */
    @javax.persistence.Column(name = "my_job", nullable = true, length = 16)
    @io.swagger.annotations.ApiModelProperty("应聘的岗位")
    private String myJob;

    /**
     * 备注
     */
    @javax.persistence.Column(name = "note", nullable = true, length = 512)
    @io.swagger.annotations.ApiModelProperty("备注")
    private String note;

    private static final long serialVersionUID = 1L;

    @Override
    public MeetHr clone() throws CloneNotSupportedException {
        return (MeetHr) super.clone();
    }
}
