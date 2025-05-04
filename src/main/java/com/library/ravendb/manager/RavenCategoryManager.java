package com.library.ravendb.manager;

import com.library.common.model.Category;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;

import java.util.*;
import java.util.stream.Collectors;

public class RavenCategoryManager {

		private final DocumentStore store;

		public RavenCategoryManager() {
				this.store = RavenConfig.getDocumentStore();
		}

		public static void main(String[] args) {
				// Reset RavenDB database
				RavenConfig.resetDatabase();

				// Init manager and generate data
				RavenCategoryManager manager = new RavenCategoryManager();
				List<Category> categories = ModelDataGenerator.generateCategories();

				try (IDocumentSession session = manager.store.openSession()) {
						for (Category c : categories) {
								session.store(c);
						}
						session.saveChanges();
				}

				System.out.println("\n1. Categories by depth level:");
				manager.countCategoriesByDepthLevel();

				System.out.println("\n2. Top-level categories:");
				manager.listTopLevelCategories();

				System.out.println("\n3. Subcategories count per top-level:");
				manager.countSubcategoriesPerTopLevel();

				System.out.println("\n4. Leaf categories:");
				manager.findLeafCategories();

				System.out.println("\n5. Categories with their parents:");
				manager.listCategoriesWithParents();

				System.out.println("\n6. Search categories by name containing 'fic':");
				manager.searchCategoriesByName("fic");
		}

		// 1. Count how many categories exist at each depth level (based on "path")
		public void countCategoriesByDepthLevel() {
				try (IDocumentSession session = store.openSession()) {
						Map<Integer, Long> result = session.query(Category.class)
										.toList()
										.stream()
										.collect(Collectors.groupingBy(
														cat -> cat.getPath().split("/").length,
														TreeMap::new,
														Collectors.counting()
										));

						result.forEach((depth, count) -> System.out.printf("Depth %d: %d categories%n", depth, count));
				}
		}

		// 2. List all parent categories (top-level from path)
		public void listTopLevelCategories() {
				try (IDocumentSession session = store.openSession()) {
						Set<String> topLevels = session.query(Category.class)
										.toList()
										.stream()
										.map(cat -> cat.getPath().split("/")[0])
										.collect(Collectors.toCollection(TreeSet::new));

						topLevels.forEach(System.out::println);
				}
		}

		// 3. Count the number of subcategories under each top-level category
		public void countSubcategoriesPerTopLevel() {
				try (IDocumentSession session = store.openSession()) {
						Map<String, Long> result = session.query(Category.class)
										.toList()
										.stream()
										.collect(Collectors.groupingBy(
														cat -> cat.getPath().split("/")[0],
														Collectors.counting()
										));

						result.entrySet().stream()
										.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
										.forEach(e -> System.out.printf("%s: %d subcategories%n", e.getKey(), e.getValue()));
				}
		}

		// 4. Find all leaf categories (not parents of any other)
		public void findLeafCategories() {
				try (IDocumentSession session = store.openSession()) {
						List<Category> all = session.query(Category.class).toList();
						Set<String> allPaths = all.stream().map(Category::getPath).collect(Collectors.toSet());

						List<Category> leaves = all.stream()
										.filter(cat -> allPaths.stream()
														.noneMatch(other -> !other.equals(cat.getPath()) && other.startsWith(cat.getPath() + "/"))
										)
										.collect(Collectors.toList());

						leaves.forEach(cat -> System.out.printf("Leaf: %s (%s)%n", cat.getName(), cat.getPath()));
				}
		}

		// 5. List categories with their direct parent name
		public void listCategoriesWithParents() {
				try (IDocumentSession session = store.openSession()) {
						List<Category> categories = session.query(Category.class).toList();

						for (Category cat : categories) {
								String[] parts = cat.getPath().split("/");
								String parent = parts.length >= 2 ? parts[parts.length - 2] : null;
								System.out.printf("Category: %s, Parent: %s%n", cat.getName(), parent);
						}
				}
		}

		// 6. Search categories by partial name (case-insensitive)
		public void searchCategoriesByName(String keyword) {
				try (IDocumentSession session = store.openSession()) {
						List<Category> results = session.query(Category.class)
										.whereEquals("name", keyword) // fallback if not indexed
										.toList()
										.stream()
										.filter(cat -> cat.getName().toLowerCase().contains(keyword.toLowerCase()))
										.collect(Collectors.toList());

						results.forEach(cat -> System.out.printf("Match: %s (%s)%n", cat.getName(), cat.getPath()));
				}
		}
}
