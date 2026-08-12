package com.campusgo.domain.exception;

/**
 * 与后端架构文档 §5.2 业务码对齐。
 */
public final class ErrorCodes {

    public static final int OK = 0;
    public static final int VALIDATION = 40001;
    public static final int UNAUTHORIZED = 40100;
    public static final int TOKEN_EXPIRED = 40101;
    public static final int FORBIDDEN = 40300;
    public static final int NOT_FOUND = 40400;
    public static final int CONFLICT = 40900;
    public static final int INSUFFICIENT_BALANCE = 40901;
    public static final int INVALID_STATE = 40902;
    public static final int TOO_MANY_REQUESTS = 42900;
    public static final int INTERNAL = 50000;

    private ErrorCodes() {
    }
}
