package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.model.Category;
import com.library.ravendb.crud.RavenCategoryRepository;

import java.util.List;

/**
 * Category data loading via RavenDB
 */
public class RavenCategoryDataLoader extends RavenGenericDataLoader<Category> {
		private static final String CATEGORIES_FILENAME = "categories.json";

		public RavenCategoryDataLoader(String dataPath) {
				// Category count is controlled by predefined hierarchy in ModelDataGenerator
				super(dataPath, 0, new RavenCategoryRepository());
		}

		public static void main(String[] args) {
				RavenCategoryDataLoader loader = new RavenCategoryDataLoader("generated-data");
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