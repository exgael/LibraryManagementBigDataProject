package com.library.ravendb.crud;

import com.library.common.model.Publisher;
import com.library.ravendb.RavenConfig;
import com.library.common.util.ModelDataGenerator;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * RavenDB Repository for Publisher entities
 */
public class RavenPublisherRepository extends RavenGenericRepository<Publisher> {
		private static final Logger logger = LogManager.getLogger();

		public RavenPublisherRepository() {
				super(Publisher.class, "publisher", "publishers");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				RavenPublisherRepository repository = new RavenPublisherRepository();

				// Reset database for clean testing
				logger.info("Resetting database");
				RavenConfig.resetDatabase();

				// Generate test publishers
				logger.info("Generating test publishers");
				List<Publisher> testPublishers = ModelDataGenerator.generatePublishers(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Publisher firstPublisher = testPublishers.get(0);
				String insertedId = repository.insertOne(firstPublisher);
				logger.info("Inserted publisher ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Publisher> publishersToInsert = testPublishers.subList(1, testPublishers.size());
				List<String> insertedIds = repository.insertMany(publishersToInsert);
				logger.info("Inserted {} publishers with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all publishers ===");
				List<Publisher> allPublishers = repository.find();
				logger.info("Found {} publishers", allPublishers.size());

				if (!allPublishers.isEmpty()) {
						Publisher sample = allPublishers.get(0);
						logger.info("Sample publisher: Name={}", sample.getName());
				}

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

				// Test append suffix to names
				logger.info("=== Testing append suffix to publisher names ===");
				boolean appendResult = repository.appendSuffixToNames(" Publishing Inc.");
				logger.info("Append suffix result: {}", appendResult);

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

				// Test delete publishers with names starting with 'P'
				logger.info("=== Testing delete publishers starting with 'P' ===");
				List<Publisher> pPublishers = repository.findByName("^P");
				logger.info("Publishers starting with 'P' before deletion: {}", pPublishers.size());

				if (!pPublishers.isEmpty()) {
						for (Publisher publisher : pPublishers) {
								repository.delete(publisher);
						}

						// Verify deletion
						pPublishers = repository.findByName("^P");
						logger.info("Publishers starting with 'P' after deletion: {}", pPublishers.size());
				}

				logger.info("All publisher tests completed successfully!");

				// Shutdown the RavenDB store when done
				RavenConfig.shutdown();
		}

		@Override
		protected String getEntityId(Publisher publisher) {
				return publisher.getId();
		}

		@Override
		protected void setEntityId(Publisher publisher, String id) {
				publisher.setId(id);
		}

		/**
		 * Find publisher by name
		 */
		public List<Publisher> findByName(String name) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Publisher.class)
										.whereRegex("name", "(?i).*" + name + ".*")
										.toList();
				}
		}

		/**
		 * Append suffix to all publisher names
		 */
		public boolean appendSuffixToNames(String suffix) {
				try (IDocumentSession session = store.openSession()) {
						List<Publisher> publishers = find();

						for (Publisher publisher : publishers) {
								if (!publisher.getName().endsWith(suffix)) {
										publisher.setName(publisher.getName() + suffix);
										session.store(publisher);
								}
						}

						session.saveChanges();
						logger.info("Appended suffix '{}' to publisher names", suffix);
						return true;
				} catch (Exception e) {
						logger.error("Failed to append suffix to publisher names: ", e);
						return false;
				}
		}
}