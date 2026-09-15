package com.kylexu.elibrary.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 当前用户已借阅列表项：借阅信息 + 书名 + 已借天数。
 */
@Data
public class CurrentLoanItem {

	private Long loanId;
	private Long bookId;
	private String title;
	private String author;
	private LocalDateTime borrowedAt;
	private LocalDateTime dueAt;
	/** 已借阅天数。 */
	private long borrowedDays;
}
