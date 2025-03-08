package com.library.ravendb.crud;

import com.library.common.model.Category;
import com.library.ravendb.RavenConfig;
import com.library.common.util.ModelDataGenerator;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * RavenDB Repository for Category entities
 */
public class RavenCategoryRepository extends RavenGenericRepository<Category> {
		private static final Logger logger = LogManager.getLogger();

		public RavenCategoryRepository() {
				super(Category.class, "category", "categories");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				RavenCategoryRepository repository = new RavenCategoryRepository();

				// Reset database for clean testing
				logger.info("Resetting database");
				RavenConfig.resetDatabase();

				// Generate test categories
				logger.info("Generating test categories");
				List<Category> testCategories = ModelDataGenerator.generateCategories();

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Category firstCategory = testCategories.get(0);
				String insertedId = repository.insertOne(firstCategory);
				logger.info("Inserted category ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Category> categoriesToInsert = testCategories.subList(1, testCategories.size());
				List<String> insertedIds = repository.insertMany(categoriesToInsert);
				logger.info("Inserted {} categories with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all categories ===");
				List<Category> allCategories = repository.find();
				logger.info("Found {} categories", allCategories.size());

				if (!allCategories.isEmpty()) {
						Category sample = allCategories.get(0);
						logger.info("Sample category: Name={}, Path={}",
										sample.getName(), sample.getPath());
				}

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

				// Test update path prefix
				if (!allCategories.isEmpty()) {
						logger.info("=== Testing update path prefix ===");
						Category sampleCategory = allCategories.get(0);
						String pathPrefix = sampleCategory.getPath().split("/")[0]; // Parent category

						boolean updateResult = repository.updatePathPrefix(pathPrefix, "PREFIX_" + pathPrefix);
						logger.info("Update path prefix result: {}", updateResult);

						// Verify updated paths
						List<Category> categoriesWithUpdatedPrefix = repository.findByPath("PREFIX_" + pathPrefix);
						logger.info("Categories with updated prefix: {}", categoriesWithUpdatedPrefix.size());
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

				// Get categories with "Non-Fiction" path
				List<Category> nonFictionCategories = repository.findByPath("Non-Fiction");
				logger.info("Non-Fiction categories before deletion: {}", nonFictionCategories.size());

				if (!nonFictionCategories.isEmpty()) {
						boolean deleteManyResult = repository.deleteMany("path", "Non-Fiction");
						logger.info("Delete many result: {}", deleteManyResult);

						// Verify deletion
						nonFictionCategories = repository.findByPath("Non-Fiction");
						logger.info("Non-Fiction categories after deletion: {}", nonFictionCategories.size());
				}

				logger.info("All category tests completed successfully!");

				// Shutdown the RavenDB store when done
				RavenConfig.shutdown();
		}

		@Override
		protected String getEntityId(Category category) {
				return category.getId();
		}

		@Override
		protected void setEntityId(Category category, String id) {
				category.setId(id);
		}

		/**
		 * Find category by name
		 */
		public List<Category> findByName(String name) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Category.class)
										.whereRegex("name", "(?i).*" + name + ".*")
										.toList();
				}
		}

		/**
		 * Find category by path
		 */
		public List<Category> findByPath(String path) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Category.class)
										.whereRegex("path", "(?i).*" + path + ".*")
										.toList();
				}
		}

		/**
		 * Find category by parent
		 */
		public List<Category> findByParent(String parent) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Category.class)
										.whereRegex("path", "^" + parent + "/.*")
										.toList();
				}
		}

		/**
		 * Update category path with prefix
		 */
		public boolean updatePathPrefix(String oldPrefix, String newPrefix) {
				try (IDocumentSession session = store.openSession()) {
						List<Category> categories = findByPath(oldPrefix);
						if (categories.isEmpty()) {
								return false;
						}

						for (Category category : categories) {
								String oldPath = category.getPath();
								String newPath = oldPath.replace(oldPrefix, newPrefix);
								category.setPath(newPath);
								session.store(category);
						}

						session.saveChanges();
						logger.info("Updated path prefix from '{}' to '{}' for {} categories",
										oldPrefix, newPrefix, categories.size());
						return true;
				} catch (Exception e) {
						logger.error("Failed to update path prefix: ", e);
						return false;
				}
		}
}