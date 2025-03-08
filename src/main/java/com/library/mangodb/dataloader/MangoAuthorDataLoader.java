package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.mangodb.crud.MangoAuthorRepository;
import com.library.common.model.Author;

import java.util.List;

/**
 * Question 6 - Author data loading via MongoDB
 */
public class MangoAuthorDataLoader extends MongoGenericDataLoader<Author> {
		private static final String AUTHORS_FILENAME = "authors.json";

		public MangoAuthorDataLoader(String dataPath, int authorCount) {
				super(dataPath, authorCount, new MangoAuthorRepository());
		}

		public static void main(String[] args) {
				MangoAuthorDataLoader loader = new MangoAuthorDataLoader("generated-data", 20);
				loader.loadData();
		}

		@Override
		protected String getEntityName() {
				return "author";
		}

		@Override
		protected String getEntityFileName() {
				return AUTHORS_FILENAME;
		}

		@Override
		protected TypeReference<List<Author>> getTypeReference() {
				return new TypeReference<>() {
				};
		}
}