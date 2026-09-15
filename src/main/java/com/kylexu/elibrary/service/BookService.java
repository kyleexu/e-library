package com.kylexu.elibrary.service;

import com.kylexu.elibrary.common.ApiCode;
import com.kylexu.elibrary.common.BusinessException;
import com.kylexu.elibrary.dto.BookBorrowerItem;
import com.kylexu.elibrary.mapper.BookMapper;
import com.kylexu.elibrary.mapper.LoanMapper;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.BookType;
import com.kylexu.elibrary.model.Loan;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 书籍领域服务：浏览书籍、查询书籍详情、查询当前借出用户。
 */
@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private LoanMapper loanMapper;

    /**
     * 浏览书籍列表。
     *
     * @param q    可选关键字（标题 / 作者）
     * @param type 可选类型过滤
     */
    public List<Book> listBooks(String q, BookType type) {
        return bookMapper.findList(q, type);
    }

    /**
     * 查询书籍详细资料。
     *
     * @param bookId 书籍 ID
     */
    public Book getBook(Long bookId) {
        return bookMapper.findById(bookId);
    }

    /**
     * 查询某本书当前借出在哪些用户手里。
     *
     * @param bookId 书籍 ID
     */
    public List<BookBorrowerItem> listCurrentBorrowers(Long bookId) {
        Book book = bookMapper.findById(bookId);
        if (book == null) {
            throw new BusinessException(ApiCode.NOT_FOUND, "No this book");
        }
        List<Loan> loans = loanMapper.findCurrentByBookId(bookId);
        if (loans == null || loans.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate today = LocalDate.now();
        return loans.stream()
                .map(loan -> toBorrowerItem(loan, today))
                .collect(Collectors.toList());
    }

    private BookBorrowerItem toBorrowerItem(Loan loan, LocalDate today) {
        BookBorrowerItem item = new BookBorrowerItem();
        item.setLoanId(loan.getId());
        item.setBookId(loan.getBookId());
        item.setUserId(loan.getUserId());
        item.setBorrowedAt(loan.getBorrowedAt());
        item.setDueAt(loan.getDueAt());
        if (loan.getBorrowedAt() != null) {
            item.setBorrowedDays(ChronoUnit.DAYS.between(loan.getBorrowedAt().toLocalDate(), today));
        }
        return item;
    }
}
