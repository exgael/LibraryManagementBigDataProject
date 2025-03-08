package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.mangodb.crud.MangoCategoryRepository;
import com.library.common.model.Category;

import java.util.List;

/**
 * Question 6 - Category data loading via MongoDB
 */
public class MangoCategoryDataLoader extends MongoGenericDataLoader<Category> {
		private static final String CATEGORIES_FILENAME = "categories.json";

		public MangoCategoryDataLoader(String dataPath) {
				// Category count is controlled by predefined hierarchy in ModelDataGenerator
				super(dataPath, 0, new MangoCategoryRepository());
		}

		public static void main(String[] args) {
				MangoCategoryDataLoader loader = new MangoCategoryDataLoader("generated-data");
				loader.loadData();
		}

		@Override
		protected String getEntityName() {
				return "category";
		}

		@Override
		protected String getEntityFileName() {
				return CATEGORIES_FILENAME;
		}

		@Override
		protected TypeReference<List<Category>> getTypeReference() {
				return new TypeReference<>() {
				};
		}
}