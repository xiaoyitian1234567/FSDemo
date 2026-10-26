package com.feishu.integration.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国际化工具类，用于获取不同语言环境的消息
 */
public class I18nUtil {
    private static final String BASE_NAME = "i18n.messages";
    private static final Locale DEFAULT_LOCALE = Locale.CHINA;

    /**
     * 获取默认语言环境的消息
     * @param key 消息键
     * @return 本地化消息
     */
    public static String getMessage(String key) {
        return getMessage(key, DEFAULT_LOCALE);
    }

    /**
     * 获取指定语言环境的消息
     * @param key 消息键
     * @param locale 语言环境
     * @return 本地化消息
     */
    public static String getMessage(String key, Locale locale) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, locale);
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // 找不到资源时返回键本身
        }
    }

    /**
     * 获取带参数的默认语言环境消息
     * @param key 消息键
     * @param args 消息参数
     * @return 格式化后的本地化消息
     */
    public static String getMessage(String key, Object... args) {
        return getMessage(key, DEFAULT_LOCALE, args);
    }

    /**
     * 获取带参数的指定语言环境消息
     * @param key 消息键
     * @param locale 语言环境
     * @param args 消息参数
     * @return 格式化后的本地化消息
     */
    public static String getMessage(String key, Locale locale, Object... args) {
        String message = getMessage(key, locale);
        return String.format(message, args);
    }
}