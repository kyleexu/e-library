package com.kylexu.elibrary.controller;

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
	public void listBooks(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) String type) {
		// TODO: 实现书籍列表查询
	}

	/**
	 * 查询书籍详细资料。
	 *
	 * @param bookId 书籍 ID
	 */
	@GetMapping("/{bookId}")
	public void getBook(@PathVariable Long bookId) {
		// TODO: 实现书籍详情查询
	}
}
