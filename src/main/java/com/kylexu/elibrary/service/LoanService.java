package com.kylexu.elibrary.service;

import org.springframework.stereotype.Service;

/**
 * 借阅领域服务：借阅、归还、查询当前用户已借阅列表。
 */
@Service
public class LoanService {

    /**
     * 借阅书籍。
     *
     * @param userId 当前用户 ID
     * @param bookId 书籍 ID
     */
    public void borrowBook(String userId, Long bookId) {
        // TODO: 实现借阅
        // 1. 查看是否存在这个书，不存在 --> 异常
        // 2. 构建 loan 的对象，然后存入数据库，持久化
    }

    /**
     * 归还书籍。
     *
     * @param userId 当前用户 ID
     * @param loanId 借阅单 ID
     */
    public void returnBook(String userId, Long loanId) {
        // TODO: 实现归还
        // 1. 查看是否存在这个借书记录，不存在 --> 异常
        // 2. 修改当前 loan 对象，然后修改这个对象然后插入

    }

    /**
     * 查询当前用户已借阅的书籍列表（含借阅时间、已借天数等）。
     *
     * @param userId 当前用户 ID
     */
    public void listCurrentLoans(String userId) {
        // TODO: 实现我的借阅列表

    }
}
