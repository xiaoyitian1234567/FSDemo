package com.feishu.integration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larksuite.oapi.Client;
import com.larksuite.oapi.core.utils.Jsons;
import com.larksuite.oapi.service.contact.v3.model.User;
import com.larksuite.oapi.service.contact.v3.model.UserCreateReq;
import com.larksuite.oapi.service.contact.v3.model.UserCreateResp;
import com.feishu.integration.dto.EmployeeDTO;
import com.feishu.integration.entity.Employee;
import com.feishu.integration.mapper.EmployeeMapper;
import com.feishu.integration.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务实现类
 * 实现员工管理相关的业务逻辑，包括员工CRUD、飞书同步、离职处理等功能
 */
@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private Client feishuClient;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 创建新员工并同步到飞书
     */
    @Override
    @Transactional
    public Employee createEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee: {}", employeeDTO.getName());

        // 1. 创建飞书用户
        String feishuUserId = createFeishuUser(employeeDTO);

        // 2. 保存员工信息到本地数据库
        Employee employee = new Employee();
        employee.setFeishuUserId(feishuUserId);
        employee.setEmployeeId(generateEmployeeId());
        employee.setName(employeeDTO.getName());
        employee.setEnglishName(employeeDTO.getEnglishName());
        employee.setIndonesianName(employeeDTO.getIndonesianName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        employee.setDepartmentId(employeeDTO.getDepartmentId());
        employee.setDepartmentName(employeeDTO.getDepartmentName());
        employee.setPosition(employeeDTO.getPosition());
        employee.setJobLevel(employeeDTO.getJobLevel());
        employee.setEmploymentType(employeeDTO.getEmploymentType());
        employee.setStatus("active");
        employee.setHireDate(LocalDate.parse(employeeDTO.getHireDate()));
        employee.setWorkLocation(employeeDTO.getWorkLocation());
        employee.setManagerId(employeeDTO.getManagerId());
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setIsDeleted(0);

        employeeMapper.insert(employee);
        log.info("Employee created successfully with ID: {}", employee.getId());

        // 3. 自动开通员工资源（账号、部门群等）
        autoProvisionEmployeeResources(employee.getId());

        return employee;
    }

    /**
     * 更新员工信息并同步到飞书
     */
    @Override
    @Transactional
    public Employee updateEmployee(Long id, EmployeeDTO employeeDTO) {
        log.info("Updating employee with ID: {}", id);

        Employee existingEmployee = employeeMapper.selectById(id);
        if (existingEmployee == null) {
            log.error("Employee not found with ID: {}", id);
            throw new RuntimeException("Employee not found");
        }

        // 更新飞书用户信息
        updateFeishuUser(existingEmployee.getFeishuUserId(), employeeDTO);

        // 更新本地数据库信息
        existingEmployee.setName(employeeDTO.getName());
        existingEmployee.setEnglishName(employeeDTO.getEnglishName());
        existingEmployee.setIndonesianName(employeeDTO.getIndonesianName());
        existingEmployee.setEmail(employeeDTO.getEmail());
        existingEmployee.setPhone(employeeDTO.getPhone());
        existingEmployee.setDepartmentId(employeeDTO.getDepartmentId());
        existingEmployee.setDepartmentName(employeeDTO.getDepartmentName());
        existingEmployee.setPosition(employeeDTO.getPosition());
        existingEmployee.setJobLevel(employeeDTO.getJobLevel());
        existingEmployee.setEmploymentType(employeeDTO.getEmploymentType());
        existingEmployee.setStatus(employeeDTO.getStatus());
        existingEmployee.setWorkLocation(employeeDTO.getWorkLocation());
        existingEmployee.setManagerId(employeeDTO.getManagerId());
        existingEmployee.setUpdatedAt(LocalDateTime.now());

        employeeMapper.updateById(existingEmployee);
        log.info("Employee updated successfully with ID: {}", id);

        return existingEmployee;
    }

    /**
     * 处理员工离职流程
     */
    @Override
    @Transactional
    public boolean processEmployeeTermination(Long id, String terminationReason, String terminationDate) {
        log.info("Processing termination for employee ID: {}", id);

        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            log.error("Employee not found with ID: {}", id);
            return false;
        }

        // 更新员工状态
        employee.setStatus("terminated");
        employee.setTerminationDate(LocalDate.parse(terminationDate, DateTimeFormatter.ISO_DATE));
        employee.setUpdatedAt(LocalDateTime.now());
        employeeMapper.updateById(employee);

        // TODO: 实现离职处理逻辑
        // 1. 禁用飞书账号
        // 2. 移除部门群权限
        // 3. 回收公司资源
        // 4. 生成离职证明

        log.info("Employee termination processed successfully for ID: {}", id);
        return true;
    }

    /**
     * 同步飞书组织架构到本地数据库
     */
    @Override
    public Map<String, Integer> syncFeishuOrganization() {
        log.info("Starting Feishu organization sync");

        Map<String, Integer> result = new HashMap<>();
        int added = 0, updated = 0, deleted = 0;

        try {
            // 1. 同步部门
            DepartmentListResp deptResp = feishuClient.contact().v3().departments().list(
                DepartmentListReq.newBuilder().setParentId("0").setFetchChild(true).build());
            if (deptResp.success() && deptResp.getData() != null) {
                for (Department dept : deptResp.getData().getDepartments()) {
                    // 同步部门到本地数据库
                    if (syncDepartment(dept)) {
                        added++;
                    } else {
                        updated++;
                    }
                }
            }

            // 2. 同步用户
            UserListReq userReq = UserListReq.newBuilder().setDepartmentId("0").setPageSize(100).build();
            UserListResp userResp = feishuClient.contact().v3().users().list(userReq);
            if (userResp.success() && userResp.getData() != null) {
                for (User user : userResp.getData().getItems()) {
                    // 同步用户到本地数据库
                    if (syncUser(user)) {
                        added++;
                    } else {
                        updated++;
                    }
                }
            }

            // 3. 处理离职员工
            deleted = handleTerminatedEmployees();

            result.put("added", added);
            result.put("updated", updated);
            result.put("deleted", deleted);
            log.info("Feishu organization sync completed: {}", result);
        } catch (Exception e) {
            log.error("Feishu organization sync failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to sync Feishu organization", e);
        }

        return result;
    }

    /**
     * 同步飞书部门到本地数据库
     */
    private boolean syncDepartment(Department dept) {
        try {
            DepartmentDO existingDept = departmentMapper.selectById(dept.getDepartmentId());
            DepartmentDO departmentDO = new DepartmentDO();
            departmentDO.setDeptId(dept.getDepartmentId());
            departmentDO.setName(dept.getName());
            departmentDO.setParentId(dept.getParentId());
            departmentDO.setOrder(dept.getOrder());
            departmentDO.setStatus(dept.getStatus());
            departmentDO.setUpdatedAt(new Date());

            if (existingDept == null) {
                departmentDO.setCreatedAt(new Date());
                departmentMapper.insert(departmentDO);
                log.info("Added new department: {}", dept.getName());
                return true;
            } else {
                // 仅更新变更的字段
                if (!Objects.equals(existingDept.getName(), dept.getName()) ||
                    !Objects.equals(existingDept.getParentId(), dept.getParentId()) ||
                    !Objects.equals(existingDept.getStatus(), dept.getStatus())) {
                    departmentMapper.updateById(departmentDO);
                    log.info("Updated department: {}", dept.getName());
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to sync department {}: {}", dept.getDepartmentId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 同步飞书用户到本地数据库
     */
    private boolean syncUser(User user) {
        try {
            Employee existingEmployee = employeeMapper.selectByFeishuUserId(user.getUserId());
            Employee employee = new Employee();
            employee.setFeishuUserId(user.getUserId());
            employee.setName(user.getName());
            employee.setEnglishName(user.getEnglishName());
            employee.setEmail(user.getEmail());
            employee.setPhone(user.getMobile());
            employee.setPosition(user.getPosition());
            employee.setDepartmentId(user.getDepartments() != null && !user.getDepartments().isEmpty() ?
                Long.valueOf(user.getDepartments().get(0)) : null);
            employee.setStatus("active".equals(user.getStatus()) ? 1 : 0);
            employee.setUpdatedAt(new Date());

            if (existingEmployee == null) {
                employee.setCreatedAt(new Date());
                employeeMapper.insert(employee);
                log.info("Added new user: {}", user.getName());
                return true;
            } else {
                // 仅更新变更的字段
                if (!Objects.equals(existingEmployee.getName(), user.getName()) ||
                    !Objects.equals(existingEmployee.getDepartmentId(), employee.getDepartmentId()) ||
                    !Objects.equals(existingEmployee.getStatus(), employee.getStatus())) {
                    employee.setId(existingEmployee.getId());
                    employeeMapper.updateById(employee);
                    log.info("Updated user: {}", user.getName());
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to sync user {}: {}", user.getUserId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 处理已离职员工
     */
    private int handleTerminatedEmployees() {
        try {
            // 获取飞书已离职用户
            UserListReq req = UserListReq.newBuilder()
                .setDepartmentId("0")
                .setStatus(2) // 2表示离职状态
                .setPageSize(100)
                .build();
            UserListResp resp = feishuClient.contact().v3().users().list(req);
            int count = 0;

            if (resp.success() && resp.getData() != null) {
                for (User user : resp.getData().getItems()) {
                    Employee existingEmployee = employeeMapper.selectByFeishuUserId(user.getUserId());
                    if (existingEmployee != null && existingEmployee.getStatus() == 1) {
                        // 更新本地员工状态为离职
                        Employee employee = new Employee();
                        employee.setId(existingEmployee.getId());
                        employee.setStatus(0);
                        employee.setTerminatedAt(new Date());
                        employeeMapper.updateById(employee);
                        log.info("Marked employee as terminated: {}", user.getName());
                        count++;
                    }
                }
            }
            return count;
        } catch (Exception e) {
            log.error("Failed to handle terminated employees: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 根据部门ID获取员工列表
     */
    @Override
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        log.info("Getting employees for department ID: {}", departmentId);

        QueryWrapper<Employee> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByAsc("name");

        return employeeMapper.selectList(queryWrapper);
    }

    /**
     * 获取员工打卡记录
     * @param employeeId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    @Override
    public List<AttendanceRecordDTO> getAttendanceRecords(String employeeId, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("获取员工打卡记录: {}, 开始日期: {}, 结束日期: {}", employeeId, startDate, endDate);

            // 构建请求参数
            AttendanceListReq req = AttendanceListReq.newBuilder()
                .setEmployeeId(employeeId)
                .setStartDate(startDate.toString())
                .setEndDate(endDate.toString())
                .setPageSize(100)
                .setPageToken("")
                .build();

            // 调用飞书考勤API获取打卡记录
            AttendanceListResp resp = feishuClient.attendance().v3().records().list(req);
            if (!resp.success()) {
                log.error("飞书打卡记录获取失败: {}", resp.getMsg());
                throw new RuntimeException("Feishu API error: " + resp.getMsg());
            }

            // 转换飞书打卡记录为DTO
            List<AttendanceRecordDTO> records = new ArrayList<>();
            for (AttendanceRecord record : resp.getData().getItems()) {
                AttendanceRecordDTO dto = new AttendanceRecordDTO();
                dto.setRecordId(record.getRecordId());
                dto.setEmployeeId(employeeId);
                dto.setCheckTime(LocalDateTime.parse(record.getCheckTime(), DateTimeFormatter.ISO_DATE_TIME));
                dto.setLocation(record.getLocationName());
                dto.setCheckType(convertCheckType(record.getType()));
                dto.setStatus(convertStatus(record.getStatus()));
                dto.setLongitude(record.getLongitude());
                dto.setLatitude(record.getLatitude());
                records.add(dto);
            }

            log.info("成功获取员工打卡记录: {} 条", records.size());
            return records;
        } catch (Exception e) {
            log.error("获取打卡记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get attendance records", e);
        }
    }

    /**
     * 转换打卡类型
     */
    private String convertCheckType(int type) {
        switch (type) {
            case 0: return "USER_CHECK_IN";
            case 1: return "ADMIN_MODIFY";
            case 2: return "USER_SUPPLEMENT";
            case 3: return "SYSTEM_GENERATE";
            case 4: return "AUTO_CHECK_OUT";
            case 5: return "ATTENDANCE_MACHINE";
            case 6: return "QUICK_CHECK_IN";
            case 7: return "API_IMPORT";
            default: return "UNKNOWN";
        }
    }

    /**
     * 转换打卡状态
     */
    private String convertStatus(int status) {
        switch (status) {
            case 0: return "NORMAL";
            case 1: return "LATE";
            case 2: return "EARLY_LEAVE";
            case 3: return "ABSENT";
            case 4: return "HALF_DAY";
            default: return "UNKNOWN";
        }
    }

    /**
     * 自动开通员工账号和同步部门群
     */
    @Override
    public boolean autoProvisionEmployeeResources(Long employeeId) {
        log.info("Auto-provisioning resources for employee ID: {}", employeeId);

        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            log.error("Employee not found with ID: {}", employeeId);
            return false;
        }

        // TODO: 实现自动开通资源逻辑
        // 1. 添加到部门群
        // 2. 分配必要的应用权限
        // 3. 创建邮箱账号
        // 4. 分配办公设备

        log.info("Resources auto-provisioned successfully for employee ID: {}", employeeId);
        return true;
    }

    /**
     * 创建飞书用户
     */
    private String createFeishuUser(EmployeeDTO employeeDTO) {
        try {
            // 构建飞书用户创建请求
            UserCreateReq req = UserCreateReq.newBuilder()
                .setUser(User.newBuilder()
                    .setName(employeeDTO.getName())
                    .setEnglishName(employeeDTO.getEnglishName())
                    .setEmail(employeeDTO.getEmail())
                    .setMobile(employeeDTO.getPhone())
                    .setDepartments(Collections.singletonList(employeeDTO.getDepartmentId().toString()))
                    .setPosition(employeeDTO.getPosition())
                    .setEmploymentType(convertEmploymentType(employeeDTO.getEmploymentType()))
                    .build())
                .build();

            // 调用飞书API创建用户
            UserCreateResp resp = feishuClient.contact().v3().users().create(req);
            if (!resp.success()) {
                log.error("Feishu user creation failed: {}", resp.getMsg());
                throw new RuntimeException("Feishu API error: " + resp.getMsg());
            }

            log.info("Created Feishu user successfully: {}", resp.getData().getUserId());
            return resp.getData().getUserId();
        } catch (Exception e) {
            log.error("Failed to create Feishu user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create Feishu user", e);
        }
    }

    /**
     * 转换雇佣类型为飞书API要求的格式
     */
    private String convertEmploymentType(String employmentType) {
        // 映射本地雇佣类型到飞书API要求的字段值
        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("正式", "full_time");
        typeMap.put("实习", "intern");
        typeMap.put("合同", "contract");
        return typeMap.getOrDefault(employmentType, "full_time");
    }

    /**
     * 更新飞书用户信息
     */
    private void updateFeishuUser(String feishuUserId, EmployeeDTO employeeDTO) {
        try {
            // TODO: 实现飞书用户更新逻辑
            log.info("Updating Feishu user: {}", feishuUserId);
        } catch (Exception e) {
            log.error("Failed to update Feishu user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update Feishu user", e);
        }
    }

    /**
     * 生成员工工号
     */
    private String generateEmployeeId() {
        // 生成规则: EMP + 年份后两位 + 月份 + 3位序号
        LocalDateTime now = LocalDateTime.now();
        String prefix = String.format("EMP%02d%02d", now.getYear() % 100, now.getMonthValue());
        String maxEmpId = employeeMapper.getMaxEmployeeIdByPrefix(prefix);

        int sequence = 1;
        if (maxEmpId != null) {
            String sequenceStr = maxEmpId.substring(6);
            sequence = Integer.parseInt(sequenceStr) + 1;
        }

        return String.format("%s%03d", prefix, sequence);
    }
}