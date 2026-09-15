package com.kylexu.elibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kylexu.elibrary.common.ApiCode;
import com.kylexu.elibrary.common.BusinessException;
import com.kylexu.elibrary.dto.BorrowBookRequest;
import com.kylexu.elibrary.mapper.BookMapper;
import com.kylexu.elibrary.mapper.LoanMapper;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.Loan;
import com.kylexu.elibrary.model.LoanStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LoanService 关键路径单测：借书成功、重复借、书不存在、借阅单不存在。
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

	@Mock
	private BookMapper bookMapper;

	@Mock
	private LoanMapper loanMapper;

	@InjectMocks
	private LoanService loanService;

	@Test
	void borrowBook_success() {
		String userId = "u1";
		Long bookId = 1L;
		Book book = availableBook(bookId);

		when(bookMapper.findById(bookId)).thenReturn(book);
		when(loanMapper.findActiveByUserAndBook(userId, bookId)).thenReturn(null);
		when(loanMapper.insert(any(Loan.class))).thenReturn(1);
		when(bookMapper.decreaseAvailableCopies(bookId)).thenReturn(1);

		BorrowBookRequest request = new BorrowBookRequest();
		request.setBookId(bookId);
		request.setLoanDays(14);

		loanService.borrowBook(userId, request);

		verify(loanMapper).insert(any(Loan.class));
		verify(bookMapper).decreaseAvailableCopies(bookId);
	}

	@Test
	void borrowBook_whenAlreadyBorrowed_throwsConflict() {
		String userId = "u1";
		Long bookId = 1L;
		Book book = availableBook(bookId);
		Loan active = new Loan();
		active.setId(99L);
		active.setStatus(LoanStatus.BORROWED);

		when(bookMapper.findById(bookId)).thenReturn(book);
		when(loanMapper.findActiveByUserAndBook(userId, bookId)).thenReturn(active);

		BorrowBookRequest request = new BorrowBookRequest();
		request.setBookId(bookId);
		request.setLoanDays(14);

		BusinessException ex = assertThrows(
				BusinessException.class,
				() -> loanService.borrowBook(userId, request));

		assertEquals(ApiCode.CONFLICT, ex.getApiCode());
		assertEquals("Already borrowed", ex.getMessage());
		verify(loanMapper, never()).insert(any(Loan.class));
		verify(bookMapper, never()).decreaseAvailableCopies(any());
	}

	@Test
	void borrowBook_whenBookNotFound_throwsNotFound() {
		String userId = "u1";
		Long bookId = 404L;

		when(bookMapper.findById(bookId)).thenReturn(null);

		BorrowBookRequest request = new BorrowBookRequest();
		request.setBookId(bookId);
		request.setLoanDays(14);

		BusinessException ex = assertThrows(
				BusinessException.class,
				() -> loanService.borrowBook(userId, request));

		assertEquals(ApiCode.NOT_FOUND, ex.getApiCode());
		assertEquals("No this book", ex.getMessage());
		verify(loanMapper, never()).insert(any(Loan.class));
	}

	@Test
	void returnBook_whenLoanNotFound_throwsNotFound() {
		when(loanMapper.findById(999L)).thenReturn(null);

		BusinessException ex = assertThrows(
				BusinessException.class,
				() -> loanService.returnBook("u1", 999L));

		assertEquals(ApiCode.NOT_FOUND, ex.getApiCode());
		assertEquals("No this loan", ex.getMessage());
		verify(bookMapper, never()).increaseAvailableCopies(any());
	}

	@Test
	void returnBook_success() {
		String userId = "u1";
		Long loanId = 10L;
		Long bookId = 1L;

		Loan loan = new Loan();
		loan.setId(loanId);
		loan.setUserId(userId);
		loan.setBookId(bookId);
		loan.setStatus(LoanStatus.BORROWED);

		when(loanMapper.findById(loanId)).thenReturn(loan);
		when(loanMapper.markReturned(eq(loanId), eq(LoanStatus.RETURNED), any())).thenReturn(1);
		when(bookMapper.increaseAvailableCopies(bookId)).thenReturn(1);

		loanService.returnBook(userId, loanId);

		verify(loanMapper).markReturned(eq(loanId), eq(LoanStatus.RETURNED), any());
		verify(bookMapper).increaseAvailableCopies(bookId);
	}

	private static Book availableBook(Long bookId) {
		Book book = new Book();
		book.setId(bookId);
		book.setTitle("Clean Code");
		book.setAvailableCopies(2);
		book.setTotalCopies(2);
		return book;
	}
}
