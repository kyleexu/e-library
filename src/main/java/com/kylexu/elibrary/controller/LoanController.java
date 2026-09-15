package com.kylexu.elibrary.controller;

import com.kylexu.elibrary.common.ApiResponse;
import com.kylexu.elibrary.dto.BorrowBookRequest;
import com.kylexu.elibrary.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 借阅相关接口：借阅书籍、归还书籍。
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * 借阅书籍。
     *
     * @param request 借阅请求（userId、bookId、借阅天数）
     */
    @PostMapping
    public ApiResponse<Void> borrowBook(@RequestHeader("X-User-Id") String userId, @RequestBody BorrowBookRequest request) {
        loanService.borrowBook( userId,request);
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
        loanService.returnBook(userId, loanId);
        return ApiResponse.success();
    }
}
