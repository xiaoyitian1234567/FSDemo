package com.feishu.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larksuite.oapi.Client;
import com.larksuite.oapi.service.approval.v4.model.CreateApprovalInstanceReq;
import com.larksuite.oapi.service.approval.v4.model.CreateApprovalInstanceResp;
import com.feishu.integration.dto.PayrollCalculationDTO;
import com.feishu.integration.entity.Employee;
import com.feishu.integration.entity.Payroll;
import com.feishu.integration.mapper.PayrollMapper;
import com.feishu.integration.service.EmployeeService;
import com.feishu.integration.service.PayrollService;
import com.feishu.integration.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 薪酬服务实现类
 * 实现薪资计算、审批流程、个税核算、银行报盘等薪酬管理核心业务逻辑
 */
@Service
@Slf4j
public class PayrollServiceImpl extends ServiceImpl<PayrollMapper, Payroll> implements PayrollService {

    @Autowired
    private PayrollMapper payrollMapper;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private Client feishuClient;

    // 缓存薪资规则配置，提高计算效率
    private static final Map<String, Object> SALARY_RULE_CACHE = new ConcurrentHashMap<>();

    // 社保公积金费率缓存
    private static final Map<String, Map<String, BigDecimal>> SOCIAL_SECURITY_RATE_CACHE = new ConcurrentHashMap<>();

    /**
     * 计算员工薪资
     */
    @Override
    @Transactional
    public Payroll calculateEmployeePayroll(PayrollCalculationDTO calculationDTO) {
        log.info("Calculating payroll for employee ID: {} in month: {}",
                calculationDTO.getEmployeeId(), calculationDTO.getPayrollMonth());

        // 1. 获取员工信息
        Employee employee = employeeService.getById(calculationDTO.getEmployeeId());
        if (employee == null) {
            log.error("Employee not found with ID: {}", calculationDTO.getEmployeeId());
            throw new RuntimeException("Employee not found");
        }

        // 2. 检查是否已存在该月薪资记录
        QueryWrapper<Payroll> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("employee_id", calculationDTO.getEmployeeId());
        queryWrapper.eq("payroll_month", calculationDTO.getPayrollMonth());
        Payroll existingPayroll = payrollMapper.selectOne(queryWrapper);
        if (existingPayroll != null) {
            log.warn("Payroll record already exists for employee ID: {} in month: {}",
                    calculationDTO.getEmployeeId(), calculationDTO.getPayrollMonth());
            // 如果已存在，更新状态为草稿，重新计算
            existingPayroll.setStatus("draft");
            payrollMapper.updateById(existingPayroll);
        }

        // 3. 创建/更新薪资记录
        Payroll payroll = existingPayroll != null ? existingPayroll : new Payroll();
        payroll.setEmployeeId(employee.getId());
        payroll.setEmployeeNo(employee.getEmployeeId());
        payroll.setEmployeeName(employee.getName());
        payroll.setDepartmentId(employee.getDepartmentId());
        payroll.setDepartmentName(employee.getDepartmentName());
        payroll.setPayrollMonth(calculationDTO.getPayrollMonth());
        payroll.setStatus("draft");
        payroll.setCreatedAt(LocalDateTime.now());
        payroll.setUpdatedAt(LocalDateTime.now());
        payroll.setCreatedBy("system");
        payroll.setUpdatedBy("system");
        payroll.setIsDeleted(0);
        payroll.setCityCode(calculationDTO.getCityCode() != null ? calculationDTO.getCityCode() : "310000"); // 默认上海

        // 4. 计算各项薪资构成
        calculateSalaryComponents(payroll, employee, calculationDTO);

        // 5. 计算社保公积金
        calculateSocialSecurityAndFund(payroll);

        // 6. 计算个税
        calculateIncomeTax(payroll, calculationDTO);

        // 7. 计算最终实发工资
        calculateNetSalary(payroll);

        // 8. 保存薪资记录
        if (existingPayroll == null) {
            payrollMapper.insert(payroll);
        } else {
            payrollMapper.updateById(payroll);
        }

        log.info("Payroll calculated successfully for employee ID: {}, net salary: {}",
                employee.getId(), payroll.getNetSalary());

        return payroll;
    }

