package com.feishu.integration.controller;

import com.feishu.integration.dto.EmployeeDTO;
import com.feishu.integration.entity.Employee;
import com.feishu.integration.service.EmployeeService;
import com.feishu.integration.util.ResultUtil;
import com.feishu.integration.util.I18nUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 员工管理控制器
 * 提供员工信息的CRUD操作、组织架构同步、考勤记录查询等REST API接口
 */
@RestController
@RequestMapping("/employees")
@Api(tags = "员工管理接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 创建新员工
     */
    @PostMapping
    @ApiOperation("创建新员工")
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeDTO employeeDTO) {
        try {
            Employee employee = employeeService.createEmployee(employeeDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResultUtil.success(employee, I18nUtil.getMessage("employee.add.success")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 更新员工信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新员工信息")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO employeeDTO) {
        try {
            Employee updatedEmployee = employeeService.updateEmployee(id, employeeDTO);
            return ResponseEntity.ok(
                    ResultUtil.success(updatedEmployee, I18nUtil.getMessage("employee.update.success")));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResultUtil.error(I18nUtil.getMessage("employee.not_found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取员工详情")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getById(id);
            if (employee == null || employee.getIsDeleted() == 1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResultUtil.error(I18nUtil.getMessage("employee.not_found")));
            }
            return ResponseEntity.ok(ResultUtil.success(employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除员工")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getById(id);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResultUtil.error(I18nUtil.getMessage("employee.not_found")));
            }
            // 逻辑删除
            employee.setIsDeleted(1);
            employeeService.updateById(employee);
            return ResponseEntity.ok(
                    ResultUtil.success(null, I18nUtil.getMessage("employee.delete.success")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 处理员工离职
     */
    @PostMapping("/{id}/terminate")
    @ApiOperation("处理员工离职")
    public ResponseEntity<?> terminateEmployee(
            @PathVariable Long id,
            @RequestParam String terminationReason,
            @RequestParam String terminationDate) {
        try {
            boolean result = employeeService.processEmployeeTermination(id, terminationReason, terminationDate);
            if (result) {
                return ResponseEntity.ok(ResultUtil.success(null, I18nUtil.getMessage("employee.termination.success")));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ResultUtil.error("Failed to process employee termination"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResultUtil.error(I18nUtil.getMessage("employee.not_found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 同步飞书组织架构
     */
    @PostMapping("/sync-organization")
    @ApiOperation("同步飞书组织架构")
    public ResponseEntity<?> syncFeishuOrganization() {
        try {
            Map<String, Integer> syncResult = employeeService.syncFeishuOrganization();
            return ResponseEntity.ok(ResultUtil.success(syncResult, I18nUtil.getMessage("organization.sync.success")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 根据部门获取员工列表
     */
    @GetMapping("/department/{departmentId}")
    @ApiOperation("根据部门获取员工列表")
    public ResponseEntity<?> getEmployeesByDepartment(@PathVariable Long departmentId) {
        try {
            List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
            return ResponseEntity.ok(ResultUtil.success(employees));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }

    /**
     * 获取员工考勤记录
     */
    @GetMapping("/{id}/attendance")
    @ApiOperation("获取员工考勤记录")
    public ResponseEntity<?> getAttendanceRecords(
            @PathVariable Long id,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Map<String, Object>> records = employeeService.getAttendanceRecords(id, startDate, endDate);
            return ResponseEntity.ok(ResultUtil.success(records));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResultUtil.error(I18nUtil.getMessage("employee.not_found")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResultUtil.error(I18nUtil.getMessage("common.error") + ": " + e.getMessage()));
        }
    }
}