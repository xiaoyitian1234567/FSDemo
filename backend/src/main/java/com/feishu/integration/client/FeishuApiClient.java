package com.feishu.integration.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 飞书API客户端（OkHttp实现）
 * 替代官方SDK，使用OkHttp直接调用飞书API接口
 */
@Component("okHttpFeishuApiClient")
public class FeishuApiClient {
    private final OkHttpClient httpClient;
    private final String appId;
    private final String appSecret;
    private final String baseUrl;
    private String accessToken;
    private long tokenExpireTime;

    /**
     * 初始化飞书API客户端
     * @param appId 飞书应用ID
     * @param appSecret 飞书应用密钥
     * @param domain 飞书API域名
     */
    public FeishuApiClient(
            @Value("${feishu.app-id}") String appId,
            @Value("${feishu.app-secret}") String appSecret,
            @Value("${feishu.domain:https://open.feishu.cn}") String domain) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.baseUrl = domain + "/open-apis";

        // 初始化OkHttpClient
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 获取访问令牌（自动处理令牌过期）
     */
    private String getAccessToken() throws IOException {
        // 检查令牌是否有效
        if (System.currentTimeMillis() < tokenExpireTime - 60000) {
            return accessToken;
        }

        // 请求新的访问令牌
        JSONObject requestBody = new JSONObject();
        requestBody.put("app_id", appId);
        requestBody.put("app_secret", appSecret);

        Request request = new Request.Builder()
                .url(baseUrl + "/auth/v3/tenant_access_token/internal/")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), requestBody.toString()))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取访问令牌失败: " + response.code() + " " + response.message());
            }

            JSONObject responseBody = JSON.parseObject(response.body().string());
            if (responseBody.getIntValue("code") != 0) {
                throw new IOException("获取访问令牌失败: " + responseBody.getString("msg"));
            }

            // 更新令牌和过期时间
            accessToken = responseBody.getString("tenant_access_token");
            tokenExpireTime = System.currentTimeMillis() + (responseBody.getLongValue("expire") * 1000);
            return accessToken;
        }
    }

    /**
     * 获取飞书用户详情
     * @param userId 飞书用户ID（open_id/user_id/union_id）
     * @return 用户详情JSON对象
     */
    public JSONObject getUserDetail(String userId) throws IOException {
        String token = getAccessToken();

        Request request = new Request.Builder()
                .url(baseUrl + "/contact/v3/users/" + userId + "?user_id_type=open_id")
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("获取用户详情失败: " + response.code() + " " + response.message());
            }

            JSONObject responseBody = JSON.parseObject(response.body().string());
            if (responseBody.getIntValue("code") != 0) {
                throw new IOException("获取用户详情失败: " + responseBody.getString("msg"));
            }

            return responseBody.getJSONObject("data").getJSONObject("user");
        }
    }

    /**
     * 同步飞书部门列表
     * @param departmentId 部门ID，根部门为"0"
     * @return 部门列表JSON数组
     */
    public JSONObject syncDepartments(String departmentId) throws IOException {
        String token = getAccessToken();

        Request request = new Request.Builder()
                .url(baseUrl + "/contact/v3/departments/" + departmentId + "/children?fetch_child=true")
                .header("Authorization", "Bearer " + token)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("同步部门失败: " + response.code() + " " + response.message());
            }

            JSONObject responseBody = JSON.parseObject(response.body().string());
            if (responseBody.getIntValue("code") != 0) {
                throw new IOException("同步部门失败: " + responseBody.getString("msg"));
            }

            return responseBody.getJSONObject("data");
        }
    }
}
