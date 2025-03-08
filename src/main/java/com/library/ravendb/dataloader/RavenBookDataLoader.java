package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.model.Book;
import com.library.ravendb.crud.RavenBookRepository;

import java.util.List;

/**
 * Book data loading via RavenDB
 */
public class RavenBookDataLoader extends RavenGenericDataLoader<Book> {
		private static final String BOOKS_FILENAME = "books.json";

		public RavenBookDataLoader(String dataPath, int bookCount) {
				super(dataPath, bookCount, new RavenBookRepository());
		}

		public static void main(String[] args) {
				RavenBookDataLoader loader = new RavenBookDataLoader("generated-data", 100);
				loader.loadData();
		}

		@Override
		protected String getEntityName() {
				return "book";
		}

		@Override
		protected String getEntityFileName() {
				return BOOKS_FILENAME;
		}

		@Override
		protected TypeReference<List<Book>> getTypeReference() {
				return new TypeReference<>() {
				};
		}
}