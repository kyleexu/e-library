package com.kylexu.elibrary.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 某本书当前借出记录：在哪个用户手里。
 */
@Data
public class BookBorrowerItem {

	private Long loanId;
	private Long bookId;
	private String userId;
	private LocalDateTime borrowedAt;
	private LocalDateTime dueAt;
	/** 已借阅天数。 */
	private long borrowedDays;
}
