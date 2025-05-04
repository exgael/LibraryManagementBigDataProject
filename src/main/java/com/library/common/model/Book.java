package com.library.common.model;

import java.util.List;
import java.util.Map;

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

		public String getId() {
				return id;
		}

		public void setId(String id) {
				this.id = id;
		}

		public String getIsbn() {
				return isbn;
		}

		public void setIsbn(String isbn) {
				this.isbn = isbn;
		}

		public String getTitle() {
				return title;
		}

		public void setTitle(String title) {
				this.title = title;
		}

		public int getPublicationYear() {
				return publicationYear;
		}

		public void setPublicationYear(int publicationYear) {
				this.publicationYear = publicationYear;
		}

		public int getPageCount() {
				return pageCount;
		}

		public void setPageCount(int pageCount) {
				this.pageCount = pageCount;
		}

		public boolean isAvailable() {
				return available;
		}

		public void setAvailable(boolean available) {
				this.available = available;
		}

		public List<String> getAuthorsId() {
				return authorsId;
		}

		public void setAuthorsId(List<String> authorsId) {
				this.authorsId = authorsId;
		}

		public String getCategoryId() {
				return categoryId;
		}

		public void setCategoryId(String categoryId) {
				this.categoryId = categoryId;
		}

		public String getPublisherId() {
				return publisherId;
		}

		public void setPublisherId(String publisherId) {
				this.publisherId = publisherId;
		}

		public List<LoanRecord> getLoanHistory() {
				return loanHistory;
		}

		public void setLoanHistory(List<LoanRecord> loanHistory) {
				this.loanHistory = loanHistory;
		}

		public Map<String, Object> getMetadata() {
				return metadata;
		}

		public void setMetadata(Map<String, Object> metadata) {
				this.metadata = metadata;
		}

		public static class LoanRecord {
				private String memberId;
				private String memberName;
				private long loanDate;
				private long dueDate;
				private Long returnDate;

				public String getMemberId() {
						return memberId;
				}

				public void setMemberId(String memberId) {
						this.memberId = memberId;
				}

				public String getMemberName() {
						return memberName;
				}

				public void setMemberName(String memberName) {
						this.memberName = memberName;
				}

				public long getLoanDate() {
						return loanDate;
				}

				public void setLoanDate(long loanDate) {
						this.loanDate = loanDate;
				}

				public long getDueDate() {
						return dueDate;
				}

				public void setDueDate(long dueDate) {
						this.dueDate = dueDate;
				}

				public Long getReturnDate() {
						return returnDate;
				}

				public void setReturnDate(Long returnDate) {
						this.returnDate = returnDate;
				}
		}
}