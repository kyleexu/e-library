package com.kylexu.elibrary.controller;

import com.kylexu.elibrary.common.ApiCode;
import com.kylexu.elibrary.common.ApiResponse;
import com.kylexu.elibrary.common.BusinessException;
import com.kylexu.elibrary.dto.BookBorrowerItem;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.BookType;
import com.kylexu.elibrary.service.BookService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 书籍相关接口：浏览书籍、查询书籍详情、查询当前借出用户。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

	@Autowired
	private BookService bookService;

	/**
	 * 浏览书籍列表。
	 *
	 * @param q    可选关键字（标题 / 作者）
	 * @param type 可选类型过滤
	 */
	@GetMapping
	public ApiResponse<List<Book>> listBooks(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String type) {
		BookType bookType = parseBookType(type);
		List<Book> books = bookService.listBooks(q, bookType);
		return ApiResponse.success(books);
	}

	/**
	 * 查询书籍详细资料。
	 *
	 * @param bookId 书籍 ID
	 */
	@GetMapping("/{bookId}")
	public ApiResponse<Book> getBook(@PathVariable Long bookId) {
		Book book = bookService.getBook(bookId);
		if (book == null) {
			throw new BusinessException(ApiCode.NOT_FOUND, "No this book");
		}
		return ApiResponse.success(book);
	}

	/**
	 * 查询某本书当前借出在哪些用户手里。
	 *
	 * @param bookId 书籍 ID
	 */
	@GetMapping("/{bookId}/borrowers")
	public ApiResponse<List<BookBorrowerItem>> listBorrowers(@PathVariable Long bookId) {
		return ApiResponse.success(bookService.listCurrentBorrowers(bookId));
	}

	private BookType parseBookType(String type) {
		if (!StringUtils.hasText(type)) {
			return null;
		}
		try {
			return BookType.valueOf(type.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new BusinessException(ApiCode.BAD_REQUEST, "Invalid book type: " + type);
		}
	}
}
