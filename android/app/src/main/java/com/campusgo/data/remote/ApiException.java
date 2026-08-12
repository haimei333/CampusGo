package com.campusgo.data.remote;

/**
 * 业务或网络层异常，携带后端 code。
 */
public class ApiException extends Exception {

    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
