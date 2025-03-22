package com.library.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javafaker.Faker;
import com.library.common.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Question 5
 */
public class ModelDataGenerator {
		private static final Logger logger = LogManager.getLogger();
		private static final Faker faker = new Faker(new Locale("en-US"));
		private static final Long MILLISECONDS_IN_DAY = 86400000L;

		// Maps to store generated entities
		// Allows for building relationships between entities
		private static final List<Author> authors = new ArrayList<>();
		private static final List<Category> categories = new ArrayList<>();
		private static final List<Publisher> publishers = new ArrayList<>();
		private static final List<Book> books = new ArrayList<>();
		private static final List<Member> members = new ArrayList<>();

		/**
		 * Generate author documents
		 */
		public static List<Author> generateAuthors(int count) {
				logger.info("Generating {} author documents", count);

				Random random = new Random();
				for (int i = 0; i < count; i++) {
						Author author = new Author();
						author.setId(new ObjectId().toString());
						author.setName(faker.name().fullName());
						author.setNationality(faker.nation().nationality());
						authors.add(author);
				}

				return authors;
		}

		/**
		 * Generate category documents
		 */
		public static List<Category> generateCategories() {
				logger.info("Generating category documents");

				// Generate category hierarchy
				Map<String, List<String>> categoryMap = generateCategoryHierarchy();

				for (String parentCategory : categoryMap.keySet()) {
						for (String subCategoryName : categoryMap.get(parentCategory)) {
								Category category = new Category();
								category.setId(new ObjectId().toString());
								category.setName(subCategoryName);
								category.setPath(parentCategory + "/" + subCategoryName);
								categories.add(category);
						}
				}

				return categories;
		}

		/**
		 * Generate publisher documents
		 */
		public static List<Publisher> generatePublishers(int count) {
				logger.info("Generating {} publisher documents", count);

				Set<String> uniquePublishers = new HashSet<>();

				// Generate unique publishers
				while (publishers.size() < count) {
						String publisherName = faker.book().publisher();
						if (uniquePublishers.add(publisherName)) {
								Publisher publisher = new Publisher();
								publisher.setId(new ObjectId().toString());
								publisher.setName(publisherName);
								publishers.add(publisher);
						}
				}

				return publishers;
		}

		/**
		 * Generate book documents with proper relationships
		 */
		public static List<Book> generateBooks(int count) {
				logger.info("Generating {} book documents", count);

				// Ensure we have authors, categories, and publishers
				if (authors.isEmpty()) generateAuthors(100);
				if (categories.isEmpty()) generateCategories();
				if (publishers.isEmpty()) generatePublishers(30);

				Random random = new Random();

				for (int i = 0; i < count; i++) {
						Book book = new Book();
						book.setId(new ObjectId().toString());
						book.setIsbn(faker.code().isbn13());
						book.setTitle(faker.book().title());
						book.setPublicationYear(faker.number().numberBetween(1900, 2024));
						book.setPageCount(faker.number().numberBetween(100, 1000));
						book.setAvailable(random.nextBoolean());

						// Assign authors (1-3)
						int authorCount = random.nextInt(3) + 1;
						List<String> bookAuthors = new ArrayList<>();
						for (int j = 0; j < authorCount; j++) {
								Author author = getRandomElement(authors, random);
								bookAuthors.add(author.getId());
						}
						book.setAuthorsId(bookAuthors);

						// Assign category
						Category category = getRandomElement(categories, random);
						book.setCategoryId(category.getId());

						// Assign publisher
						Publisher publisher = getRandomElement(publishers, random);
						book.setPublisherId(publisher.getId());

						// Generate loan history (0-10 loans)
						int loanCount = random.nextInt(11);
						if (loanCount > 0) {
								List<Book.LoanRecord> loanHistory = new ArrayList<>();
								long now = System.currentTimeMillis();

								for (int j = 0; j < loanCount; j++) {
										long loanDate = now - faker.number().numberBetween(1, 365 * 3) * MILLISECONDS_IN_DAY;
										long dueDate = loanDate + 14 * MILLISECONDS_IN_DAY; // 14 days later

										Book.LoanRecord loan = new Book.LoanRecord();
										loan.setMemberId(new ObjectId().toString());
										loan.setMemberName(faker.name().fullName());
										loan.setLoanDate(loanDate);
										loan.setDueDate(dueDate);

										// 90% of past loans are returned
										if (j < loanCount - 1 || random.nextDouble() < 0.9) {
												// Return date between loan date and now, sometimes overdue
												long returnDate = loanDate + faker.number().numberBetween(1, 30) * MILLISECONDS_IN_DAY;
												loan.setReturnDate(returnDate);
										}

										loanHistory.add(loan);
								}

								// Sort loan history by date
								loanHistory.sort(Comparator.comparingLong(Book.LoanRecord::getLoanDate));
								book.setLoanHistory(loanHistory);

								// Set available based on most recent loan
								Book.LoanRecord mostRecentLoan = loanHistory.get(loanHistory.size() - 1);
								book.setAvailable(mostRecentLoan.getReturnDate() != null);
						}

						// Generate metadata
						Map<String, Object> metadata = new HashMap<>();
						metadata.put("format", getRandomElement(Arrays.asList("Hardcover", "Paperback", "E-book", "Audiobook"), random));
						metadata.put("language", getRandomElement(Arrays.asList("English", "French", "Spanish", "German", "Italian"), random));
						metadata.put("edition", random.nextInt(5) + 1);

						if (random.nextBoolean()) {
								metadata.put("series", faker.book().title() + " Series");
								metadata.put("volume", random.nextInt(10) + 1);
						}

						if (random.nextBoolean()) {
								metadata.put("awards", Arrays.asList(
												faker.book().title() + " Award",
												faker.book().title() + " Prize"
								));
						}

						book.setMetadata(metadata);
						books.add(book);
				}

				return books;
		}

