package com.library.ravendb.crud;

import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic RavenDB repository with CRUD operations
 *
 * @param <T> Entity type
 */
public abstract class RavenGenericRepository<T> {
		private static final Logger logger = LogManager.getLogger();
		protected final DocumentStore store;
		protected final Class<T> entityClass;
		protected final String entityName;
		protected final String collectionName;

		/**
		 * Constructor for generic repository
		 *
		 * @param entityClass    Class of the entity
		 * @param entityName     Name of the entity (for logging)
		 * @param collectionName Collection name in RavenDB
		 */
		protected RavenGenericRepository(Class<T> entityClass, String entityName, String collectionName) {
				this.store = RavenConfig.getDocumentStore();
				this.entityClass = entityClass;
				this.entityName = entityName;
				this.collectionName = collectionName;
		}

		/**
		 * Get entity ID
		 */
		protected abstract String getEntityId(T entity);

		/**
		 * Set entity ID
		 */
		protected abstract void setEntityId(T entity, String id);

		/// ///////////////////////
		///  CREATE OPERATIONS   //
		/// ///////////////////////

		/**
		 * Insert one entity
		 *
		 * @param entity Entity to insert
		 * @return ID of the inserted entity
		 */
		public String insertOne(T entity) {
				try (IDocumentSession session = store.openSession()) {
						session.store(entity);
						String id = session.advanced().getDocumentId(entity);
						session.saveChanges();
						logger.info("Inserted {} with ID: {}", entityName, id);
						return id;
				} catch (Exception e) {
						logger.error("Error inserting {}: ", entityName, e);
						throw new RuntimeException("Failed to insert " + entityName, e);
				}
		}

		/**
		 * Insert many entities
		 *
		 * @param entities List of entities to insert
		 * @return List of inserted entity IDs
		 */
		public List<String> insertMany(List<T> entities) {
				try (IDocumentSession session = store.openSession()) {
						List<String> ids = new ArrayList<>();

						for (T entity : entities) {
								session.store(entity);
								ids.add(session.advanced().getDocumentId(entity));
						}

						session.saveChanges();
						logger.info("Inserted {} {}s", entities.size(), entityName);
						return ids;
				} catch (Exception e) {
						logger.error("Error inserting {}s: ", entityName, e);
						throw new RuntimeException("Failed to insert " + entityName + "s", e);
				}
		}

		/// ///////////////////////
		///  READ OPERATIONS     //
		/// ///////////////////////

		/**
		 * Find entity by ID
		 */
		public T findById(String id) {
				try (IDocumentSession session = store.openSession()) {
						return session.load(entityClass, id);
				}
		}

		/**
		 * Find all entities
		 */
		public List<T> find() {
				try (IDocumentSession session = store.openSession()) {
						return session.query(entityClass)
										.toList();
				}
		}

		/**
		 * Find with WhereClauses
		 * Note: RavenDB uses different query approach than MongoDB
		 * This is a basic implementation that can be extended
		 */
		public List<T> findWithWhereClause(String fieldName, Object value) {
				try (IDocumentSession session = store.openSession()) {
						return session.query(entityClass)
										.whereEquals(fieldName, value)
										.toList();
				}
		}

		/// ///////////////////////
		///  UPDATE OPERATIONS   //
		/// ///////////////////////

		/**
		 * Update an entity
		 */
		public boolean update(T entity) {
				try (IDocumentSession session = store.openSession()) {
						session.store(entity);
						session.saveChanges();
						logger.info("{} update operation - id: {}", entityName, getEntityId(entity));
						return true;
				} catch (Exception e) {
						logger.error("Failed to update {} with id: {}", entityName, getEntityId(entity), e);
						return false;
				}
		}

		/**
		 * Update an entity field with specific value
		 */
		public boolean updateField(String id, String fieldName, Object value) {
				try (IDocumentSession session = store.openSession()) {
						T entity = session.load(entityClass, id);
						if (entity == null) {
								return false;
						}

						// In RavenDB, just modifying the loaded entity and saving changes
						// will update only the modified fields
						session.advanced().patch(entity, fieldName, value);
						session.saveChanges();

						logger.info("{} field '{}' updated for id: {}", entityName, fieldName, id);
						return true;
				} catch (Exception e) {
						logger.error("Failed to update field {} for {} with id: {}", fieldName, entityName, id, e);
						return false;
				}
		}

		/**
		 * Update multiple entities matching a condition
		 * Note: In RavenDB, this is typically done with patch operations
		 */
		public boolean updateMany(String fieldName, Object matchValue, String updateField, Object updateValue) {
				try (IDocumentSession session = store.openSession()) {
						List<T> entities = session.query(entityClass)
										.whereEquals(fieldName, matchValue)
										.toList();

						if (entities.isEmpty()) {
								return false;
						}

						for (T entity : entities) {
								session.advanced().patch(entity, updateField, updateValue);
						}

						session.saveChanges();
						logger.info("Updated {} {}s where {}={}", entities.size(), entityName, fieldName, matchValue);
						return true;
				} catch (Exception e) {
						logger.error("Failed to update {}s where {}={}", entityName, fieldName, matchValue, e);
						return false;
				}
		}

		/// ///////////////////////
		///  DELETE OPERATIONS   //
		/// ///////////////////////

		/**
		 * Delete an entity
		 */
		public boolean delete(T entity) {
				try (IDocumentSession session = store.openSession()) {
						String id = getEntityId(entity);
						session.delete(id);
						session.saveChanges();
						logger.info("Deleted {} with ID {}", entityName, id);
						return true;
				} catch (Exception e) {
						logger.error("Failed to delete {} with id: {}", entityName, getEntityId(entity), e);
						return false;
				}
		}

		/**
		 * Delete entity by ID
		 */
		public boolean deleteById(String id) {
				try (IDocumentSession session = store.openSession()) {
						session.delete(id);
						session.saveChanges();
						logger.info("Deleted {} with ID {}", entityName, id);
						return true;
				} catch (Exception e) {
						logger.error("Failed to delete {} with id: {}", entityName, id, e);
						return false;
				}
		}

		/**
		 * Delete multiple entities matching a condition
		 */
		public boolean deleteMany(String fieldName, Object value) {
				try (IDocumentSession session = store.openSession()) {
						List<T> entities = session.query(entityClass)
										.whereEquals(fieldName, value)
										.toList();

						if (entities.isEmpty()) {
								return false;
						}

						for (T entity : entities) {
								session.delete(entity);
						}

						session.saveChanges();
						logger.info("Deleted {} {}s where {}={}", entities.size(), entityName, fieldName, value);
						return true;
				} catch (Exception e) {
						logger.error("Failed to delete {}s where {}={}", entityName, fieldName, value, e);
						return false;
				}
		}

		/**
		 * Delete all entities in the collection
		 *
		 * @return true if operation was successful
		 */
		public boolean deleteAll() {
				try (IDocumentSession session = store.openSession()) {
						// First, load all entities in this collection
						List<T> entities = session.query(entityClass).toList();

						if (entities.isEmpty()) {
								logger.info("No {}s found to delete", entityName);
								return true;
						}

						// Delete each entity
						for (T entity : entities) {
								session.delete(entity);
						}

						session.saveChanges();
						logger.info("Deleted all {} {}s", entities.size(), entityName);
						return true;
				} catch (Exception e) {
						logger.error("Failed to delete all {}s: ", entityName, e);
						return false;
				}
		}
}