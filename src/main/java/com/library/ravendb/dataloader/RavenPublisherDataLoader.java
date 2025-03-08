package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.model.Publisher;
import com.library.ravendb.crud.RavenPublisherRepository;

import java.util.List;

/**
 * Publisher data loading via RavenDB
 */
public class RavenPublisherDataLoader extends RavenGenericDataLoader<Publisher> {
		private static final String PUBLISHERS_FILENAME = "publishers.json";

		public RavenPublisherDataLoader(String dataPath, int publisherCount) {
				super(dataPath, publisherCount, new RavenPublisherRepository());
		}

		public static void main(String[] args) {
				RavenPublisherDataLoader loader = new RavenPublisherDataLoader("generated-data", 15);
				loader.loadData();
		}

		@Override
		protected String getEntityName() {
				return "publisher";
		}

		@Override
		protected String getEntityFileName() {
				return PUBLISHERS_FILENAME;
		}

		@Override
		protected TypeReference<List<Publisher>> getTypeReference() {
				return new TypeReference<>() {
				};
		}
}