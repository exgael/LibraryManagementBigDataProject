package com.library.common.model;

import java.util.List;
import java.util.Map;

public class Member {

		/// //////////////////////
		/// Fields
		/// //////////////////////

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

		/// //////////////////////
		/// Getters and Setters
		/// //////////////////////

		public String getId() {
				return id;
		}

		public void setId(String id) {
				this.id = id;
		}

		public String getFirstName() {
				return firstName;
		}

		public void setFirstName(String firstName) {
				this.firstName = firstName;
		}

		public String getLastName() {
				return lastName;
		}

		public void setLastName(String lastName) {
				this.lastName = lastName;
		}

		public String getEmail() {
				return email;
		}

		public void setEmail(String email) {
				this.email = email;
		}

		public String getAddress() {
				return address;
		}

		public void setAddress(String address) {
				this.address = address;
		}

		public long getRegistrationDate() {
				return registrationDate;
		}

		public void setRegistrationDate(long registrationDate) {
				this.registrationDate = registrationDate;
		}

		public ContactInfo getContactInfo() {
				return contactInfo;
		}

		public void setContactInfo(ContactInfo contactInfo) {
				this.contactInfo = contactInfo;
		}

		public List<ActiveLoan> getActiveLoans() {
				return activeLoans;
		}

		public void setActiveLoans(List<ActiveLoan> activeLoans) {
				this.activeLoans = activeLoans;
		}

		public ReadingStats getReadingStats() {
				return readingStats;
		}

		public void setReadingStats(ReadingStats readingStats) {
				this.readingStats = readingStats;
		}

		public Map<String, Object> getPreferences() {
				return preferences;
		}

		public void setPreferences(Map<String, Object> preferences) {
				this.preferences = preferences;
		}

		/// //////////////////////
		/// Inner classes
		/// //////////////////////

		// Inner class for contact information
		public static class ContactInfo {
				private String phone;
				private String alternateEmail;
				private EmergencyContact emergencyContact;

				// Getters and Setters
				public String getPhone() {
						return phone;
				}

				public void setPhone(String phone) {
						this.phone = phone;
				}

				public String getAlternateEmail() {
						return alternateEmail;
				}

				public void setAlternateEmail(String alternateEmail) {
						this.alternateEmail = alternateEmail;
				}

				public EmergencyContact getEmergencyContact() {
						return emergencyContact;
				}

				public void setEmergencyContact(EmergencyContact emergencyContact) {
						this.emergencyContact = emergencyContact;
				}

				// Nested emergency contact
				public static class EmergencyContact {
						private String name;
						private String relationship;
						private String phone;

						// Getters and Setters
						public String getName() {
								return name;
						}

						public void setName(String name) {
								this.name = name;
						}

						public String getRelationship() {
								return relationship;
						}

						public void setRelationship(String relationship) {
								this.relationship = relationship;
						}

						public String getPhone() {
								return phone;
						}

						public void setPhone(String phone) {
								this.phone = phone;
						}
				}
		}

		// Inner class for active loans
		public static class ActiveLoan {
				private String bookId;
				private String bookTitle;
				private String isbn;
				private long loanDate;
				private long dueDate;
				private boolean isOverdue;

				// Getters and Setters
				public String getBookId() {
						return bookId;
				}

				public void setBookId(String bookId) {
						this.bookId = bookId;
				}

				public String getBookTitle() {
						return bookTitle;
				}

				public void setBookTitle(String bookTitle) {
						this.bookTitle = bookTitle;
				}

				public String getIsbn() {
						return isbn;
				}

				public void setIsbn(String isbn) {
						this.isbn = isbn;
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

				public boolean isOverdue() {
						return isOverdue;
				}

				public void setOverdue(boolean overdue) {
						isOverdue = overdue;
				}
		}

		// Inner class for reading statistics
		public static class ReadingStats {
				private int totalBooksRead;
				private int booksReadThisYear;
				private int averageDaysToReturn;
				private Map<String, Integer> categoryPreferences; // Map of category name to count
				private List<String> favoriteAuthors;

				// Getters and Setters
				public int getTotalBooksRead() {
						return totalBooksRead;
				}

				public void setTotalBooksRead(int totalBooksRead) {
						this.totalBooksRead = totalBooksRead;
				}

				public int getBooksReadThisYear() {
						return booksReadThisYear;
				}

				public void setBooksReadThisYear(int booksReadThisYear) {
						this.booksReadThisYear = booksReadThisYear;
				}

				public int getAverageDaysToReturn() {
						return averageDaysToReturn;
				}

				public void setAverageDaysToReturn(int averageDaysToReturn) {
						this.averageDaysToReturn = averageDaysToReturn;
				}

				public Map<String, Integer> getCategoryPreferences() {
						return categoryPreferences;
				}

				public void setCategoryPreferences(Map<String, Integer> categoryPreferences) {
						this.categoryPreferences = categoryPreferences;
				}

				public List<String> getFavoriteAuthors() {
						return favoriteAuthors;
				}

				public void setFavoriteAuthors(List<String> favoriteAuthors) {
						this.favoriteAuthors = favoriteAuthors;
				}
		}
}