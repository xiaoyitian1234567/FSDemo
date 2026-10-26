package com.feishu.integration.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 飞书API客户端工具类（不依赖SDK，直接HTTP调用）
 * 实现访问令牌管理和API请求封装
 */
@Component
public class FeishuApiClient {
    // 注入配置参数
    @Value("${feishu.app.id}")
    private String appId;

    @Value("${feishu.app.secret}")
    private String appSecret;

    private final RestTemplate restTemplate;
    private final Gson gson;
    private final Map<String, String> tokenCache = new ConcurrentHashMap<>();

    // 构造函数注入依赖
    public FeishuApiClient(RestTemplate restTemplate, Gson gson) {
        this.restTemplate = restTemplate;
        this.gson = gson;
    }

    /**
     * 获取飞书API访问令牌
     * 自动处理令牌缓存和刷新
     */
    public String getAccessToken() {
        // 检查缓存中是否有有效令牌
        if (tokenCache.containsKey("access_token")) {
            return tokenCache.get("access_token");
        }

        // 构建请求参数
        Map<String, String> params = new HashMap<>();
        params.put("app_id", appId);
        params.put("app_secret", appSecret);

        // 发送请求获取令牌
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal",
            request,
            String.class
        );

        // 解析响应并缓存令牌
        Map<String, Object> result = gson.fromJson(response.getBody(), Map.class);
        String accessToken = (String) result.get("tenant_access_token");
        tokenCache.put("access_token", accessToken);

        // 设置令牌过期时间（实际应该根据返回的expire字段设置定时清除）
        new Thread(() -> {
            try {
                Thread.sleep(((Double) result.get("expire")).longValue() * 1000);
                tokenCache.remove("access_token");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return accessToken;
    }

    /**
     * 发送GET请求到飞书API
     * @param apiPath API路径（如：/contact/v3/users）
     * @param params 请求参数
     * @return API响应结果
     */
    public Map<String, Object> get(String apiPath, Map<String, String> params) {
        HttpHeaders headers = createHeaders();
        HttpEntity<?> request = new HttpEntity<>(headers);

        // 构建完整URL
        StringBuilder url = new StringBuilder("https://open.feishu.cn/open-apis");
        url.append(apiPath);

        // 添加查询参数
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((k, v) -> url.append(k).append("=").append(v).append("&")
            );
            url.deleteCharAt(url.length() - 1);
        }

        ResponseEntity<String> response = restTemplate.exchange(
            url.toString(),
            HttpMethod.GET,
            request,
            String.class
        );

        return gson.fromJson(response.getBody(), Map.class);
    }

    /**
     * 发送POST请求到飞书API
     * @param apiPath API路径
     * @param body 请求体
     * @return API响应结果
     */
    public Map<String, Object> post(String apiPath, Object body) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Object> request = new HttpEntity<>(body, headers);

        String url = "https://open.feishu.cn/open-apis" + apiPath;
        ResponseEntity<String> response = restTemplate.postForEntity(
            url,
            request,
            String.class
        );

        return gson.fromJson(response.getBody(), Map.class);
    }

    /**
     * 创建带有认证信息的请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());
        return headers;
    }
}