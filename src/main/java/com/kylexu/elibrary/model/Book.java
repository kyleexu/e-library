package com.kylexu.elibrary.model;

import lombok.Data;

@Data
public class Book {

	private Long id;
	private String isbn;
	private String title;
	private String author;
	private BookType type;
	private String description;
	private Integer totalCopies;
	private Integer availableCopies;
}
