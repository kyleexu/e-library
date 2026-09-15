package com.kylexu.elibrary.dto;

import lombok.Data;

/**
 * 借阅书籍请求体。
 */
@Data
public class BorrowBookRequest {

	/** 书籍 ID。 */
	private Long bookId;

	/** 借阅天数，如 14、30。 */
	private Integer loanDays;
}
