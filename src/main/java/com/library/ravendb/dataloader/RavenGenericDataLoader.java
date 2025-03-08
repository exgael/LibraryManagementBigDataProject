package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.GenericDataLoader;
import com.library.ravendb.RavenConfig;
import com.library.ravendb.crud.RavenGenericRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * RavenDB implementation of the data loader
 *
 * @param <T> Entity type
 */
public abstract class RavenGenericDataLoader<T> extends GenericDataLoader<T, RavenGenericRepository<T>> {
		private static final Logger logger = LogManager.getLogger();

		/**
		 * Constructor for RavenGenericDataLoader
		 *
		 * @param dataPath    Path to store or read data
		 * @param entityCount Number of entities to generate if needed
		 * @param repository  Repository for the entity type
		 */
		protected RavenGenericDataLoader(String dataPath, int entityCount, RavenGenericRepository<T> repository) {
				super(dataPath, entityCount, repository);
		}

		@Override
		protected void resetDatabase() {
				RavenConfig.resetDatabase();
		}

		@Override
		protected void populateDatabase(List<T> entities) {
				// Delete existing data for this entity type (no direct collection drop in RavenDB)
				repository.deleteAll();

				// Insert new data
				repository.insertMany(entities);
				logger.info("Inserted {} {} into RavenDB", entities.size(), getEntityName());
		}
}