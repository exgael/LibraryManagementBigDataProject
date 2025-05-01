package com.library.mangodb.crud;

import com.library.mangodb.MangoUtils;
import com.library.mangodb.MongoConfig;
import com.library.common.model.Book;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.manager.MangoBookManager;
import com.mongodb.client.model.UpdateOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Question 7 - CRUD Operations
 */
public class MangoBookRepository extends MongoGenericRepository<Book> {
		private static final Logger logger = LogManager.getLogger();

		public MangoBookRepository() {
				super("books", "book");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				MangoBookRepository repository = new MangoBookRepository();

				// Drop existing db for clean testing
				logger.info("Resetting database");
				MongoConfig.resetDatabase();

				// Generate test books
				logger.info("Generating test books");
				List<Book> testBooks = ModelDataGenerator.generateBooks(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Book firstBook = testBooks.get(0);
				ObjectId insertedId = repository.insertOne(firstBook);
				logger.info("Inserted book ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Book> booksToInsert = testBooks.subList(1, testBooks.size());
				List<ObjectId> insertedIds = repository.insertMany(booksToInsert);
				logger.info("Inserted {} books with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all books ===");
				List<Book> allBooks = repository.find(new Document());
				logger.info("Found {} books", allBooks.size());

				if (!allBooks.isEmpty()) {
						Book sample = allBooks.get(0);
						logger.info("Sample book: Title={}, ISBN={}, Available={}",
										sample.getTitle(), sample.getIsbn(), sample.isAvailable());
				}

				// Test find with projection
				logger.info("=== Testing find with projection ===");
				Document projection = new Document("title", 1).append("isbn", 1).append("available", 1);
				List<Book> booksWithProjection = repository.find(new Document(), projection);
				logger.info("Found {} books with projection", booksWithProjection.size());

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

				// Test update many - mark all books as available
				logger.info("=== Testing update many books ===");
				Document query = new Document("available", false);
				Document update = new Document("$set", new Document("available", true));
				UpdateOptions updateOptions = new UpdateOptions();
				boolean updateManyResult = repository.updateMany(query, update, updateOptions);
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
				List<Book> booksBeforeDeletion = repository.find(new Document());
				logger.info("Books before deletion: {}", booksBeforeDeletion.size());

				// Delete books with page count > 500
				Document deleteQuery = new Document("pageCount", new Document("$gt", 500));
				boolean deleteManyResult = repository.deleteMany(deleteQuery);
				logger.info("Delete many result: {}", deleteManyResult);

				// Get count after deletion
				List<Book> booksAfterDeletion = repository.find(new Document());
				logger.info("Books after deletion: {}", booksAfterDeletion.size());
				logger.info("Deleted {} books", booksBeforeDeletion.size() - booksAfterDeletion.size());


				// ===========================
				// TESTS DES MÉTHODES COMPLEXES
				// ===========================

				// Création du manager
				MangoBookManager manager = new MangoBookManager();

				// ===========================
				// TEST DES MÉTHODES D’AGRÉGATION
				// ===========================

				logger.info("\n[TEST 1] Nombre de livres par année de publication");
				manager.countBooksPerPublicationYear();

				logger.info("\n[TEST 2] Moyenne de pages par catégorie (avec jointure)");
				manager.averagePageCountPerCategory();

				logger.info("\n[TEST 3] Liste des livres avec leurs auteurs");
				manager.listBooksWithAuthors();

				logger.info("\n[TEST 4] Emprunts groupés par membre et livre (recherche hiérarchique)");
				manager.loansGroupedByMemberAndBook();

				logger.info("\n[TEST 5] Liste des livres jamais retournés");
				manager.listUnreturnedBooks();

				logger.info("\n[TEST 6] Nombre total de prêts par livre");
				manager.countLoansPerBook();

			logger.info("All tests completed successfully!");
		}

		@Override
		protected Document entityToDocument(Book book) {
				Document doc = new Document();

				if (book.getId() != null) {
						doc.append("_id", toObjectId(book.getId()));
				}

				// Add basic fields
				doc.append("isbn", book.getIsbn())
								.append("title", book.getTitle())
								.append("publicationYear", book.getPublicationYear())
								.append("pageCount", book.getPageCount())
								.append("available", book.isAvailable());

				// Add author IDs
				if (book.getAuthorsId() != null && !book.getAuthorsId().isEmpty()) {
						List<Object> authorIdObjects = book.getAuthorsId().stream()
										.map(MangoUtils::toObjectId)
										.collect(Collectors.toList());
						doc.append("authorsId", authorIdObjects);
				}

				// Add category ID
				if (book.getCategoryId() != null) {
						doc.append("categoryId", toObjectId(book.getCategoryId()));
				}

				// Add publisher ID
				if (book.getPublisherId() != null) {
						doc.append("publisherId", toObjectId(book.getPublisherId()));
				}

				// Add loan history
				if (book.getLoanHistory() != null && !book.getLoanHistory().isEmpty()) {
						List<Document> loanDocs = new ArrayList<>();
						for (Book.LoanRecord loan : book.getLoanHistory()) {
								Document loanDoc = new Document()
												.append("memberId", toObjectId(loan.getMemberId()))
												.append("memberName", loan.getMemberName())
												.append("loanDate", loan.getLoanDate())
												.append("dueDate", loan.getDueDate());

								if (loan.getReturnDate() != null) {
										loanDoc.append("returnDate", loan.getReturnDate());
								}

								loanDocs.add(loanDoc);
						}
						doc.append("loanHistory", loanDocs);
				}

				// Add metadata
				if (book.getMetadata() != null && !book.getMetadata().isEmpty()) {
						doc.append("metadata", new Document(book.getMetadata()));
				}

				return doc;
		}

		@Override
		protected Book documentToEntity(Document doc) {
				if (doc == null) {
						return null;
				}

				Book book = new Book();

				// Set basic fields
				book.setId(MangoUtils.getIdAsString(doc, "_id"));
				book.setIsbn(doc.getString("isbn"));
				book.setTitle(doc.getString("title"));

				// Handle primitive types with null checks
				Integer publicationYear = doc.getInteger("publicationYear");
				if (publicationYear != null) {
						book.setPublicationYear(publicationYear);
				}

				Integer pageCount = doc.getInteger("pageCount");
				if (pageCount != null) {
						book.setPageCount(pageCount);
				}

				Boolean available = doc.getBoolean("available");
				if (available != null) {
						book.setAvailable(available);
				}

				// Set author IDs
				@SuppressWarnings("unchecked")
				List<Object> authorIdObjs = (List<Object>) doc.get("authorsId");
				if (authorIdObjs != null && !authorIdObjs.isEmpty()) {
						List<String> authorIds = authorIdObjs.stream()
										.map(obj -> obj instanceof ObjectId ? ((ObjectId) obj).toHexString() : obj.toString())
										.collect(Collectors.toList());
						book.setAuthorsId(authorIds);
				}

				// Set category ID
				if (doc.containsKey("categoryId")) {
						book.setCategoryId(MangoUtils.getIdAsString(doc, "categoryId"));
				}

				// Set publisher ID
				if (doc.containsKey("publisherId")) {
						book.setPublisherId(MangoUtils.getIdAsString(doc, "publisherId"));
				}

				// Convert loan history
				@SuppressWarnings("unchecked")
				List<Document> loanDocs = (List<Document>) doc.get("loanHistory");
				if (loanDocs != null && !loanDocs.isEmpty()) {
						List<Book.LoanRecord> loanHistory = new ArrayList<>();
						for (Document loanDoc : loanDocs) {
								Book.LoanRecord loan = new Book.LoanRecord();
								loan.setMemberId(MangoUtils.getIdAsString(loanDoc, "memberId"));
								loan.setMemberName(loanDoc.getString("memberName"));
								loan.setLoanDate(loanDoc.getLong("loanDate"));
								loan.setDueDate(loanDoc.getLong("dueDate"));

								if (loanDoc.containsKey("returnDate")) {
										loan.setReturnDate(loanDoc.getLong("returnDate"));
								}

								loanHistory.add(loan);
						}
						book.setLoanHistory(loanHistory);
				}

				// Convert metadata
				Document metadataDoc = (Document) doc.get("metadata");
				if (metadataDoc != null) {
						Map<String, Object> metadata = new HashMap<>();
						for (String key : metadataDoc.keySet()) {
								metadata.put(key, metadataDoc.get(key));
						}
						book.setMetadata(metadata);
				}

				return book;
		}

		@Override
		protected String getEntityId(Book entity) {
				return entity.getId();
		}

		/**
		 * Find books by category
		 */
		public List<Book> findByCategory(String categoryId) {
				Document query = new Document("category", categoryId);
				return find(query);
		}

		/**
		 * Find books by author
		 */
		public List<Book> findByAuthor(String authorId) {
				Document query = new Document("authors", authorId);
				return find(query);
		}

		/**
		 * Find available books
		 */
		public List<Book> findAvailableBooks() {
				Document query = new Document("available", true);
				return find(query);
		}

		/**
		 * Update book availability
		 */
		public boolean updateAvailability(String bookId, boolean available) {
				Book book = findById(bookId);
				if (book == null) {
						return false;
				}

				book.setAvailable(available);
				return update(book);
		}

		/**
		 * Find book by ID
		 */
		public Book findById(String id) {
				Document query = new Document("_id", new ObjectId(id));
				List<Book> books = find(query);
				return books.isEmpty() ? null : books.get(0);
		}
}