package com.library.common.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Member {
		private String id;
		private String firstName;
		private String lastName;
		private String email;
		private String address;
		private long registrationDate;
		private ContactInfo contactInfo;
		private List<ActiveLoan> activeLoans;
		private ReadingStats readingStats;
		private Map<String, Object> preferences;

		@Getter
		@Setter
		public static class ContactInfo {
				private String phone;
				private String alternateEmail;
				private EmergencyContact emergencyContact;

				@Getter
				@Setter
				public static class EmergencyContact {
						private String name;
						private String relationship;
						private String phone;
				}
		}

		@Getter
		@Setter
		public static class ActiveLoan {
				private String bookId;
				private String bookTitle;
				private String isbn;
				private long loanDate;
				private long dueDate;
				private boolean isOverdue;

				public boolean getIsOverdue() {
						return isOverdue;
				}
		}

		@Getter
		@Setter
		public static class ReadingStats {
				private int totalBooksRead;
				private int booksReadThisYear;
				private int averageDaysToReturn;
				private Map<String, Integer> categoryPreferences;
				private List<String> favoriteAuthors;
		}
}