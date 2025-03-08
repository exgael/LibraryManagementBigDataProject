package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.mangodb.crud.MangoBookRepository;
import com.library.common.model.Book;

import java.util.List;

/**
 * Question 6 - Book data loading via MongoDB
 */
public class MangoBookDataLoader extends MongoGenericDataLoader<Book> {
		private static final String BOOKS_FILENAME = "books.json";

		public MangoBookDataLoader(String dataPath, int bookCount) {
				super(dataPath, bookCount, new MangoBookRepository());
		}

		public static void main(String[] args) {
				MangoBookDataLoader loader = new MangoBookDataLoader("generated-data", 100);
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