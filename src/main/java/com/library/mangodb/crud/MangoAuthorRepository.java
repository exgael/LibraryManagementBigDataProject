package com.library.mangodb.crud;

import com.library.mangodb.MangoUtils;
import com.library.mangodb.MongoConfig;
import com.library.common.model.Author;
import com.library.common.util.ModelDataGenerator;
import com.mongodb.client.model.UpdateOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Question 7 - CRUD Operations
 */
public class MangoAuthorRepository extends MongoGenericRepository<Author> {
		private static final Logger logger = LogManager.getLogger();

		public MangoAuthorRepository() {
				super("authors", "author");
		}

		public static void main(String[] args) {
				MangoAuthorRepository repository = new MangoAuthorRepository();

				// Drop existing db for clean testing
				logger.info("Resetting database");
				MongoConfig.resetDatabase();

				// Generate test authors
				logger.info("Generating test authors");
				List<Author> testAuthors = ModelDataGenerator.generateAuthors(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Author firstAuthor = testAuthors.get(0);
				ObjectId insertedId = repository.insertOne(firstAuthor);
				logger.info("Inserted author ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Author> authorsToInsert = testAuthors.subList(1, testAuthors.size());
				List<ObjectId> insertedIds = repository.insertMany(authorsToInsert);
				logger.info("Inserted {} authors with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all authors ===");
				List<Author> allAuthors = repository.find(new Document());
				logger.info("Found {} authors", allAuthors.size());

				if (!allAuthors.isEmpty()) {
						Author sample = allAuthors.get(0);
						logger.info("Sample author: Name={}, Nationality={}",
										sample.getName(), sample.getNationality());
				}

				// Test find with projection
				logger.info("=== Testing find with projection ===");
				Document projection = new Document("name", 1).append("nationality", 1);
				List<Author> authorsWithProjection = repository.find(new Document(), projection);
				logger.info("Found {} authors with projection", authorsWithProjection.size());

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

				// Test update many - update nationality for multiple authors
				logger.info("=== Testing update many authors ===");
				if (!allAuthors.isEmpty()) {
						Author sampleAuthor = allAuthors.get(0);
						String nationality = sampleAuthor.getNationality();

						Document query = new Document("nationality", nationality);
						Document update = new Document("$set", new Document("nationality", nationality + " - Modified"));
						UpdateOptions updateOptions = new UpdateOptions();
						boolean updateManyResult = repository.updateMany(query, update, updateOptions);
						logger.info("Update many result: {}", updateManyResult);

						// Verify updates
						List<Author> updatedAuthors = repository.findByNationality(nationality + " - Modified");
						logger.info("Authors with modified nationality: {}", updatedAuthors.size());
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

				// Test delete many authors
				logger.info("=== Testing delete many authors ===");

				// Get count before deletion
				List<Author> authorsBeforeDeletion = repository.find(new Document());
				logger.info("Authors before deletion: {}", authorsBeforeDeletion.size());

				// Delete authors with a specific nationality pattern
				if (!allAuthors.isEmpty()) {
						Document deleteQuery = new Document("nationality",
										new Document("$regex", "Modified").append("$options", "i"));
						boolean deleteManyResult = repository.deleteMany(deleteQuery);
						logger.info("Delete many result: {}", deleteManyResult);

						// Get count after deletion
						List<Author> authorsAfterDeletion = repository.find(new Document());
						logger.info("Authors after deletion: {}", authorsAfterDeletion.size());
						logger.info("Deleted {} authors", authorsBeforeDeletion.size() - authorsAfterDeletion.size());
				}

				logger.info("All author tests completed successfully!");
		}

		@Override
		protected Document entityToDocument(Author author) {
				Document doc = new Document();

				if (author.getId() != null) {
						doc.append("_id", MangoUtils.toObjectId(author.getId()));
				}

				doc.append("name", author.getName());

				if (author.getNationality() != null) {
						doc.append("nationality", author.getNationality());
				}

				return doc;
		}

		@Override
		protected Author documentToEntity(Document doc) {
				if (doc == null) {
						return null;
				}

				Author author = new Author();
				author.setId(MangoUtils.getIdAsString(doc, "_id"));
				author.setName(doc.getString("name"));
				author.setNationality(doc.getString("nationality"));

				return author;
		}

		@Override
		protected String getEntityId(Author author) {
				return author.getId();
		}

		/**
		 * Find author by name
		 */
		public List<Author> findByName(String name) {
				Document query = new Document("name", new Document("$regex", name).append("$options", "i"));
				return find(query);
		}

		/**
		 * Find author by nationality
		 */
		public List<Author> findByNationality(String nationality) {
				Document query = new Document("nationality", nationality);
				return find(query);
		}
}