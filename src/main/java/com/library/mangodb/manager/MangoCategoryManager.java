package com.library.mangodb.manager;

import com.library.common.model.Book;
import com.library.common.model.Category;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MangoCategoryManager {

    private final MongoCollection<Document> categoryCollection;

    public MangoCategoryManager() {
        this.categoryCollection = MongoConfig.getDatabase().getCollection("categories");
    }

    // 1. Count how many categories exist at each depth level (based on "path")
    // Split path by "/" and compute depth
    public void countCategoriesByDepthLevel() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("depth", new Document("$size", new Document("$split", Arrays.asList("$path", "/"))))),
                new Document("$group", new Document("_id", "$depth").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 2. List all parent categories (i.e., top-level category from path)
    // Extract first element of path
    public void listTopLevelCategories() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("topLevel", new Document("$arrayElemAt", Arrays.asList(new Document("$split", Arrays.asList("$path", "/")), 0)))),
                new Document("$group", new Document("_id", "$topLevel")),
                new Document("$sort", new Document("_id", 1))
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 3. Count the number of subcategories under each top-level category
    // Extract first element and group by it
    public void countSubcategoriesPerTopLevel() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("topLevel", new Document("$arrayElemAt", Arrays.asList(new Document("$split", Arrays.asList("$path", "/")), 0)))),
                new Document("$group", new Document("_id", "$topLevel").append("subcategoryCount", new Document("$sum", 1))),
                new Document("$sort", new Document("subcategoryCount", -1))
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 4. Find all leaf categories (categories that are not parents of any other category)
    // Compare full paths to see which ones are never prefixes
    public void findLeafCategories() {
        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document("from", "categories")
                        .append("let", new Document("path", "$path"))
                        .append("pipeline", Arrays.asList(
                                new Document("$match", new Document("$expr", new Document("$and", Arrays.asList(
                                        new Document("$ne", Arrays.asList("$$path", "$path")),
                                        new Document("$regexMatch", new Document("input", "$path").append("regex", new Document("$concat", Arrays.asList("^", "$$path", "/"))))
                                ))))
                        ))
                        .append("as", "children")),
                new Document("$match", new Document("children", new Document("$size", 0))),
                new Document("$project", new Document("name", 1).append("path", 1))
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 5. List categories with their direct parent name
    // Extract second to last part of the path
    public void listCategoriesWithParents() {
        List<Document> pipeline = Arrays.asList(
                new Document("$addFields", new Document("pathParts", new Document("$split", Arrays.asList("$path", "/")))),
                new Document("$addFields", new Document("depth", new Document("$size", "$pathParts"))),
                new Document("$project", new Document("name", 1)
                        .append("path", 1)
                        .append("parent", new Document("$cond", Arrays.asList(
                                new Document("$gt", Arrays.asList("$depth", 1)),
                                new Document("$arrayElemAt", Arrays.asList("$pathParts", new Document("$subtract", Arrays.asList("$depth", 2)))),
                                null
                        )))
                )
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 6. Search categories by partial name (case-insensitive)
    // Use regex in $match
    public void searchCategoriesByName(String keyword) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("name", new Document("$regex", keyword).append("$options", "i"))),
                new Document("$project", new Document("name", 1).append("path", 1))
        );
        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }


    public static void main(String[] args) {
        // Reset database to ensure a clean state
        MongoConfig.resetDatabase();

        // Initialize collection and manager
        MongoCollection<Document> categoryCol = MongoConfig.getDatabase().getCollection("categories");
        MangoCategoryManager categoryManager = new MangoCategoryManager();

        // Generate and insert test data
        List<Category> categories = ModelDataGenerator.generateCategories();
        for (Category c : categories) {
            Document doc = new Document("name", c.getName())
                    .append("path", c.getPath());
            categoryCol.insertOne(doc);
        }

        // Call methods to test each pipeline
        System.out.println("\n1. Categories by depth level:");
        categoryManager.countCategoriesByDepthLevel();

        System.out.println("\n2. Top-level categories:");
        categoryManager.listTopLevelCategories();

        System.out.println("\n3. Subcategories count per top-level:");
        categoryManager.countSubcategoriesPerTopLevel();

        System.out.println("\n4. Leaf categories:");
        categoryManager.findLeafCategories();

        System.out.println("\n5. Categories with their parents:");
        categoryManager.listCategoriesWithParents();

        System.out.println("\n6. Search categories by name containing 'fic':");
        categoryManager.searchCategoriesByName("fic"); // e.g., should match 'Fiction'
    }
}
