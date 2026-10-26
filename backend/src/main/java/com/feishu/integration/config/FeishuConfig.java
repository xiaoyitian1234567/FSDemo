package com.feishu.integration.config;

import com.larksuite.oapi.Client;
import com.larksuite.oapi.core.Config;
import com.larksuite.oapi.core.api.request.RequestOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feishu API客户端配置类
 * 初始化Feishu API客户端，用于所有飞书开放平台接口调用
 */
@Configuration
public class FeishuConfig {

    @Value("${feishu.app.id:${feishu.app-id:}}")
    private String appId;

    @Value("${feishu.app.secret:${feishu.app-secret:}}")
    private String appSecret;

    @Value("${feishu.api-base-url}")
    private String apiBaseUrl;

    @Value("${feishu.event-verify-token}")
    private String eventVerifyToken;

    @Value("${feishu.encrypt-key}")
    private String encryptKey;

    /**
     * 创建Feishu API客户端实例
     * @return Feishu API客户端
     */
    @Bean
    public Client feishuClient() {
        // 创建配置对象
        Config config = Config.newBuilder(appId, appSecret)
                .setDomain(apiBaseUrl)
                .setEventVerifyToken(eventVerifyToken)
                .setEncryptKey(encryptKey)
                .build();
        
        // 创建并返回客户端实例
        return new Client(config);
    }

    /**
     * 创建默认的请求选项
     * @return 默认请求选项
     */
    @Bean
    public RequestOptions defaultRequestOptions() {
        return RequestOptions.newBuilder()
                .setTimeout(3000) // 3秒超时
                .build();
    }
}
