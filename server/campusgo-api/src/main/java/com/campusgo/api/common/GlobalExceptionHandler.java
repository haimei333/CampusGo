package com.campusgo.api.common;

import com.campusgo.domain.exception.BusinessException;
import com.campusgo.domain.exception.ErrorCodes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return withTrace(ApiResponse.fail(ex.getCode(), ex.getMessage()), request);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleValidation(Exception ex, HttpServletRequest request) {
        return withTrace(ApiResponse.fail(ErrorCodes.VALIDATION, "参数校验失败"), request);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnknown(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error", ex);
        return withTrace(ApiResponse.fail(ErrorCodes.INTERNAL, "服务器内部错误"), request);
    }

    private static ApiResponse<Void> withTrace(ApiResponse<Void> response, HttpServletRequest request) {
        response.setTraceId(request.getHeader("X-Trace-Id"));
        return response;
    }
}
