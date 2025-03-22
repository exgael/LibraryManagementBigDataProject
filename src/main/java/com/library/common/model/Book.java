package com.library.common.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Book {
		private String id;
		private String isbn;
		private String title;
		private int publicationYear;
		private int pageCount;
		private boolean available;
		private List<String> authorsId;
		private String categoryId;
		private String publisherId;
		private List<LoanRecord> loanHistory;
		private Map<String, Object> metadata;

		@Getter
		@Setter
		public static class LoanRecord {
				private String memberId;
				private String memberName;
				private long loanDate;
				private long dueDate;
				private Long returnDate;
		}
}