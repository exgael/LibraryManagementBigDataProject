package com.library.mangodb.manager;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class MangoCategoryManager {

    private final MongoCollection<Document> categoryCollection;

    public MangoCategoryManager() {
        this.categoryCollection = MongoConfig.getDatabase().getCollection("categories");
    }

    /**
     * 1. Nombre de sous-catégories par catégorie principale (chemin hiérarchique)
     */
    public void countSubcategoriesPerMainCategory() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("mainCategory", new Document("$arrayElemAt", Arrays.asList(
                        new Document("$split", Arrays.asList("$path", "/")), 0)))),
                new Document("$group", new Document("_id", "$mainCategory")
                        .append("subCategoryCount", new Document("$sum", 1))),
                new Document("$sort", new Document("subCategoryCount", -1))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 2. Liste des chemins hiérarchiques uniques classés par profondeur
     */
    public void listCategoryPathsByDepth() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("path", 1)
                        .append("depth", new Document("$size", new Document("$split", Arrays.asList("$path", "/"))))),
                new Document("$sort", new Document("depth", -1))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 3. Nombre de catégories par niveau hiérarchique (root, sous-catégorie, etc.)
     */
    public void countCategoriesByLevel() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("level", new Document("$size", new Document("$split", Arrays.asList("$path", "/"))))),
                new Document("$group", new Document("_id", "$level")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 4. Catégories dont le nom contient un mot-clé spécifique (en capitalisant les occurrences)
     */
    public void highlightCategoriesWithKeyword(String keyword) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("name", new Document("$regex", keyword).append("$options", "i"))),
                new Document("$project", new Document("original", "$name")
                        .append("highlighted", new Document("$replaceAll", new Document("input", "$name")
                                .append("find", keyword)
                                .append("replacement", keyword.toUpperCase()))))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 5. Statistiques sur les longueurs de noms de catégories (longueur min, max, moyenne)
     */
    public void categoryNameLengthStats() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("nameLength", new Document("$strLenCP", "$name"))),
                new Document("$group", new Document("_id", null)
                        .append("minLength", new Document("$min", "$nameLength"))
                        .append("maxLength", new Document("$max", "$nameLength"))
                        .append("avgLength", new Document("$avg", "$nameLength")))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 6. Reconstitution de l’arbre des catégories en identifiant les parents directs
     */
    public void reconstructCategoryHierarchy() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("name", 1)
                        .append("path", 1)
                        .append("parent", new Document("$cond", Arrays.asList(
                                new Document("$gt", Arrays.asList(new Document("$size", new Document("$split", Arrays.asList("$path", "/"))), 1)),
                                new Document("$arrayElemAt", Arrays.asList(new Document("$split", Arrays.asList("$path", "/")), 0)),
                                null
                        )))),
                new Document("$sort", new Document("parent", 1))
        );

        AggregateIterable<Document> results = categoryCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // Test rapide
    public static void main(String[] args) {
        MangoCategoryManager manager = new MangoCategoryManager();
        System.out.println("=== 1. Sous-catégories par catégorie principale ===");
        manager.countSubcategoriesPerMainCategory();

        System.out.println("=== 2. Chemins hiérarchiques triés par profondeur ===");
        manager.listCategoryPathsByDepth();

        System.out.println("=== 3. Catégories par niveau hiérarchique ===");
        manager.countCategoriesByLevel();

        System.out.println("=== 4. Mise en évidence des noms avec mot-clé 'fiction' ===");
        manager.highlightCategoriesWithKeyword("fiction");

        System.out.println("=== 5. Statistiques sur les longueurs de noms ===");
        manager.categoryNameLengthStats();

        System.out.println("=== 6. Arborescence des catégories ===");
        manager.reconstructCategoryHierarchy();
    }
}