    /**
     * 批量计算部门薪资
     */
    @Override
    @Transactional
    public Map<String, List<Long>> batchCalculateDepartmentPayroll(Long departmentId, String payrollMonth) {
        log.info("Batch calculating payroll for department ID: {} in month: {}", departmentId, payrollMonth);

        Map<String, List<Long>> result = new HashMap<>();
        result.put("success", new ArrayList<>());
        result.put("failed", new ArrayList<>());

        // 获取部门员工列表
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        log.info("Found {} employees in department ID: {}", employees.size(), departmentId);

        // 逐个计算薪资
        for (Employee employee : employees) {
            try {
                PayrollCalculationDTO calculationDTO = new PayrollCalculationDTO();
                calculationDTO.setEmployeeId(employee.getId());
                calculationDTO.setPayrollMonth(payrollMonth);
                // 使用员工工作地点作为城市编码
                calculationDTO.setCityCode(getCityCodeByWorkLocation(employee.getWorkLocation()));

                calculateEmployeePayroll(calculationDTO);
                result.get("success").add(employee.getId());
            } catch (Exception e) {
                log.error("Failed to calculate payroll for employee ID: {}", employee.getId(), e);
                result.get("failed").add(employee.getId());
            }
        }

        log.info("Batch payroll calculation completed. Success: {}, Failed: {}",
                result.get("success").size(), result.get("failed").size());

        return result;
    }

    /**
     * 发起薪资审批流程
     */
    @Override
    public String initiatePayrollApproval(Long payrollId) {
        log.info("Initiating payroll approval for payroll ID: {}", payrollId);

        Payroll payroll = payrollMapper.selectById(payrollId);
        if (payroll == null) {
            log.error("Payroll record not found with ID: {}", payrollId);
            throw new RuntimeException("Payroll record not found");
        }

        if (!"calculated".equals(payroll.getStatus())) {
            log.error("Cannot initiate approval for payroll ID: {}, status is not 'calculated'", payrollId);
            throw new RuntimeException("Payroll must be in 'calculated' status to initiate approval");
        }

        try {
            // 构建飞书审批实例请求
            CreateApprovalInstanceReq req = new CreateApprovalInstanceReq();
            // TODO: 根据实际飞书审批模板配置请求参数
            // 这里需要根据实际的飞书审批模板来设置表单字段

            // 调用飞书API创建审批实例
            CreateApprovalInstanceResp resp = feishuClient.approval().v4().instances().create(req);

            if (!resp.success()) {
                log.error("Failed to create approval instance: {}", resp.getMsg());
                throw new RuntimeException("Failed to initiate approval process");
            }

            // 更新薪资记录状态
            payroll.setStatus("pending_approval");
            payroll.setUpdatedAt(LocalDateTime.now());
            payrollMapper.updateById(payroll);

            String approvalInstanceId = resp.getData().getInstanceId();
            log.info("Payroll approval initiated successfully, approval instance ID: {}", approvalInstanceId);

            return approvalInstanceId;
        } catch (Exception e) {
            log.error("Error initiating payroll approval", e);
            throw new RuntimeException("Failed to initiate approval process: " + e.getMessage());
        }
    }