		/**
		 * Generate member documents
		 */
		public static List<Member> generateMembers(int count) {
				logger.info("Generating {} member documents", count);

				Random random = new Random();

				// Ensure we have books
				if (books.isEmpty()) generateBooks(500);

				for (int i = 0; i < count; i++) {
						Member member = new Member();
						member.setId(new ObjectId().toString());
						member.setFirstName(faker.name().firstName());
						member.setLastName(faker.name().lastName());
						member.setEmail(faker.internet().emailAddress());
						member.setAddress(faker.address().fullAddress());

						// Registration between 10 years ago and today
						long registrationDate = System.currentTimeMillis() -
										faker.number().numberBetween(1, 3650) * MILLISECONDS_IN_DAY;
						member.setRegistrationDate(registrationDate);

						// Generate contact info
						Member.ContactInfo contactInfo = new Member.ContactInfo();
						contactInfo.setPhone(faker.phoneNumber().phoneNumber());
						contactInfo.setAlternateEmail(faker.internet().emailAddress());

						if (random.nextBoolean()) {
								Member.ContactInfo.EmergencyContact emergency = new Member.ContactInfo.EmergencyContact();
								emergency.setName(faker.name().fullName());
								emergency.setRelationship(getRandomElement(
												Arrays.asList("Spouse", "Parent", "Sibling", "Friend", "Child"), random));
								emergency.setPhone(faker.phoneNumber().phoneNumber());
								contactInfo.setEmergencyContact(emergency);
						}

						member.setContactInfo(contactInfo);

						// Generate active loans (0-5) using real book IDs
						int activeLoanCount = random.nextInt(6);
						if (activeLoanCount > 0 && !books.isEmpty()) {
								List<Member.ActiveLoan> activeLoans = new ArrayList<>();
								long now = System.currentTimeMillis();

								for (int j = 0; j < activeLoanCount; j++) {
										Book randomBook = getRandomElement(books, random);

										Member.ActiveLoan loan = new Member.ActiveLoan();
										loan.setBookId(randomBook.getId());
										loan.setBookTitle(randomBook.getTitle());
										loan.setIsbn(randomBook.getIsbn());

										// Loan between 1 and 30 days ago
										long loanDate = now - faker.number().numberBetween(1, 30) * MILLISECONDS_IN_DAY;
										loan.setLoanDate(loanDate);

										// Due date 14 days after loan date
										long dueDate = loanDate + 14 * MILLISECONDS_IN_DAY;
										loan.setDueDate(dueDate);

										// Check if overdue
										loan.setOverdue(dueDate < now);

										activeLoans.add(loan);
								}

								member.setActiveLoans(activeLoans);
						}

						// Generate reading stats
						Member.ReadingStats stats = new Member.ReadingStats();

						// Total books between 1 and 200
						int totalBooks = faker.number().numberBetween(1, 200);
						stats.setTotalBooksRead(totalBooks);

						// Books this year between 0 and 50, but not more than total
						int booksThisYear = Math.min(faker.number().numberBetween(0, 50), totalBooks);
						stats.setBooksReadThisYear(booksThisYear);

						// Average days to return between 3 and 21
						stats.setAverageDaysToReturn(faker.number().numberBetween(3, 21));

						// Generate category preferences using real category paths
						Map<String, Integer> categoryPreferences = new HashMap<>();

						// Pick 3-8 random categories
						int categoryCount = faker.number().numberBetween(3, Math.min(8, categories.size()));
						Set<Integer> categoryIndices = new HashSet<>();
						while (categoryIndices.size() < categoryCount) {
								categoryIndices.add(random.nextInt(categories.size()));
						}

						for (Integer index : categoryIndices) {
								Category category = categories.get(index);
								// Between 1 and 50 books in this category
								categoryPreferences.put(category.getPath(), faker.number().numberBetween(1, 50));
						}
						stats.setCategoryPreferences(categoryPreferences);

						// Generate favorite authors using real author names
						List<String> favoriteAuthors = new ArrayList<>();
						int authorCount = faker.number().numberBetween(1, 5);
						Set<Integer> authorIndices = new HashSet<>();
						while (authorIndices.size() < authorCount) {
								authorIndices.add(random.nextInt(authors.size()));
						}

						for (Integer index : authorIndices) {
								favoriteAuthors.add(authors.get(index).getName());
						}
						stats.setFavoriteAuthors(favoriteAuthors);

						member.setReadingStats(stats);

						// Generate preferences
						Map<String, Object> preferences = new HashMap<>();
						preferences.put("preferredFormat", getRandomElement(
										Arrays.asList("Hardcover", "Paperback", "E-book", "Audiobook"), random));

						Map<String, Boolean> notificationPrefs = new HashMap<>();
						notificationPrefs.put("email", random.nextBoolean());
						notificationPrefs.put("sms", random.nextBoolean());
						notificationPrefs.put("overdueReminders", random.nextBoolean());
						notificationPrefs.put("newArrivals", random.nextBoolean());
						preferences.put("notificationPreferences", notificationPrefs);

						preferences.put("favoriteSubjects", IntStream.range(0, faker.number().numberBetween(1, 5))
										.mapToObj(j -> faker.lorem().word())
										.collect(Collectors.toList()));

						member.setPreferences(preferences);

						members.add(member);
				}

				return members;
		}

