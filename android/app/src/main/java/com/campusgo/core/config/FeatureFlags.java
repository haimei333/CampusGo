package com.campusgo.core.config;

/**
 * 功能开关（源码常量，IDE 可索引）。
 * 与 {@code app/build.gradle} 中 {@code buildConfigField} 保持一致。
 */
public final class FeatureFlags {

    /** 模拟器访问本机后端；真机联调改为电脑局域网 IP */
    public static final String API_BASE_URL = "http://10.0.2.2:8080/";

    /** true = 走 Retrofit 真实 API；false = 本地 Mock */
    public static final boolean USE_REMOTE_API = true;

    /** 与 BuildConfig.DEBUG 对齐，Debug 包为 true */
    public static final boolean DEBUG = true;

    private FeatureFlags() {
    }
}
