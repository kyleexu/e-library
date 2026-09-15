package com.kylexu.elibrary.controller;

import com.kylexu.elibrary.common.ApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 借阅相关接口：借阅书籍、归还书籍。
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

	/**
	 * 借阅书籍。
	 *
	 * @param userId 当前用户 ID（请求头 X-User-Id）
	 * @param body   借阅请求体，包含 bookId
	 */
	@PostMapping
	public ApiResponse<Void> borrowBook(
			@RequestHeader("X-User-Id") String userId,
			@RequestBody Object body) {
		return ApiResponse.success();
	}

	/**
	 * 归还书籍。
	 *
	 * @param loanId 借阅单 ID
	 * @param userId 当前用户 ID（请求头 X-User-Id）
	 */
	@PostMapping("/{loanId}/return")
	public ApiResponse<Void> returnBook(
			@PathVariable Long loanId,
			@RequestHeader("X-User-Id") String userId) {
		return ApiResponse.success();
	}
}