		/**
		 * Generate category hierarchy
		 */
		private static Map<String, List<String>> generateCategoryHierarchy() {
				Map<String, List<String>> categories = new HashMap<>();

				// Fiction categories
				categories.put("Fiction", Arrays.asList(
								"Science Fiction", "Fantasy", "Mystery", "Thriller",
								"Romance", "Horror", "Historical Fiction", "Literary Fiction"
				));

				// Non-fiction categories
				categories.put("Non-Fiction", Arrays.asList(
								"Biography", "Autobiography", "History", "Science",
								"Self-Help", "Philosophy", "Business", "Politics"
				));

				// Academic categories
				categories.put("Academic", Arrays.asList(
								"Mathematics", "Computer Science", "Physics", "Chemistry",
								"Biology", "Psychology", "Sociology", "Economics"
				));

				// Children's categories
				categories.put("Children", Arrays.asList(
								"Picture Books", "Middle Grade", "Young Adult",
								"Educational", "Fairy Tales", "Adventure"
				));

				return categories;
		}

		/**
		 * Get a random element from a list
		 */
		private static <T> T getRandomElement(List<T> list, Random random) {
				return list.get(random.nextInt(list.size()));
		}

		/**
		 * Export all generated data to JSON files
		 */
		public static void exportAll(String outputPath) {
				try {
						File outputDir = new File(outputPath);
						if (!outputDir.exists()) {
								boolean _wasCreated = outputDir.mkdirs();
						}

						ObjectMapper mapper = new ObjectMapper();
						mapper.enable(SerializationFeature.INDENT_OUTPUT); // For pretty printing

						// Export authors
						if (!authors.isEmpty()) {
								File authorsFile = new File(outputPath + "/authors.json");
								mapper.writeValue(authorsFile, authors);
								logger.info("Successfully exported {} authors to {}", authors.size(), authorsFile);
						}

						// Export categories
						if (!categories.isEmpty()) {
								File categoriesFile = new File(outputPath + "/categories.json");
								mapper.writeValue(categoriesFile, categories);
								logger.info("Successfully exported {} categories to {}", categories.size(), categoriesFile);
						}

						// Export publishers
						if (!publishers.isEmpty()) {
								File publishersFile = new File(outputPath + "/publishers.json");
								mapper.writeValue(publishersFile, publishers);
								logger.info("Successfully exported {} publishers to {}", publishers.size(), publishersFile);
						}

						// Export books
						if (!books.isEmpty()) {
								File booksFile = new File(outputPath + "/books.json");
								mapper.writeValue(booksFile, books);
								logger.info("Successfully exported {} books to {}", books.size(), booksFile);
						}

						// Export members
						if (!members.isEmpty()) {
								File membersFile = new File(outputPath + "/members.json");
								mapper.writeValue(membersFile, members);
								logger.info("Successfully exported {} members to {}", members.size(), membersFile);
						}

				} catch (IOException e) {
						logger.error("Error exporting data to JSON: ", e);
				}
		}

		/**
		 * Reset the states
		 */
		public static void resetData() {
				authors.clear();
				categories.clear();
				publishers.clear();
				books.clear();
				members.clear();
		}

		public static void generateData() {
				// Clean up
				resetData();

				// Generate
				generateAuthors(100);
				generateCategories();
				generatePublishers(30);
				generateBooks(5000);
				generateMembers(1000);

				// Export to file
				exportAll("generated-data");

				// Clean up
				resetData();
		}

		public static void main(String[] args) {
				generateData();
		}
}