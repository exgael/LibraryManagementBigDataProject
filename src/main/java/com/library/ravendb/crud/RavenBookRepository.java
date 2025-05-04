package com.library.ravendb.crud;

import com.library.common.model.Book;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * RavenDB Repository for Book entities
 */
public class RavenBookRepository extends RavenGenericRepository<Book> {
		private static final Logger logger = LogManager.getLogger();

		public RavenBookRepository() {
				super(Book.class, "book", "books");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				RavenBookRepository repository = new RavenBookRepository();

				// Reset database for clean testing
				logger.info("Resetting database");
				RavenConfig.resetDatabase();

				// Generate test books
				logger.info("Generating test books");
				List<Book> testBooks = ModelDataGenerator.generateBooks(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Book firstBook = testBooks.get(0);
				String insertedId = repository.insertOne(firstBook);
				logger.info("Inserted book ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Book> booksToInsert = testBooks.subList(1, testBooks.size());
				List<String> insertedIds = repository.insertMany(booksToInsert);
				logger.info("Inserted {} books with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all books ===");
				List<Book> allBooks = repository.find();
				logger.info("Found {} books", allBooks.size());

				if (!allBooks.isEmpty()) {
						Book sample = allBooks.get(0);
						logger.info("Sample book: Title={}, ISBN={}, Available={}",
										sample.getTitle(), sample.getIsbn(), sample.isAvailable());
				}

				// Test find by category
				if (!allBooks.isEmpty()) {
						Book sampleBook = allBooks.get(0);
						String categoryId = sampleBook.getCategoryId();

						logger.info("=== Testing findByCategory ===");
						List<Book> booksByCategory = repository.findByCategory(categoryId);
						logger.info("Found {} books in category {}", booksByCategory.size(), categoryId);
				}

				// Test find available books
				logger.info("=== Testing findAvailableBooks ===");
				List<Book> availableBooks = repository.findAvailableBooks();
				logger.info("Found {} available books", availableBooks.size());

				// Test find by title
				if (!allBooks.isEmpty()) {
						Book sampleBook = allBooks.get(0);
						String titleFragment = sampleBook.getTitle().split(" ")[0]; // First word

						logger.info("=== Testing findByTitle ===");
						List<Book> booksByTitle = repository.findByTitle(titleFragment);
						logger.info("Found {} books with title containing '{}'", booksByTitle.size(), titleFragment);
				}

				// =====================
				// UPDATE OPERATIONS
				// =====================

				// Test update
				if (!allBooks.isEmpty()) {
						logger.info("=== Testing update book ===");
						Book bookToUpdate = allBooks.get(0);
						String originalTitle = bookToUpdate.getTitle();
						bookToUpdate.setTitle(originalTitle + " - Updated");
						bookToUpdate.setAvailable(!bookToUpdate.isAvailable());

						boolean updateResult = repository.update(bookToUpdate);
						logger.info("Update result: {}", updateResult);

						// Verify update
						Book updatedBook = repository.findById(bookToUpdate.getId());
						if (updatedBook != null) {
								logger.info("Original title: {}, Updated title: {}", originalTitle, updatedBook.getTitle());
								logger.info("Updated availability: {}", updatedBook.isAvailable());
						}
				}

				// Test updateAvailability
				if (allBooks.size() > 1) {
						logger.info("=== Testing updateAvailability ===");
						Book bookToUpdate = allBooks.get(1);
						boolean originalAvailability = bookToUpdate.isAvailable();

						boolean updateResult = repository.updateAvailability(
										bookToUpdate.getId(),
										!originalAvailability
						);
						logger.info("Update availability result: {}", updateResult);

						// Verify update
						Book updatedBook = repository.findById(bookToUpdate.getId());
						if (updatedBook != null) {
								logger.info("Original availability: {}, Updated availability: {}",
												originalAvailability, updatedBook.isAvailable());
						}
				}

				// Test updateMany
				logger.info("=== Testing update many books ===");
				boolean updateManyResult = repository.updateMany("available", false, "available", true);
				logger.info("Update many result: {}", updateManyResult);

				// Verify all books are available
				List<Book> availableBooksAfterUpdate = repository.findAvailableBooks();
				logger.info("Available books after update: {}", availableBooksAfterUpdate.size());

				// =====================
				// DELETE OPERATIONS
				// =====================

				// Test delete one book
				if (!allBooks.isEmpty()) {
						logger.info("=== Testing delete book ===");
						Book bookToDelete = allBooks.get(0);
						boolean deleteResult = repository.delete(bookToDelete);
						logger.info("Delete result for book {}: {}", bookToDelete.getId(), deleteResult);

						// Verify book was deleted
						Book deletedBook = repository.findById(bookToDelete.getId());
						logger.info("Book still exists? {}", deletedBook != null);
				}

				// Test delete many books
				logger.info("=== Testing delete many books ===");

				// Get count before deletion
				List<Book> booksWithHighPageCount = repository.findByPageCountGreaterThan(500);
				logger.info("Books with page count > 500 before deletion: {}", booksWithHighPageCount.size());

				// Delete books with page count > 500
				if (!booksWithHighPageCount.isEmpty()) {
						boolean deleteManyResult = repository.deleteMany("pageCount", 500);
						logger.info("Delete many result: {}", deleteManyResult);

						// Get count after deletion
						booksWithHighPageCount = repository.findByPageCountGreaterThan(500);
						logger.info("Books with page count > 500 after deletion: {}", booksWithHighPageCount.size());
				}

				logger.info("All tests completed successfully!");

				// Shutdown the RavenDB store when done
				RavenConfig.shutdown();
		}

		@Override
		protected String getEntityId(Book book) {
				return book.getId();
		}

		@Override
		protected void setEntityId(Book book, String id) {
				book.setId(id);
		}

		/**
		 * Find books by category
		 */
		public List<Book> findByCategory(String categoryId) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Book.class)
										.whereEquals("categoryId", categoryId)
										.toList();
				}
		}

		/**
		 * Find books by author
		 */
		public List<Book> findByAuthor(String authorId) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Book.class)
										.whereEquals("authorsId", authorId)
										.toList();
				}
		}

		/**
		 * Find available books
		 */
		public List<Book> findAvailableBooks() {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Book.class)
										.whereEquals("available", true)
										.toList();
				}
		}

		/**
		 * Update book availability
		 */
		public boolean updateAvailability(String bookId, boolean available) {
				return updateField(bookId, "available", available);
		}

		/**
		 * Find book by ISBN
		 */
		public Book findByIsbn(String isbn) {
				try (IDocumentSession session = store.openSession()) {
						List<Book> books = session.query(Book.class)
										.whereEquals("isbn", isbn)
										.toList();
						return books.isEmpty() ? null : books.get(0);
				}
		}

		/**
		 * Find books by title (partial match)
		 */
		public List<Book> findByTitle(String titleFragment) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Book.class)
										.whereRegex("title", "(?i).*" + titleFragment + ".*")
										.toList();
				}
		}

		/**
		 * Find books by publication year
		 */
		public List<Book> findByPublicationYear(int year) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Book.class)
										.whereEquals("publicationYear", year)
										.toList();
				}
		}

		/**
		 * Find books with page count greater than specified value
		 */
		public List<Book> findByPageCountGreaterThan(int pageCount) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Book.class)
										.whereGreaterThan("pageCount", pageCount)
										.toList();
				}
		}
}