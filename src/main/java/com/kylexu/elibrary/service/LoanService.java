package com.kylexu.elibrary.service;

import com.kylexu.elibrary.dto.BorrowBookRequest;
import com.kylexu.elibrary.dto.CurrentLoanItem;
import com.kylexu.elibrary.mapper.BookMapper;
import com.kylexu.elibrary.mapper.LoanMapper;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.Loan;
import com.kylexu.elibrary.model.LoanStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 借阅领域服务：借阅、归还、查询当前用户已借阅列表。
 */
@Service
public class LoanService {

    // 借阅天数默认 14 天
    private static final int LOAN_DAYS = 14;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private LoanMapper loanMapper;

    /**
     * 借阅书籍。
     *
     * @param request 借阅请求（userId、bookId、借阅天数）
     */
    public void borrowBook(String userId, BorrowBookRequest request) {
        Long bookId = request.getBookId();
        int loanDays = resolveLoanDays(request.getLoanDays());

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
        Loan loan = this.buildLoan(userId, book, loanDays);
        loanMapper.insert(loan);
        // 4. 借出时，扣减书本数量
        int i = bookMapper.decreaseAvailableCopies(bookId);
        if (i == 0) {
            throw new RuntimeException("借出失败");
        }
    }

    private int resolveLoanDays(Integer loanDays) {
        if (loanDays == null) {
            return LOAN_DAYS;
        }
        if (loanDays != 14 && loanDays != 30) {
            throw new RuntimeException("loanDays must be 14 or 30");
        }
        return loanDays;
    }

    private Loan buildLoan(String userId, Book book, int loanDays) {
        LocalDateTime now = LocalDateTime.now();
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setBookId(book.getId());
        loan.setStatus(LoanStatus.BORROWED);
        loan.setBorrowedAt(now);
        loan.setDueAt(now.plusDays(loanDays));
        loan.setReturnedAt(null);
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
        int i = bookMapper.increaseAvailableCopies(loan.getBookId());
        if (i == 0) {
            throw new RuntimeException("归还失败");
        }
    }

    /**
     * 查询当前用户已借阅的书籍列表（含借阅时间、已借天数等）。
     *
     * @param userId 当前用户 ID
     */
    public List<CurrentLoanItem> listCurrentLoans(String userId) {
        List<Loan> loanList = loanMapper.findCurrentByUserId(userId);
        if (loanList == null || loanList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> bookIds = loanList.stream()
                .map(Loan::getBookId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (bookIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Book> bookMap = bookMapper.batchGet(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity(), (a, b) -> a));

        LocalDate today = LocalDate.now();
        return loanList.stream()
                .map(loan -> toCurrentLoanItem(loan, bookMap.get(loan.getBookId()), today))
                .collect(Collectors.toList());
    }

    private CurrentLoanItem toCurrentLoanItem(Loan loan, Book book, LocalDate today) {
        CurrentLoanItem item = new CurrentLoanItem();
        item.setLoanId(loan.getId());
        item.setBookId(loan.getBookId());
        item.setBorrowedAt(loan.getBorrowedAt());
        item.setDueAt(loan.getDueAt());
        if (book != null) {
            item.setTitle(book.getTitle());
            item.setAuthor(book.getAuthor());
        }
        if (loan.getBorrowedAt() != null) {
            item.setBorrowedDays(ChronoUnit.DAYS.between(loan.getBorrowedAt().toLocalDate(), today));
        }
        return item;
    }
}