    /**
     * 处理飞书审批回调
     */
    @Override
    @Transactional
    public boolean handleApprovalCallback(Map<String, Object> approvalEvent) {
        log.info("Handling approval callback: {}", JsonUtil.toJson(approvalEvent));

        try {
            // 解析审批事件
            String eventType = (String) approvalEvent.get("type");
            Map<String, Object> event = (Map<String, Object>) approvalEvent.get("event");
            String instanceId = (String) event.get("instance_id");
            String status = (String) event.get("status");

            // 查找对应的薪资记录
            // TODO: 需要在发起审批时关联approvalInstanceId和payrollId
            Payroll payroll = null; // 这里需要实现根据approvalInstanceId查询薪资记录的逻辑

            if (payroll == null) {
                log.error("Cannot find payroll record for approval instance ID: {}", instanceId);
                return false;
            }

            // 根据审批状态更新薪资记录
            if ("APPROVED".equals(status)) {
                payroll.setStatus("approved");
                payroll.setApprovalDate(LocalDateTime.now());
                // TODO: 获取审批人信息
                payroll.setApproverId(1L); // 临时设置
                payroll.setApproverName("System Admin"); // 临时设置
                payroll.setUpdatedAt(LocalDateTime.now());
                payrollMapper.updateById(payroll);
                log.info("Payroll approved, payroll ID: {}", payroll.getId());

                // 自动生成银行报盘文件
                // generateBankFile(payroll.getPayrollMonth(), payroll.getDepartmentId());
            } else if ("REJECTED".equals(status)) {
                payroll.setStatus("rejected");
                payroll.setUpdatedAt(LocalDateTime.now());
                payrollMapper.updateById(payroll);
                log.info("Payroll rejected, payroll ID: {}", payroll.getId());
            }

            return true;
        } catch (Exception e) {
            log.error("Error handling approval callback", e);
            return false;
        }
    }

    /**
     * 生成银行报盘文件
     */
    @Override
    public String generateBankFile(String payrollMonth, Long departmentId) {
        log.info("Generating bank file for month: {}, department ID: {}", payrollMonth, departmentId);

        // TODO: 实现银行报盘文件生成逻辑
        // 1. 查询该月已审批的薪资记录
        // 2. 按照银行要求的格式生成报盘文件
        // 3. 上传文件到文件服务器
        // 4. 返回下载URL

        return "https://example.com/download/bankfile_" + payrollMonth + "_" + System.currentTimeMillis() + ".txt";
    }

    /**
     * 获取员工薪资历史记录
     */
    @Override
    public List<PayrollVO> getEmployeePayrollHistory(Long employeeId, String startMonth, String endMonth) {
        log.info("Getting payroll history for employee ID: {}, from {} to {}", employeeId, startMonth, endMonth);

        // TODO: 实现查询员工薪资历史记录的逻辑
        return new ArrayList<>();
    }

    /**
     * 获取薪资月份统计信息
     */
    @Override
    public Map<String, Object> getPayrollStatistics(String payrollMonth) {
        log.info("Getting payroll statistics for month: {}", payrollMonth);

        // TODO: 实现薪资统计逻辑
        Map<String, Object> statistics = new HashMap<>();
        return statistics;
    }

    /**
     * 更新薪资规则配置
     */
    @Override
    public boolean updateSalaryRuleConfig(Map<String, Object> ruleConfig) {
        log.info("Updating salary rule config");

        // TODO: 实现更新薪资规则配置的逻辑
        // 1. 保存到数据库
        // 2. 更新缓存

        return true;
    }

    /**
     * 获取薪资计算明细
     */
    @Override
    public Map<String, Object> getPayrollCalculationDetails(Long payrollId) {
        log.info("Getting payroll calculation details for payroll ID: {}", payrollId);

        // TODO: 实现获取薪资计算明细的逻辑
        Map<String, Object> details = new HashMap<>();
        return details;
    }

    /**
     * 同步社保公积金数据
     */
    @Override
    public boolean syncSocialSecurityData(String cityCode, int year, int month) {
        log.info("Syncing social security data for city: {}, {}-{}", cityCode, year, month);

        // TODO: 实现同步社保公积金数据的逻辑
        // 1. 调用外部API获取最新的社保公积金数据
        // 2. 更新数据库
        // 3. 更新缓存

        return true;
    }

