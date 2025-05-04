package com.library.ravendb.crud;

import com.library.common.model.Author;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * RavenDB Repository for Author entities
 */
public class RavenAuthorRepository extends RavenGenericRepository<Author> {
		private static final Logger logger = LogManager.getLogger();

		public RavenAuthorRepository() {
				super(Author.class, "author", "authors");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				RavenAuthorRepository repository = new RavenAuthorRepository();

				// Reset database for clean testing
				logger.info("Resetting database");
				RavenConfig.resetDatabase();

				// Generate test authors
				logger.info("Generating test authors");
				List<Author> testAuthors = ModelDataGenerator.generateAuthors(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Author firstAuthor = testAuthors.get(0);
				String insertedId = repository.insertOne(firstAuthor);
				logger.info("Inserted author ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Author> authorsToInsert = testAuthors.subList(1, testAuthors.size());
				List<String> insertedIds = repository.insertMany(authorsToInsert);
				logger.info("Inserted {} authors with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all authors ===");
				List<Author> allAuthors = repository.find();
				logger.info("Found {} authors", allAuthors.size());

				if (!allAuthors.isEmpty()) {
						Author sample = allAuthors.get(0);
						logger.info("Sample author: Name={}, Nationality={}",
										sample.getName(), sample.getNationality());
				}

				// Test find by nationality
				if (!allAuthors.isEmpty()) {
						Author sampleAuthor = allAuthors.get(0);
						String nationality = sampleAuthor.getNationality();

						logger.info("=== Testing findByNationality ===");
						List<Author> authorsByNationality = repository.findByNationality(nationality);
						logger.info("Found {} authors with nationality {}", authorsByNationality.size(), nationality);
				}

				// Test find by name (partial match)
				if (!allAuthors.isEmpty()) {
						Author sampleAuthor = allAuthors.get(0);
						String nameFragment = sampleAuthor.getName().split(" ")[0]; // First name

						logger.info("=== Testing findByName ===");
						List<Author> authorsByName = repository.findByName(nameFragment);
						logger.info("Found {} authors with name containing '{}'", authorsByName.size(), nameFragment);
				}

				// =====================
				// UPDATE OPERATIONS
				// =====================

				// Test update
				if (!allAuthors.isEmpty()) {
						logger.info("=== Testing update author ===");
						Author authorToUpdate = allAuthors.get(0);
						String originalName = authorToUpdate.getName();
						authorToUpdate.setName(originalName + " - Updated");
						authorToUpdate.setNationality("Updated Nationality");

						boolean updateResult = repository.update(authorToUpdate);
						logger.info("Update result: {}", updateResult);

						// Verify update
						Author updatedAuthor = repository.findById(authorToUpdate.getId());
						if (updatedAuthor != null) {
								logger.info("Original name: {}, Updated name: {}", originalName, updatedAuthor.getName());
								logger.info("Updated nationality: {}", updatedAuthor.getNationality());
						}
				}

				// Test updateField
				if (!allAuthors.isEmpty()) {
						logger.info("=== Testing update author field ===");
						Author authorToUpdate = allAuthors.get(1);
						String originalNationality = authorToUpdate.getNationality();

						boolean updateResult = repository.updateField(
										authorToUpdate.getId(),
										"nationality",
										originalNationality + " - Modified"
						);
						logger.info("Update field result: {}", updateResult);

						// Verify update
						Author updatedAuthor = repository.findById(authorToUpdate.getId());
						if (updatedAuthor != null) {
								logger.info("Original nationality: {}, Updated nationality: {}",
												originalNationality, updatedAuthor.getNationality());
						}
				}

				// =====================
				// DELETE OPERATIONS
				// =====================

				// Test delete one author
				if (!allAuthors.isEmpty()) {
						logger.info("=== Testing delete author ===");
						Author authorToDelete = allAuthors.get(0);
						boolean deleteResult = repository.delete(authorToDelete);
						logger.info("Delete result for author {}: {}", authorToDelete.getId(), deleteResult);

						// Verify author was deleted
						Author deletedAuthor = repository.findById(authorToDelete.getId());
						logger.info("Author still exists? {}", deletedAuthor != null);
				}

				// Test delete many by field
				if (allAuthors.size() > 2) {
						logger.info("=== Testing delete many authors ===");

						// Choose a nationality from an existing author
						String nationalityToDelete = allAuthors.get(2).getNationality();

						// Get count before deletion
						List<Author> authorsBeforeDeletion = repository.findByNationality(nationalityToDelete);
						logger.info("Authors with nationality {} before deletion: {}",
										nationalityToDelete, authorsBeforeDeletion.size());

						// Delete authors with that nationality
						boolean deleteManyResult = repository.deleteMany("nationality", nationalityToDelete);
						logger.info("Delete many result: {}", deleteManyResult);

						// Verify deletion
						List<Author> authorsAfterDeletion = repository.findByNationality(nationalityToDelete);
						logger.info("Authors with nationality {} after deletion: {}",
										nationalityToDelete, authorsAfterDeletion.size());
				}

				logger.info("All author tests completed successfully!");

				// Shutdown the RavenDB store when done
				RavenConfig.shutdown();
		}

		@Override
		protected String getEntityId(Author author) {
				return author.getId();
		}

		@Override
		protected void setEntityId(Author author, String id) {
				author.setId(id);
		}

		/**
		 * Find author by name (using startsWith for partial matches)
		 */
		public List<Author> findByName(String name) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Author.class)
										.whereRegex("name", "(?i).*" + name + ".*")
										.toList();
				}
		}

		/**
		 * Find author by nationality
		 */
		public List<Author> findByNationality(String nationality) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(Author.class)
										.whereEquals("nationality", nationality)
										.toList();
				}
		}
}