package com.library.mangodb.crud;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generic MongoDB repository with CRUD operations
 *
 * @param <T> Entity type
 */
public abstract class MongoGenericRepository<T> {
		private static final Logger logger = LogManager.getLogger();
		protected final MongoCollection<Document> collection;
		protected final String entityName;

		/**
		 * Constructor for generic repository
		 *
		 * @param collectionName Name of the MongoDB collection
		 * @param entityName     Name of the entity (for logging)
		 */
		protected MongoGenericRepository(String collectionName, String entityName) {
				this.collection = MongoConfig.getDatabase().getCollection(collectionName);
				this.entityName = entityName;
		}

		/**
		 * Convert entity to Document
		 */
		protected abstract Document entityToDocument(T entity);

		/**
		 * Convert Document to entity
		 */
		protected abstract T documentToEntity(Document document);

		/**
		 * Get ID from entity
		 */
		protected abstract String getEntityId(T entity);

		/**
		 * Convert String ID to ObjectId
		 */
		protected ObjectId toObjectId(String id) {
				return new ObjectId(id);
		}

		/// ///////////////////////
		///  CREATE OPERATIONS   //
		/// ///////////////////////

		/**
		 * Insert one entity
		 *
		 * @param entity Entity to insert
		 * @return ID of the inserted entity
		 */
		public ObjectId insertOne(T entity) {
				try {
						Document doc = entityToDocument(entity);
						InsertOneResult result = collection.insertOne(doc);
						ObjectId objectId = Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue();
						logger.info("Inserted {} with ID: {}", entityName, objectId);
						return objectId;
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
		public List<ObjectId> insertMany(List<T> entities) {
				try {
						List<Document> docs = entities.stream()
										.map(this::entityToDocument)
										.collect(Collectors.toList());

						InsertManyResult results = collection.insertMany(docs);
						logger.info("Inserted {} {}s", results.getInsertedIds().size(), entityName);
						return results.getInsertedIds().values().stream()
										.map(v -> v.asObjectId().getValue())
										.toList();
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
				Document query = new Document("_id", new ObjectId(id));
				List<T> entities = find(query);
				return entities.isEmpty() ? null : entities.get(0);
		}

		/**
		 * Find entities with a query, projection, and sort
		 */
		public List<T> find(Document whereQuery, Document projectionFields, Document sortFields) {
				FindIterable<Document> results = collection.find(whereQuery);

				if (sortFields != null) {
						results = results.sort(sortFields);
				}

				if (projectionFields != null) {
						results = results.projection(projectionFields);
				}

				List<Document> documents = results.into(new ArrayList<>());
				logger.info("Found {} {}s", documents.size(), entityName);

				return documents.stream()
								.map(this::documentToEntity)
								.collect(Collectors.toList());
		}

		public List<T> find(Document whereQuery, Document projectionFields) {
				return find(whereQuery, projectionFields, null);
		}

		public List<T> find(Document whereQuery) {
				return find(whereQuery, null, null);
		}

		/// ///////////////////////
		///  UPDATE OPERATIONS   //
		/// ///////////////////////

		/**
		 * Update an entity
		 */
		public boolean update(T entity) {
				try {
						Bson filter = Filters.eq("_id", toObjectId(getEntityId(entity)));
						Document doc = entityToDocument(entity);
						doc.remove("_id"); // Remove ID from update document

						UpdateResult result = collection.replaceOne(filter, doc);

						logger.info("{} update operation - id: {}, matched: {}, modified: {}",
										entityName, getEntityId(entity), result.getMatchedCount(), result.getModifiedCount());

						return result.getModifiedCount() > 0;
				} catch (Exception e) {
						logger.error("Failed to update {} with id: {}", entityName, getEntityId(entity), e);
						return false;
				}
		}

		/**
		 * Update entities with a query and update expressions
		 */
		public boolean updateMany(Document whereQuery, Document updateExpressions, UpdateOptions updateOptions) {
				try {
						UpdateResult result = collection.updateMany(whereQuery, updateExpressions, updateOptions);

						logger.info("{} update operation completed - matched: {}, modified: {}",
										entityName, result.getMatchedCount(), result.getModifiedCount());

						return result.getModifiedCount() > 0;
				} catch (Exception e) {
						logger.error("Failed to update {}s with query: {}", entityName, whereQuery, e);
						return false;
				}
		}

		/**
		 * Update entities with a query and update pipeline
		 */
		public boolean updateManyWithPipeline(Document whereQuery, List<Document> updatePipeline, UpdateOptions updateOptions) {
				try {
						UpdateResult result = collection.updateMany(whereQuery, updatePipeline, updateOptions);
						return result.getModifiedCount() > 0;
				} catch (Exception e) {
						logger.error("Failed to update with pipeline", e);
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
				DeleteResult result = collection.deleteOne(Filters.eq("_id", toObjectId(getEntityId(entity))));
				logger.info("Deleted {} with ID {} {}",
								entityName, getEntityId(entity),
								result.getDeletedCount() > 0 ? "successfully" : "unsuccessfully");
				return result.getDeletedCount() > 0;
		}

		/**
		 * Delete entities matching a where query
		 */
		public boolean deleteMany(Document whereQuery) {
				long countBefore = collection.countDocuments();
				logger.info("There are {} {}s before deletion", countBefore, entityName);

				DeleteResult result = collection.deleteMany(whereQuery);

				long countAfter = collection.countDocuments();
				logger.info("Deleted {} {}s. There are {} {}s remaining",
								result.getDeletedCount(), entityName, countAfter, entityName);

				return result.getDeletedCount() > 0;
		}

		/**
		 * Drop the collection
		 */
		public void dropCollection() {
				collection.drop();
				logger.info("Dropped {} collection", entityName);
		}
}