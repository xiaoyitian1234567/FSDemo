package com.feishu.integration.service;

import com.feishu.integration.dto.EmployeeDTO;
import com.feishu.integration.entity.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

/**
 * 员工服务接口
 * 定义员工管理相关的业务方法，包括CRUD、状态变更、飞书同步等功能
 */
public interface EmployeeService extends IService<Employee> {
    
    /**
     * 创建新员工并同步到飞书
     * @param employeeDTO 员工信息DTO
     * @return 创建成功的员工实体
     */
    Employee createEmployee(EmployeeDTO employeeDTO);
    
    /**
     * 更新员工信息并同步到飞书
     * @param id 员工ID
     * @param employeeDTO 员工信息DTO
     * @return 更新后的员工实体
     */
    Employee updateEmployee(Long id, EmployeeDTO employeeDTO);
    
    /**
     * 处理员工离职流程
     * @param id 员工ID
     * @param terminationReason 离职原因
     * @param terminationDate 离职日期
     * @return 处理结果
     */
    boolean processEmployeeTermination(Long id, String terminationReason, String terminationDate);
    
    /**
     * 同步飞书组织架构到本地数据库
     * @return 同步结果，包含新增、更新、删除的员工数量
     */
    Map<String, Integer> syncFeishuOrganization();
    
    /**
     * 根据部门ID获取员工列表
     * @param departmentId 部门ID
     * @return 员工列表
     */
    List<Employee> getEmployeesByDepartment(Long departmentId);
    
    /**
     * 获取员工打卡记录
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    List<Map<String, Object>> getAttendanceRecords(Long employeeId, String startDate, String endDate);
    
    /**
     * 自动开通员工账号和同步部门群
     * @param employeeId 员工ID
     * @return 操作结果
     */
    boolean autoProvisionEmployeeResources(Long employeeId);
}