package com.feishu.integration.service;

import com.feishu.integration.dto.PayrollDTO;
import com.feishu.integration.dto.PayrollCalculationDTO;
import com.feishu.integration.entity.Payroll;
import com.baomidou.mybatisplus.extension.service.IService;
import com.feishu.integration.vo.PayrollVO;
import java.util.List;
import java.util.Map;

/**
 * 薪酬服务接口
 * 定义薪资计算、审批流程、个税核算、银行报盘等薪酬管理相关业务方法
 */
public interface PayrollService extends IService<Payroll> {
    
    /**
     * 计算员工薪资
     * @param calculationDTO 薪资计算参数DTO
     * @return 计算后的薪资记录
     */
    Payroll calculateEmployeePayroll(PayrollCalculationDTO calculationDTO);
    
    /**
     * 批量计算部门薪资
     * @param departmentId 部门ID
     * @param payrollMonth 薪资月份
     * @return 计算结果，包含成功和失败的员工ID
     */
    Map<String, List<Long>> batchCalculateDepartmentPayroll(Long departmentId, String payrollMonth);
    
    /**
     * 发起薪资审批流程
     * @param payrollId 薪资记录ID
     * @return 审批流程ID
     */
    String initiatePayrollApproval(Long payrollId);
    
    /**
     * 处理飞书审批回调
     * @param approvalEvent 审批事件数据
     * @return 处理结果
     */
    boolean handleApprovalCallback(Map<String, Object> approvalEvent);
    
    /**
     * 生成银行报盘文件
     * @param payrollMonth 薪资月份
     * @param departmentId 部门ID，为null时生成全公司报盘
     * @return 报盘文件下载URL
     */
    String generateBankFile(String payrollMonth, Long departmentId);
    
    /**
     * 获取员工薪资历史记录
     * @param employeeId 员工ID
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 薪资记录列表
     */
    List<PayrollVO> getEmployeePayrollHistory(Long employeeId, String startMonth, String endMonth);
    
    /**
     * 获取薪资月份统计信息
     * @param payrollMonth 薪资月份
     * @return 统计信息，包含总人数、总金额、平均薪资等
     */
    Map<String, Object> getPayrollStatistics(String payrollMonth);
    
    /**
     * 更新薪资规则配置
     * @param ruleConfig 薪资规则配置
     * @return 更新结果
     */
    boolean updateSalaryRuleConfig(Map<String, Object> ruleConfig);
    
    /**
     * 获取薪资计算明细
     * @param payrollId 薪资记录ID
     * @return 薪资计算明细，包含各项收入、扣除项的计算过程
     */
    Map<String, Object> getPayrollCalculationDetails(Long payrollId);
    
    /**
     * 同步社保公积金数据
     * @param cityCode 城市编码
     * @param year 年份
     * @param month 月份
     * @return 同步结果
     */
    boolean syncSocialSecurityData(String cityCode, int year, int month);
}