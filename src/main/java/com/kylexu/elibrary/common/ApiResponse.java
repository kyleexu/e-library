package com.kylexu.elibrary.common;

import lombok.Data;

/**
 * 统一 API 响应体：code / status / message / data。
 *
 * @param <T> data 类型
 */
@Data
public class ApiResponse<T> {

	/** 业务码，0 表示成功。 */
	private int code;

	/** 状态文案，如 success / error。 */
	private String status;

	/** 提示信息。 */
	private String message;

	/** 业务数据。 */
	private T data;

	public static <T> ApiResponse<T> success(T data) {
		return of(ApiCode.SUCCESS, "OK", data);
	}

	public static <T> ApiResponse<T> success() {
		return success(null);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return of(ApiCode.SUCCESS, message, data);
	}

	public static <T> ApiResponse<T> fail(ApiCode apiCode, String message) {
		return of(apiCode, message, null);
	}

	public static <T> ApiResponse<T> fail(int code, String status, String message) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setCode(code);
		response.setStatus(status);
		response.setMessage(message);
		response.setData(null);
		return response;
	}

	private static <T> ApiResponse<T> of(ApiCode apiCode, String message, T data) {
		ApiResponse<T> response = new ApiResponse<>();
		response.setCode(apiCode.getCode());
		response.setStatus(apiCode.getStatus());
		response.setMessage(message);
		response.setData(data);
		return response;
	}
}
