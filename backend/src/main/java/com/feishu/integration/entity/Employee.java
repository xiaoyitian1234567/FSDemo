package com.feishu.integration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工实体类
 * 存储员工基本信息，包括个人资料、组织架构信息和 employment 状态
 */
@Data
@TableName("employee")
public class Employee {
    /**
     * 员工ID，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 飞书用户ID
     */
    private String feishuUserId;

    /**
     * 员工工号
     */
    private String employeeId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 英文名
     */
    private String englishName;

    /**
     * 印尼语名
     */
    private String indonesianName;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位
     */
    private String position;

    /**
     * 职级
     */
    private String jobLevel;

    /**
     * 雇佣类型：正式、实习、合同
     */
    private String employmentType;

    /**
     * 员工状态：在职、离职、休假、试用期
     */
    private String status;

    /**
     * 入职日期
     */
    private LocalDate hireDate;

    /**
     * 转正日期
     */
    private LocalDate confirmationDate;

    /**
     * 离职日期
     */
    private LocalDate terminationDate;

    /**
     * 工作地点
     */
    private String workLocation;

    /**
     * 汇报上级ID
     */
    private Long managerId;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否删除，0-未删除，1-已删除
     */
    private Integer isDeleted;
}