    /**
     * 计算各项薪资构成
     */
    private void calculateSalaryComponents(Payroll payroll, Employee employee, PayrollCalculationDTO calculationDTO) {
        // 获取薪资规则配置
        Map<String, Object> salaryRules = getSalaryRuleConfig(employee.getJobLevel(), payroll.getCityCode());

        // 计算基本工资
        BigDecimal basicSalary = calculateBasicSalary(employee, salaryRules);
        payroll.setBasicSalary(basicSalary);

        // 计算绩效工资
        BigDecimal performanceSalary = calculatePerformanceSalary(employee, calculationDTO, salaryRules);
        payroll.setPerformanceSalary(performanceSalary);

        // 计算岗位工资
        BigDecimal positionSalary = calculatePositionSalary(employee, salaryRules);
        payroll.setPositionSalary(positionSalary);

        // 计算津贴
        BigDecimal allowanceTotal = calculateAllowances(employee, payroll.getCityCode(), salaryRules);
        payroll.setAllowanceTotal(allowanceTotal);

        // 计算奖金
        BigDecimal bonusTotal = calculateBonuses(employee, calculationDTO, salaryRules);
        payroll.setBonusTotal(bonusTotal);

        // 计算加班工资
        BigDecimal overtimeSalary = calculateOvertimeSalary(employee, calculationDTO, salaryRules);
        payroll.setOvertimeSalary(overtimeSalary);

        // 计算其他收入
        BigDecimal otherIncome = calculateOtherIncome(calculationDTO);
        payroll.setOtherIncome(otherIncome);

        // 计算应发工资
        BigDecimal grossSalary = basicSalary.add(performanceSalary)
                .add(positionSalary).add(allowanceTotal)
                .add(bonusTotal).add(overtimeSalary)
                .add(otherIncome);
        payroll.setGrossSalary(grossSalary);
    }

    /**
     * 计算基本工资
     */
    private BigDecimal calculateBasicSalary(Employee employee, Map<String, Object> salaryRules) {
        // 根据职级和岗位获取基本工资
        Map<String, Object> levelConfig = (Map<String, Object>) salaryRules.get("level_config");
        Map<String, Object> jobConfig = (Map<String, Object>) levelConfig.get(employee.getJobLevel());
        return new BigDecimal(jobConfig.get("basic_salary").toString());
    }

    /**
     * 计算绩效工资
     */
    private BigDecimal calculatePerformanceSalary(Employee employee, PayrollCalculationDTO calculationDTO, Map<String, Object> salaryRules) {
        // 根据绩效等级和绩效系数计算绩效工资
        BigDecimal performanceCoefficient = calculationDTO.getPerformanceCoefficient() != null ?
                calculationDTO.getPerformanceCoefficient() : new BigDecimal(1.0);

        Map<String, Object> performanceConfig = (Map<String, Object>) salaryRules.get("performance_config");
        BigDecimal performanceRatio = new BigDecimal(performanceConfig.get("ratio").toString());

        // 绩效工资 = 基本工资 * 绩效比例 * 绩效系数
        return payroll.getBasicSalary().multiply(performanceRatio).multiply(performanceCoefficient);
    }

    /**
     * 计算岗位工资
     */
    private BigDecimal calculatePositionSalary(Employee employee, Map<String, Object> salaryRules) {
        // 根据岗位获取岗位工资
        Map<String, Object> positionConfig = (Map<String, Object>) salaryRules.get("position_config");
        Map<String, Object> positionSalaryConfig = (Map<String, Object>) positionConfig.get(employee.getPosition());

        if (positionSalaryConfig != null) {
            return new BigDecimal(positionSalaryConfig.get("amount").toString());
        }

        // 默认岗位工资为基本工资的20%
        return payroll.getBasicSalary().multiply(new BigDecimal(0.2));
    }

    /**
     * 计算津贴
     */
    private BigDecimal calculateAllowances(Employee employee, String cityCode, Map<String, Object> salaryRules) {
        BigDecimal totalAllowance = BigDecimal.ZERO;

        // 获取津贴配置
        Map<String, Object> allowanceConfig = (Map<String, Object>) salaryRules.get("allowance_config");
        List<Map<String, Object>> allowances = (List<Map<String, Object>>) allowanceConfig.get("allowances");

        // 计算各项津贴
        for (Map<String, Object> allowance : allowances) {
            String type = (String) allowance.get("type");
            String condition = (String) allowance.get("condition");
            BigDecimal amount = new BigDecimal(allowance.get("amount").toString());

            // 根据条件判断是否发放该津贴
            if (isAllowanceEligible(employee, cityCode, type, condition)) {
                totalAllowance = totalAllowance.add(amount);
            }
        }

        return totalAllowance;
    }

