package com.feishu.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 用于创建RestTemplate实例，供FeishuApiClient使用
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建并配置RestTemplate Bean
     * @return 配置好的RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}