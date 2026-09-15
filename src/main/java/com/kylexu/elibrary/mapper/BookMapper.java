package com.kylexu.elibrary.mapper;

import com.kylexu.elibrary.model.Book;
import com.kylexu.elibrary.model.BookType;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 书籍持久化：查询与库存变更。
 */
public interface BookMapper {

	/**
	 * 按 ID 查询书籍。
	 */
	Book findById(@Param("id") Long id);

	/**
	 * 浏览书籍列表。
	 *
	 * @param q    可选关键字（标题 / 作者）
	 * @param type 可选类型过滤
	 */
	List<Book> findList(@Param("q") String q, @Param("type") BookType type);

	/**
	 * 借出时扣减可借库存（仅当 available_copies &gt; 0 时成功）。
	 *
	 * @return 影响行数，0 表示库存不足或书籍不存在
	 */
	int decreaseAvailableCopies(@Param("id") Long id);

	/**
	 * 归还时增加可借库存（不超过 total_copies）。
	 *
	 * @return 影响行数
	 */
	int increaseAvailableCopies(@Param("id") Long id);
}
