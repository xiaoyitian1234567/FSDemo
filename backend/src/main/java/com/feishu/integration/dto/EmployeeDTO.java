package com.feishu.integration.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 员工数据传输对象
 * 用于Controller层与Service层之间的数据传递
 */
@Data
public class EmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long departmentId;
    private String position;
    private Integer status;
    private LocalDate hireDate;
    private LocalDate terminationDate;
}