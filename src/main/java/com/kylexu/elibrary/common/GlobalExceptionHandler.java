package com.kylexu.elibrary.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将业务/系统异常转为统一 ApiResponse。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ApiResponse<Void> handleBusinessException(BusinessException ex) {
		log.warn("Business error: code={}, message={}", ex.getApiCode(), ex.getMessage());
		return ApiResponse.fail(ex.getApiCode(), ex.getMessage());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ApiResponse<Void> handleMissingHeader(MissingRequestHeaderException ex) {
		return ApiResponse.fail(ApiCode.BAD_REQUEST, "Missing header: " + ex.getHeaderName());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
		return ApiResponse.fail(ApiCode.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(RuntimeException.class)
	public ApiResponse<Void> handleRuntimeException(RuntimeException ex) {
		log.error("Unexpected runtime error", ex);
		return ApiResponse.fail(ApiCode.INTERNAL_ERROR, ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ApiResponse<Void> handleException(Exception ex) {
		log.error("Unexpected error", ex);
		return ApiResponse.fail(ApiCode.INTERNAL_ERROR, "Internal error");
	}
}
