package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.mangodb.crud.MangoPublisherRepository;
import com.library.common.model.Publisher;

import java.util.List;

/**
 * Question 6 - Publisher data loading via MongoDB
 */
public class MangoPublisherDataLoader extends MongoGenericDataLoader<Publisher> {
		private static final String PUBLISHERS_FILENAME = "publishers.json";

		public MangoPublisherDataLoader(String dataPath, int publisherCount) {
				super(dataPath, publisherCount, new MangoPublisherRepository());
		}

		public static void main(String[] args) {
				MangoPublisherDataLoader loader = new MangoPublisherDataLoader("generated-data", 15);
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