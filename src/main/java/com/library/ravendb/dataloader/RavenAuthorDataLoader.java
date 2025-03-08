package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.model.Author;
import com.library.ravendb.crud.RavenAuthorRepository;

import java.util.List;

/**
 * Author data loading via RavenDB
 */
public class RavenAuthorDataLoader extends RavenGenericDataLoader<Author> {
		private static final String AUTHORS_FILENAME = "authors.json";

		public RavenAuthorDataLoader(String dataPath, int authorCount) {
				super(dataPath, authorCount, new RavenAuthorRepository());
		}

		public static void main(String[] args) {
				RavenAuthorDataLoader loader = new RavenAuthorDataLoader("generated-data", 20);
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