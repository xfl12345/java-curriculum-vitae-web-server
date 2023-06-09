/**
* Source Server Type             : MySQL
* Source Server AppInfo          : 5.7.26
* Source Host                    : 127.0.0.1:3306
* FileOperation Encoding         : utf-8
* Date: 2021/6/9 17:00:00
*/


# 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE meet_hr
(
    `id`                 bigint PRIMARY KEY AUTO_INCREMENT NOT NULL comment 'ID',
    `create_time`        datetime DEFAULT CURRENT_TIMESTAMP comment '创建时间',
    `first_visit_time`   datetime DEFAULT NULL comment '首次访问时间',
    `last_visit_time`    datetime DEFAULT NULL comment '上次访问时间',
    `hr_name`            char(32) DEFAULT NULL comment '面试官姓名',
    `hr_phone_number`    char(24) DEFAULT NULL comment '手机号',
    `hr_job`             char(16) DEFAULT NULL comment '面试官在公司职位',
    `my_job`             char(16) DEFAULT NULL comment '应聘的岗位',
    `note`               varchar(512) DEFAULT NULL comment '备注',
    index boost_query_all (id, create_time, first_visit_time, last_visit_time, hr_name, hr_phone_number, hr_job, my_job)
) ENGINE = InnoDB
  COMMENT '全局ID记录表'
  ROW_FORMAT = DYNAMIC;


