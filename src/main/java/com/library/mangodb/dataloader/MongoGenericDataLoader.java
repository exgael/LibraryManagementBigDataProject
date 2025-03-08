package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.GenericDataLoader;
import com.library.mangodb.MongoConfig;
import com.library.mangodb.crud.MongoGenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * MongoDB implementation of the data loader
 *
 * @param <T> Entity type
 */
public abstract class MongoGenericDataLoader<T> extends GenericDataLoader<T, MongoGenericRepository<T>> {
		private static final Logger logger = LogManager.getLogger();

		/**
		 * Constructor for MongoGenericDataLoader
		 *
		 * @param dataPath    Path to store or read data
		 * @param entityCount Number of entities to generate if needed
		 * @param repository  Repository for the entity type
		 */
		protected MongoGenericDataLoader(String dataPath, int entityCount, MongoGenericRepository<T> repository) {
				super(dataPath, entityCount, repository);
		}

		@Override
		protected void resetDatabase() {
				MongoConfig.resetDatabase();
		}

		@Override
		protected void populateDatabase(List<T> entities) {
				repository.dropCollection();
				repository.insertMany(entities);
				logger.info("Inserted {} {} into MongoDB", entities.size(), getEntityName());
		}
}