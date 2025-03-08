package com.library.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.common.util.ModelDataGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Abstract base class for database data loaders
 *
 * @param <T> Entity type
 * @param <R> Repository type
 */
public abstract class GenericDataLoader<T, R> {
		private static final Logger logger = LogManager.getLogger();

		protected final String dataPath;
		protected final int entityCount;
		protected final ObjectMapper objectMapper;
		protected final R repository;

		/**
		 * Constructor for GenericDataLoader
		 *
		 * @param dataPath    Path to store or read data
		 * @param entityCount Number of entities to generate if needed
		 * @param repository  Repository for the entity type
		 */
		protected GenericDataLoader(String dataPath, int entityCount, R repository) {
				this.dataPath = dataPath;
				this.entityCount = entityCount;
				this.objectMapper = new ObjectMapper();
				this.repository = repository;
		}

		/**
		 * Load data into database.
		 * If data doesn't exist in the specified file path, it will be generated.
		 */
		public void loadData() {
				logger.info("Starting {} data loading process", getEntityName());

				try {
						// Get entities from file or generate new ones
						List<T> entities = getOrGenerateEntities();

						// Store in database
						populateDatabase(entities);
				} catch (IOException e) {
						logger.error("Error during {} data loading process", getEntityName(), e);
						throw new RuntimeException("Failed to load " + getEntityName() + " data", e);
				}
		}

		/**
		 * Get existing entity data or generate new data
		 *
		 * @return List of entity objects
		 * @throws IOException If file operations fail
		 */
		private List<T> getOrGenerateEntities() throws IOException {
				Path entityFilePath = getEntityFilePath();

				if (!Files.exists(entityFilePath)) {
						logger.info("No existing {} data found, (re)generating entire database data", getEntityName());
						ModelDataGenerator.generateData();
						resetDatabase();
				}

				return loadEntitiesFromFile(entityFilePath);
		}

		/**
		 * Load entities from JSON file
		 *
		 * @param filePath Path to JSON file
		 * @return List of entity objects
		 * @throws IOException If file reading fails
		 */
		private List<T> loadEntitiesFromFile(Path filePath) throws IOException {
				logger.info("Reading existing {} data from {}", getEntityName(), filePath);
				List<T> entities = objectMapper.readValue(
								filePath.toFile(),
								getTypeReference()
				);
				logger.info("Loaded {} {} records from file", entities.size(), getEntityName());
				return entities;
		}

		/**
		 * Get the file path for entity JSON
		 *
		 * @return Path object for entity JSON file
		 */
		protected Path getEntityFilePath() {
				return Paths.get(dataPath, getEntityFileName());
		}

		/**
		 * Reset the database (implementation depends on database type)
		 */
		protected abstract void resetDatabase();

		/**
		 * Populate database with entity data
		 *
		 * @param entities List of entities to insert
		 */
		protected abstract void populateDatabase(List<T> entities);

		/**
		 * Get entity name (for logging)
		 *
		 * @return Entity name
		 */
		protected abstract String getEntityName();

		/**
		 * Get entity file name
		 *
		 * @return File name for entity JSON
		 */
		protected abstract String getEntityFileName();

		/**
		 * Get type reference for JSON deserialization
		 *
		 * @return TypeReference for the entity type
		 */
		protected abstract TypeReference<List<T>> getTypeReference();
}