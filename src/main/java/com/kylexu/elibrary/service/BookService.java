package com.kylexu.elibrary.service;

import com.kylexu.elibrary.mapper.BookMapper;
import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.BookType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 书籍领域服务：浏览书籍、查询书籍详情。
 */
@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    /**
     * 浏览书籍列表。
     *
     * @param q    可选关键字（标题 / 作者）
     * @param type 可选类型过滤
     */
    public void listBooks(String q, BookType type) {
        // TODO: 实现书籍列表查询
        List<Book> list = bookMapper.findList(q, type);
    }

    /**
     * 查询书籍详细资料。
     *
     * @param bookId 书籍 ID
     */
    public Book getBook(Long bookId) {
        // TODO: 实现书籍详情查询
        return bookMapper.findById(bookId);
    }
}
