package com.feishu.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 飞书员工-薪酬-财务系统集成应用主类
 * 作为Spring Boot应用的入口点，启动整个后端服务
 */
@SpringBootApplication
@MapperScan("com.feishu.integration.mapper") // 扫描MyBatis Mapper接口
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}