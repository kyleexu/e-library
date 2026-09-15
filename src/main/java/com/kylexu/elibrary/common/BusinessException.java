package com.kylexu.elibrary.common;

/**
 * 业务异常，由全局异常处理器转为统一 ApiResponse。
 */
public class BusinessException extends RuntimeException {

	private final ApiCode apiCode;

	public BusinessException(ApiCode apiCode, String message) {
		super(message);
		this.apiCode = apiCode;
	}

	public ApiCode getApiCode() {
		return apiCode;
	}
}
