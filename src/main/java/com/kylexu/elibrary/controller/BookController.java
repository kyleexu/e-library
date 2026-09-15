package com.kylexu.elibrary.controller;

import com.kylexu.elibrary.common.ApiResponse;
import com.kylexu.elibrary.model.Book;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 书籍相关接口：浏览书籍、查询书籍详情。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

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
		return ApiResponse.success();
	}

	/**
	 * 查询书籍详细资料。
	 *
	 * @param bookId 书籍 ID
	 */
	@GetMapping("/{bookId}")
	public ApiResponse<Book> getBook(@PathVariable Long bookId) {
		return ApiResponse.success();
	}
}
