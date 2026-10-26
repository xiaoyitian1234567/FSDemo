package com.feishu.integration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪酬实体类
 * 存储员工薪资信息，包括薪资构成、社保公积金、个税计算结果等
 */
@Data
@TableName("payroll")
public class Payroll {
    /**
     * 薪资记录ID，自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工工号
     */
    private String employeeNo;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 薪资月份，格式：YYYY-MM
     */
    private String payrollMonth;

    /**
     * 基本工资
     */
    private BigDecimal basicSalary;

    /**
     * 绩效工资
     */
    private BigDecimal performanceSalary;

    /**
     * 岗位工资
     */
    private BigDecimal positionSalary;

    /**
     * 津贴总额
     */
    private BigDecimal allowanceTotal;

    /**
     * 奖金总额
     */
    private BigDecimal bonusTotal;

    /**
     * 加班工资
     */
    private BigDecimal overtimeSalary;

    /**
     * 其他收入
     */
    private BigDecimal otherIncome;

    /**
     * 应发工资
     */
    private BigDecimal grossSalary;

    /**
     * 社保基数
     */
    private BigDecimal socialSecurityBase;

    /**
     * 个人社保缴纳额
     */
    private BigDecimal personalSocialSecurity;

    /**
     * 公司社保缴纳额
     */
    private BigDecimal companySocialSecurity;

    /**
     * 公积金基数
     */
    private BigDecimal housingFundBase;

    /**
     * 个人公积金缴纳额
     */
    private BigDecimal personalHousingFund;

    /**
     * 公司公积金缴纳额
     */
    private BigDecimal companyHousingFund;

    /**
     * 专项附加扣除
     */
    private BigDecimal specialDeduction;

    /**
     * 应纳税所得额
     */
    private BigDecimal taxableIncome;

    /**
     * 个人所得税
     */
    private BigDecimal personalIncomeTax;

    /**
     * 其他扣除
     */
    private BigDecimal otherDeductions;

    /**
     * 实发工资
     */
    private BigDecimal netSalary;

    /**
     * 薪资状态：draft-草稿, calculated-已计算, approved-已审批, paid-已发放
     */
    private String status;

    /**
     * 薪资计算日期
     */
    private LocalDateTime calculationDate;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批日期
     */
    private LocalDateTime approvalDate;

    /**
     * 发放日期
     */
    private LocalDate paymentDate;

    /**
     * 银行账号（脱敏）
     */
    private String bankAccountMasked;

    /**
     * 薪资规则版本
     */
    private String ruleVersion;

    /**
     * 城市编码
     */
    private String cityCode;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 是否删除，0-未删除，1-已删除
     */
    private Integer isDeleted;
}