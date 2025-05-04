package com.library.mangodb.crud;

import com.library.common.model.Category;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.MangoUtils;
import com.library.mangodb.MongoConfig;
import com.mongodb.client.model.UpdateOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Question 7 - CRUD Operations
 */
public class MangoCategoryRepository extends MongoGenericRepository<Category> {
		private static final Logger logger = LogManager.getLogger();

		public MangoCategoryRepository() {
				super("categories", "category");
		}

		public static void main(String[] args) {
				MangoCategoryRepository repository = new MangoCategoryRepository();

				// Drop existing db for clean testing
				logger.info("Resetting database");
				MongoConfig.resetDatabase();

				// Generate test categories
				logger.info("Generating test categories");
				List<Category> testCategories = ModelDataGenerator.generateCategories();

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Category firstCategory = testCategories.get(0);
				ObjectId insertedId = repository.insertOne(firstCategory);
				logger.info("Inserted category ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Category> categoriesToInsert = testCategories.subList(1, testCategories.size());
				List<ObjectId> insertedIds = repository.insertMany(categoriesToInsert);
				logger.info("Inserted {} categories with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all categories ===");
				List<Category> allCategories = repository.find(new Document());
				logger.info("Found {} categories", allCategories.size());

				if (!allCategories.isEmpty()) {
						Category sample = allCategories.get(0);
						logger.info("Sample category: Name={}, Path={}",
										sample.getName(), sample.getPath());
				}

				// Test find with projection
				logger.info("=== Testing find with projection ===");
				Document projection = new Document("name", 1).append("path", 1);
				List<Category> categoriesWithProjection = repository.find(new Document(), projection);
				logger.info("Found {} categories with projection", categoriesWithProjection.size());

				// Test find by path
				if (!allCategories.isEmpty()) {
						Category sampleCategory = allCategories.get(0);
						String pathComponent = sampleCategory.getPath().split("/")[0]; // Parent category

						logger.info("=== Testing findByPath ===");
						List<Category> categoriesByPath = repository.findByPath(pathComponent);
						logger.info("Found {} categories with path containing '{}'", categoriesByPath.size(), pathComponent);
				}

				// Test find by name
				if (!allCategories.isEmpty()) {
						logger.info("=== Testing findByName ===");
						// Find categories with "Fiction" in name
						List<Category> categoriesByName = repository.findByName("Fiction");
						logger.info("Found {} categories with name containing 'Fiction'", categoriesByName.size());
				}

				// Test find by parent
				if (!allCategories.isEmpty()) {
						logger.info("=== Testing findByParent ===");
						// Find categories under "Fiction" parent
						List<Category> categoriesByParent = repository.findByParent("Fiction");
						logger.info("Found {} categories under 'Fiction' parent", categoriesByParent.size());
				}

				// =====================
				// UPDATE OPERATIONS
				// =====================

				// Test update
				if (!allCategories.isEmpty()) {
						logger.info("=== Testing update category ===");
						Category categoryToUpdate = allCategories.get(0);
						String originalName = categoryToUpdate.getName();
						String originalPath = categoryToUpdate.getPath();
						categoryToUpdate.setName(originalName + " - Updated");
						// Update path to reflect name change
						String parent = originalPath.split("/")[0];
						categoryToUpdate.setPath(parent + "/" + categoryToUpdate.getName());

						boolean updateResult = repository.update(categoryToUpdate);
						logger.info("Update result: {}", updateResult);

						// Verify update
						Category updatedCategory = repository.findById(categoryToUpdate.getId());
						if (updatedCategory != null) {
								logger.info("Original name: {}, Updated name: {}", originalName, updatedCategory.getName());
								logger.info("Original path: {}, Updated path: {}", originalPath, updatedCategory.getPath());
						}
				}

				// Test update many using pipeline to modify path
				logger.info("=== Testing update many categories with pipeline ===");
				if (!allCategories.isEmpty()) {
						Category sampleCategory = allCategories.get(0);
						String pathPrefix = sampleCategory.getPath().split("/")[0]; // Parent category

						// Query for categories with the path prefix
						Document query = new Document("path", new Document("$regex", "^" + pathPrefix + "/").append("$options", "i"));

						// Create a pipeline update to modify the path field
						List<Document> updatePipeline = List.of(
										new Document("$set",
														new Document("path", "PREFIX_" + pathPrefix + "/Science")
										)
						);

						UpdateOptions updateOptions = new UpdateOptions();
						boolean updateResult = repository.updateManyWithPipeline(query, updatePipeline, updateOptions);

						logger.info("Update with pipeline result: {}", updateResult);
				}

				// =====================
				// DELETE OPERATIONS
				// =====================

				// Test delete one category
				if (!allCategories.isEmpty()) {
						logger.info("=== Testing delete category ===");
						Category categoryToDelete = allCategories.get(0);
						boolean deleteResult = repository.delete(categoryToDelete);
						logger.info("Delete result for category {}: {}", categoryToDelete.getId(), deleteResult);

						// Verify category was deleted
						Category deletedCategory = repository.findById(categoryToDelete.getId());
						logger.info("Category still exists? {}", deletedCategory != null);
				}

				// Test delete many categories
				logger.info("=== Testing delete many categories ===");

				// Get count before deletion
				List<Category> categoriesBeforeDeletion = repository.find(new Document());
				logger.info("Categories before deletion: {}", categoriesBeforeDeletion.size());

				// Delete categories under a specific parent
				if (!allCategories.isEmpty()) {
						Document deleteQuery = new Document("path", new Document("$regex", "^Non-Fiction/").append("$options", "i"));
						boolean deleteManyResult = repository.deleteMany(deleteQuery);
						logger.info("Delete many result: {}", deleteManyResult);

						// Get count after deletion
						List<Category> categoriesAfterDeletion = repository.find(new Document());
						logger.info("Categories after deletion: {}", categoriesAfterDeletion.size());
						logger.info("Deleted {} categories", categoriesBeforeDeletion.size() - categoriesAfterDeletion.size());
				}

				logger.info("All category tests completed successfully!");
		}

		@Override
		protected Document entityToDocument(Category category) {
				Document doc = new Document();

				if (category.getId() != null) {
						doc.append("_id", toObjectId(category.getId()));
				}

				doc.append("name", category.getName());

				if (category.getPath() != null) {
						doc.append("path", category.getPath());
				} else if (category.getName().isEmpty()) {
						doc.append("path", "NaN");
				}

				return doc;
		}

		@Override
		protected Category documentToEntity(Document doc) {
				if (doc == null) {
						return null;
				}

				Category category = new Category();
				category.setId(MangoUtils.getIdAsString(doc, "_id"));
				category.setName(doc.getString("name"));
				category.setPath(doc.getString("path"));

				return category;
		}

		@Override
		protected String getEntityId(Category category) {
				return category.getId();
		}

		/**
		 * Find category by name
		 */
		public List<Category> findByName(String name) {
				Document query = new Document("name", new Document("$regex", name).append("$options", "i"));
				return find(query);
		}

		/**
		 * Find category by path
		 */
		public List<Category> findByPath(String path) {
				Document query = new Document("path", new Document("$regex", path).append("$options", "i"));
				return find(query);
		}

		/**
		 * Find category by parent
		 */
		public List<Category> findByParent(String parent) {
				Document query = new Document("path", new Document("$regex", "^" + parent + "/").append("$options", "i"));
				return find(query);
		}
}