package com.kylexu.elibrary.mapper;

import com.kylexu.elibrary.model.Loan;
import com.kylexu.elibrary.model.LoanStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 借阅单持久化：创建、查询与归还更新。
 */
public interface LoanMapper {

	/**
	 * 新增借阅单。
	 */
	int insert(Loan loan);

	/**
	 * 按 ID 查询借阅单。
	 */
	Loan findById(@Param("id") Long id);

	/**
	 * 查询用户对某书的未归还借阅（用于防重复借阅）。
	 */
	Loan findActiveByUserAndBook(@Param("userId") String userId, @Param("bookId") Long bookId);

	/**
	 * 查询用户当前已借阅列表（status = BORROWED）。
	 */
	List<Loan> findCurrentByUserId(@Param("userId") String userId);

	/**
	 * 查询某本书当前未归还的借阅（谁手里有这本书）。
	 */
	List<Loan> findCurrentByBookId(@Param("bookId") Long bookId);

	/**
	 * 将借阅单标记为已归还。
	 *
	 * @return 影响行数，0 表示记录不存在或已归还
	 */
	int markReturned(
			@Param("id") Long id,
			@Param("status") LoanStatus status,
			@Param("returnedAt") LocalDateTime returnedAt);
}
