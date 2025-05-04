package com.library.mangodb.crud;

import com.library.mangodb.MangoUtils;
import com.library.mangodb.MongoConfig;
import com.library.common.model.Publisher;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.manager.MangoPublisherManager;
import com.mongodb.client.model.UpdateOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

/**
 * Question 7 - CRUD Operations
 */
public class MangoPublisherRepository extends MongoGenericRepository<Publisher> {
		private static final Logger logger = LogManager.getLogger();

		public MangoPublisherRepository() {
				super("publishers", "publisher");
		}

		public static void main(String[] args) {

				MangoPublisherRepository repository = new MangoPublisherRepository();

				// Drop existing db for clean testing
				logger.info("Resetting database");
				MongoConfig.resetDatabase();

				// Generate test publishers
				logger.info("Generating test publishers");
				List<Publisher> testPublishers = ModelDataGenerator.generatePublishers(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Publisher firstPublisher = testPublishers.get(0);
				ObjectId insertedId = repository.insertOne(firstPublisher);
				logger.info("Inserted publisher ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Publisher> publishersToInsert = testPublishers.subList(1, testPublishers.size());
				List<ObjectId> insertedIds = repository.insertMany(publishersToInsert);
				logger.info("Inserted {} publishers with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all publishers ===");
				List<Publisher> allPublishers = repository.find(new Document());
				logger.info("Found {} publishers", allPublishers.size());

				if (!allPublishers.isEmpty()) {
						Publisher sample = allPublishers.get(0);
						logger.info("Sample publisher: Name={}", sample.getName());
				}

				// Test find with projection
				logger.info("=== Testing find with projection ===");
				Document projection = new Document("name", 1);
				List<Publisher> publishersWithProjection = repository.find(new Document(), projection);
				logger.info("Found {} publishers with projection", publishersWithProjection.size());

				// Test find by name
				if (!allPublishers.isEmpty()) {
						Publisher samplePublisher = allPublishers.get(0);
						String nameFragment = samplePublisher.getName().split(" ")[0]; // First word

						logger.info("=== Testing findByName ===");
						List<Publisher> publishersByName = repository.findByName(nameFragment);
						logger.info("Found {} publishers with name containing '{}'", publishersByName.size(), nameFragment);
				}

				// =====================
				// UPDATE OPERATIONS
				// =====================

				// Test update
				if (!allPublishers.isEmpty()) {
						logger.info("=== Testing update publisher ===");
						Publisher publisherToUpdate = allPublishers.get(0);
						String originalName = publisherToUpdate.getName();
						publisherToUpdate.setName(originalName + " - Updated Publishing Group");

						boolean updateResult = repository.update(publisherToUpdate);
						logger.info("Update result: {}", updateResult);

						// Verify update
						Publisher updatedPublisher = repository.findById(publisherToUpdate.getId());
						if (updatedPublisher != null) {
								logger.info("Original name: {}, Updated name: {}", originalName, updatedPublisher.getName());
						}
				}

				// Test update many - append text to publisher names
				logger.info("=== Testing update many publishers ===");
				Document query = new Document("name", new Document("$not", new Document("$regex", "Updated").append("$options", "i")));
				List<Document> updatePipeline = List.of(
								new Document("$addFields",
												new Document("name", new Document("$concat", Arrays.asList("$name", " Publishing Inc.")))
								)
				);
				UpdateOptions updateOptions = new UpdateOptions();
				boolean updateManyResult = repository.updateManyWithPipeline(query, updatePipeline, updateOptions);
				logger.info("Update many result: {}", updateManyResult);

				// Verify updates
				List<Publisher> updatedPublishers = repository.findByName("Publishing Inc");
				logger.info("Publishers with modified names: {}", updatedPublishers.size());

				// =====================
				// DELETE OPERATIONS
				// =====================

				// Test delete one publisher
				if (!allPublishers.isEmpty()) {
						logger.info("=== Testing delete publisher ===");
						Publisher publisherToDelete = allPublishers.get(0);
						boolean deleteResult = repository.delete(publisherToDelete);
						logger.info("Delete result for publisher {}: {}", publisherToDelete.getId(), deleteResult);

						// Verify publisher was deleted
						Publisher deletedPublisher = repository.findById(publisherToDelete.getId());
						logger.info("Publisher still exists? {}", deletedPublisher != null);
				}

				// Test delete many publishers
				logger.info("=== Testing delete many publishers ===");

				// Get count before deletion
				List<Publisher> publishersBeforeDeletion = repository.find(new Document());
				logger.info("Publishers before deletion: {}", publishersBeforeDeletion.size());

				// Delete publishers with a specific name pattern
				Document deleteQuery = new Document("name", new Document("$regex", "^P").append("$options", "i"));
				boolean deleteManyResult = repository.deleteMany(deleteQuery);
				logger.info("Delete many result: {}", deleteManyResult);

				// Get count after deletion
				List<Publisher> publishersAfterDeletion = repository.find(new Document());
				logger.info("Publishers after deletion: {}", publishersAfterDeletion.size());
				logger.info("Deleted {} publishers", publishersBeforeDeletion.size() - publishersAfterDeletion.size());


			logger.info("All publisher tests completed!");
		}

		@Override
		protected Document entityToDocument(Publisher publisher) {
				Document doc = new Document();

				if (publisher.getId() != null) {
						doc.append("_id", toObjectId(publisher.getId()));
				}

				doc.append("name", publisher.getName());

				return doc;
		}

		@Override
		protected Publisher documentToEntity(Document doc) {
				if (doc == null) {
						return null;
				}

				Publisher publisher = new Publisher();
				publisher.setId(MangoUtils.getIdAsString(doc, "_id"));
				publisher.setName(doc.getString("name"));

				return publisher;
		}

		@Override
		protected String getEntityId(Publisher publisher) {
				return publisher.getId();
		}

		/**
		 * Find publisher by name
		 */
		public List<Publisher> findByName(String name) {
				Document query = new Document("name", new Document("$regex", name).append("$options", "i"));
				return find(query);
		}
}