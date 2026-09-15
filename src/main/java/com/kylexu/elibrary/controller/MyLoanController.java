package com.kylexu.elibrary.controller;

import com.kylexu.elibrary.common.ApiResponse;
import com.kylexu.elibrary.dto.CurrentLoanItem;
import com.kylexu.elibrary.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户借阅查询接口。
 */
@RestController
@RequestMapping("/api/me/loans")
public class MyLoanController {

    @Autowired
    private LoanService loanService;

    /**
     * 查看当前已借阅的书籍列表（含借阅时间、已借天数等）。
     *
     * @param userId 当前用户 ID（请求头 X-User-Id）
     */
    @GetMapping
    public ApiResponse<List<CurrentLoanItem>> listMyLoans(
            @RequestHeader("X-User-Id") String userId) {
        List<CurrentLoanItem> currentLoanItemList = loanService.listCurrentLoans(userId);
        return ApiResponse.success(currentLoanItemList);
    }
}
