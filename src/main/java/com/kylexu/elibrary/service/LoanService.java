package com.kylexu.elibrary.service;

import com.kylexu.elibrary.mapper.BookMapper;
import com.kylexu.elibrary.mapper.LoanMapper;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.Loan;
import com.kylexu.elibrary.model.LoanStatus;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 借阅领域服务：借阅、归还、查询当前用户已借阅列表。
 */
@Service
public class LoanService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private LoanMapper loanMapper;

    /**
     * 借阅书籍。
     *
     * @param userId 当前用户 ID
     * @param bookId 书籍 ID
     */
    public void borrowBook(String userId, Long bookId) {
        // 1. 查看是否存在这个书，不存在 --> 异常
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            throw new RuntimeException("No this book");
        }
        // 2. 查看当前还有没有库存，没有库存了 --> 异常
        if (book.getAvailableCopies() == 0) {
            throw new RuntimeException("Not Available");
        }
        // 3. 构建 loan 对象，并写入
        Loan loan = this.buildLoan(userId, book);
        loanMapper.insert(loan);
        // 4. 借出时，扣减书本数量
        bookMapper.decreaseAvailableCopies(bookId);
    }

    private Loan buildLoan(String userId, Book book) {
        Loan loan = new Loan();
        // TODO: full all field to the object.
        return loan;
    }

    /**
     * 归还书籍。
     *
     * @param userId 当前用户 ID
     * @param loanId 借阅单 ID
     */
    public void returnBook(String userId, Long loanId) {
        // 1. 查看是否存在这个借书记录，不存在 --> 异常
        Loan loan = loanMapper.findById(loanId);
        if (loan == null) {
            throw new RuntimeException("No this loan");
        }
        // 2. 只能归还自己的借阅单
        if (!userId.equals(loan.getUserId())) {
            throw new RuntimeException("Not your loan");
        }
        // 3. 已归还则不能再还
        if (loan.getStatus() != LoanStatus.BORROWED) {
            throw new RuntimeException("Already returned");
        }
        // 4. 修改 loan 状态为已归还
        int updated = loanMapper.markReturned(loanId, LoanStatus.RETURNED, LocalDateTime.now());
        if (updated == 0) {
            throw new RuntimeException("Already returned");
        }
        // 5. 对应书本剩余本数 + 1
        bookMapper.increaseAvailableCopies(loan.getBookId());
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
