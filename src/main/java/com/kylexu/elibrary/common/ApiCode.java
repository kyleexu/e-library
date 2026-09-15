package com.kylexu.elibrary.common;

/**
 * 业务状态码。
 */
public enum ApiCode {

	/** 成功。 */
	SUCCESS(0, "success"),

	/** 请求参数错误。 */
	BAD_REQUEST(400, "error"),

	/** 资源不存在。 */
	NOT_FOUND(404, "error"),

	/** 业务冲突（如库存不足、重复借阅、已归还）。 */
	CONFLICT(409, "error"),

	/** 服务器内部错误。 */
	INTERNAL_ERROR(500, "error");

	private final int code;
	private final String status;

	ApiCode(int code, String status) {
		this.code = code;
		this.status = status;
	}

	public int getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}
}
