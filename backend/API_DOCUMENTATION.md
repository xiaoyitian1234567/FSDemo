# 飞书API集成文档

## 项目概述
本项目提供基于Java的飞书API集成方案，实现员工管理、组织架构同步和考勤记录获取等功能。

## 环境配置
### 开发环境
- JDK 21
- MySQL 8.x
- Redis 6.x
- Windows 11

### 飞书应用配置
请通过环境变量或配置文件注入以下凭证：
- App ID: `FEISHU_APP_ID`
- App Secret: `FEISHU_APP_SECRET`

## API调用示例

### 1. 初始化飞书客户端
```java
// FeishuConfig.java中已配置，可直接注入使用
@Autowired
private FeishuClient feishuClient;
```

### 2. 创建飞书用户
```java
/**
 * 创建飞书用户
 * @param employee 员工信息
 * @return 创建结果
 */
public FeishuUser createFeishuUser(Employee employee) {
    UserCreateReq req = UserCreateReq.newBuilder()
        .setName(employee.getName())
        .setEmail(employee.getEmail())
        .setDepartmentIds(Arrays.asList(employee.getDepartmentId().toString()))
        .setJobTitle(employee.getPosition())
        .setEmploymentType(EmploymentType.FULL_TIME)
        .build();

    try {
        UserCreateResp resp = feishuClient.contact().users().create(req);
        if (resp.isSuccess()) {
            return convertToFeishuUser(resp.getData());
        } else {
            log.error("创建飞书用户失败: {}", resp.getMsg());
            throw new ServiceException("创建飞书用户失败");
        }
    } catch (Exception e) {
        log.error("飞书API调用异常", e);
        throw new ServiceException("系统异常，请稍后重试");
    }
}
```

### 3. 同步飞书组织架构
```java
/**
 * 同步飞书组织架构
 */
@Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
public void syncFeishuOrganization() {
    log.info("开始同步飞书组织架构");
    try {
        // 同步部门
        syncDepartments();
        // 同步用户
        syncUsers();
        // 处理离职员工
        handleTerminatedEmployees();
        log.info("飞书组织架构同步完成");
    } catch (Exception e) {
        log.error("飞书组织架构同步失败", e);
    }
}
```

### 4. 获取考勤记录
```java
/**
 * 获取员工考勤记录
 * @param employeeId 员工ID
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @return 考勤记录列表
 */
public List<AttendanceRecordDTO> getAttendanceRecords(Long employeeId, LocalDate startDate, LocalDate endDate) {
    Employee employee = employeeRepository.findById(employeeId)
        .orElseThrow(() -> new ResourceNotFoundException("员工不存在"));

    AttendanceListReq req = AttendanceListReq.newBuilder()
        .setUserId(employee.getFeishuUserId())
        .setCheckDateFrom(startDate.toString())
        .setCheckDateTo(endDate.toString())
        .build();

    try {
        AttendanceListResp resp = feishuClient.attendance().records().list(req);
        if (resp.isSuccess()) {
            return resp.getData().getRecords().stream()
                .map(this::convertToAttendanceRecordDTO)
                .collect(Collectors.toList());
        } else {
            log.error("获取考勤记录失败: {}", resp.getMsg());
            throw new ServiceException("获取考勤记录失败");
        }
    } catch (Exception e) {
        log.error("考勤API调用异常", e);
        throw new ServiceException("系统异常，请稍后重试");
    }
}
```

## 数据库集成
### 数据库配置
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/feishu_integration?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 主要数据表
- `employees`: 存储员工信息
- `departments`: 存储部门信息
- `attendance_records`: 存储考勤记录
- `feishu_sync_logs`: 存储飞书API同步日志

## 错误处理
| 错误码 | 描述 | 处理建议 |
|--------|------|----------|
| 4001 | 无效的访问令牌 | 检查App ID和App Secret是否正确 |
| 403 | 权限不足 | 在飞书开发者后台检查API权限配置 |
| 500 | 服务器内部错误 | 检查飞书API服务状态，稍后重试 |
| 10001 | 用户不存在 | 确认飞书用户ID是否正确 |

## 参考文档
- [飞书开放平台官方文档](https://open.feishu.cn/document/introduction)
- [飞书Java SDK使用指南](https://github.com/feishu-sdk/feishu-sdk-java)
- [Spring Boot官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
