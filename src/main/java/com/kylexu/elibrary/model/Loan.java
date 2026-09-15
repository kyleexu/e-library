package com.kylexu.elibrary.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Loan {

	private Long id;
	private String userId;
	private Long bookId;
	private LoanStatus status;
	private LocalDateTime borrowedAt;
	private LocalDateTime dueAt;
	private LocalDateTime returnedAt;
}
