package com.feishu.integration.dto;

import lombok.Data;

/**
 * API统一响应DTO
 * 定义所有REST API的标准响应格式，包含状态码、消息和数据体
 */
@Data
public class ApiResponse<T> {
    /**
     * 响应状态码
     * 200: 成功
     * 500: 服务器内部错误
     * 400: 请求参数错误
     * 401: 未授权
     * 403: 权限不足
     * 404: 资源不存在
     */
    private int code;

    /**
     * 响应消息
     * 成功时为"success"，失败时为具体错误信息
     */
    private String message;

    /**
     * 响应数据体
     * 成功时返回具体业务数据，失败时为null
     */
    private T data;

    /**
     * 时间戳
     * 响应生成的时间（毫秒）
     */
    private long timestamp = System.currentTimeMillis();
}