    /**
     * 判断是否符合津贴发放条件
     */
    private boolean isAllowanceEligible(Employee employee, String cityCode, String type, String condition) {
        // TODO: 实现津贴发放条件判断逻辑
        return true;
    }

    /**
     * 计算奖金
     */
    private BigDecimal calculateBonuses(Employee employee, PayrollCalculationDTO calculationDTO, Map<String, Object> salaryRules) {
        // TODO: 实现奖金计算逻辑
        return BigDecimal.ZERO;
    }

    /**
     * 计算加班工资
     */
    private BigDecimal calculateOvertimeSalary(Employee employee, PayrollCalculationDTO calculationDTO, Map<String, Object> salaryRules) {
        // TODO: 实现加班工资计算逻辑
        return BigDecimal.ZERO;
    }

    /**
     * 计算其他收入
     */
    private BigDecimal calculateOtherIncome(PayrollCalculationDTO calculationDTO) {
        // TODO: 实现其他收入计算逻辑
        return BigDecimal.ZERO;
    }

    /**
     * 计算社保公积金
     */
    private void calculateSocialSecurityAndFund(Payroll payroll) {
        // 获取社保公积金费率
        Map<String, BigDecimal> rates = getSocialSecurityRates(payroll.getCityCode());

        // 计算社保
        BigDecimal socialSecurityBase = calculateSocialSecurityBase(payroll);
        payroll.setSocialSecurityBase(socialSecurityBase);

        BigDecimal personalSocialSecurity = socialSecurityBase
                .multiply(rates.get("personal_pension_rate"))
                .add(socialSecurityBase.multiply(rates.get("personal_medical_rate")))
                .add(socialSecurityBase.multiply(rates.get("personal_unemployment_rate")))
                .add(socialSecurityBase.multiply(rates.get("personal_injury_rate")))
                .add(socialSecurityBase.multiply(rates.get("personal_maternity_rate")));
        payroll.setPersonalSocialSecurity(personalSocialSecurity.setScale(2, RoundingMode.HALF_UP));

        BigDecimal companySocialSecurity = socialSecurityBase
                .multiply(rates.get("company_pension_rate"))
                .add(socialSecurityBase.multiply(rates.get("company_medical_rate")))
                .add(socialSecurityBase.multiply(rates.get("company_unemployment_rate")))
                .add(socialSecurityBase.multiply(rates.get("company_injury_rate")))
                .add(socialSecurityBase.multiply(rates.get("company_maternity_rate")))
                .add(socialSecurityBase.multiply(rates.get("company_housing_rate")));
        payroll.setCompanySocialSecurity(companySocialSecurity.setScale(2, RoundingMode.HALF_UP));

        // 计算公积金
        BigDecimal housingFundBase = calculateHousingFundBase(payroll);
        payroll.setHousingFundBase(housingFundBase);

        BigDecimal personalHousingFund = housingFundBase.multiply(rates.get("personal_housing_rate"));
        payroll.setPersonalHousingFund(personalHousingFund.setScale(2, RoundingMode.HALF_UP));

        BigDecimal companyHousingFund = housingFundBase.multiply(rates.get("company_housing_rate"));
        payroll.setCompanyHousingFund(companyHousingFund.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 计算社保基数
     */
    private BigDecimal calculateSocialSecurityBase(Payroll payroll) {
        // TODO: 实现社保基数计算逻辑
        return payroll.getGrossSalary();
    }

    /**
     * 计算公积金基数
     */
    private BigDecimal calculateHousingFundBase(Payroll payroll) {
        // TODO: 实现公积金基数计算逻辑
        return payroll.getGrossSalary();
    }

    /**
     * 获取社保公积金费率
     */
    private Map<String, BigDecimal> getSocialSecurityRates(String cityCode) {
        // 从缓存获取，缓存不存在则从数据库加载
        if (SOCIAL_SECURITY_RATE_CACHE.containsKey(cityCode)) {
            return SOCIAL_SECURITY_RATE_CACHE.get(cityCode);
        }

        // TODO: 从数据库加载社保公积金费率
        Map<String, BigDecimal> rates = new HashMap<>();
        // 这里是默认值，实际应该从数据库加载
        rates.put("personal_pension_rate", new BigDecimal(0.08));
        rates.put("personal_medical_rate", new BigDecimal(0.02));
        rates.put("personal_unemployment_rate", new BigDecimal(0.005));
        rates.put("personal_injury_rate", new BigDecimal(0));
        rates.put("personal_maternity_rate", new BigDecimal(0));
        rates.put("personal_housing_rate", new BigDecimal(0.12));

        rates.put("company_pension_rate", new BigDecimal(0.16));
        rates.put("company_medical_rate", new BigDecimal(0.09));
        rates.put("company_unemployment_rate", new BigDecimal(0.005));
        rates.put("company_injury_rate", new BigDecimal(0.002));
        rates.put("company_maternity_rate", new BigDecimal(0.008));
        rates.put("company_housing_rate", new BigDecimal(0.12));

        SOCIAL_SECURITY_RATE_CACHE.put(cityCode, rates);
        return rates;
    }

    /**
     * 计算个税
     */
    private void calculateIncomeTax(Payroll payroll, PayrollCalculationDTO calculationDTO) {
        // TODO: 实现个税计算逻辑
        payroll.setTaxableIncome(payroll.getGrossSalary()
                .subtract(payroll.getPersonalSocialSecurity())
                .subtract(payroll.getPersonalHousingFund())
                .subtract(new BigDecimal(5000)) // 起征点
                .subtract(payroll.getSpecialDeduction() != null ? payroll.getSpecialDeduction() : BigDecimal.ZERO));

        if (payroll.getTaxableIncome().compareTo(BigDecimal.ZERO) <= 0) {
            payroll.setPersonalIncomeTax(BigDecimal.ZERO);
            return;
        }

        // 简化版个税计算
        payroll.setPersonalIncomeTax(payroll.getTaxableIncome().multiply(new BigDecimal(0.1)).subtract(new BigDecimal(210)));
    }

    /**
     * 计算最终实发工资
     */
    private void calculateNetSalary(Payroll payroll) {
        // 实发工资 = 应发工资 - 个人社保 - 个人公积金 - 个税 - 其他扣除
        BigDecimal netSalary = payroll.getGrossSalary()
                .subtract(payroll.getPersonalSocialSecurity())
                .subtract(payroll.getPersonalHousingFund())
                .subtract(payroll.getPersonalIncomeTax())
                .subtract(payroll.getOtherDeductions() != null ? payroll.getOtherDeductions() : BigDecimal.ZERO);

        payroll.setNetSalary(netSalary.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 获取薪资规则配置
     */
    private Map<String, Object> getSalaryRuleConfig(String jobLevel, String cityCode) {
        // 从缓存获取，缓存不存在则从数据库加载
        String cacheKey = jobLevel + ":" + cityCode;
        if (SALARY_RULE_CACHE.containsKey(cacheKey)) {
            return (Map<String, Object>) SALARY_RULE_CACHE.get(cacheKey);
        }

        // TODO: 从数据库加载薪资规则配置
        Map<String, Object> ruleConfig = new HashMap<>();
        // 这里是默认配置，实际应该从数据库加载
        Map<String, Object> performanceConfig = new HashMap<>();
        performanceConfig.put("ratio", 0.3); // 绩效工资占基本工资的比例
        ruleConfig.put("performance_config", performanceConfig);

        SALARY_RULE_CACHE.put(cacheKey, ruleConfig);
        return ruleConfig;
    }

    /**
     * 根据工作地点获取城市编码
     */
    private String getCityCodeByWorkLocation(String workLocation) {
        // TODO: 实现根据工作地点获取城市编码的逻辑
        return "310000"; // 默认上海
    }
}