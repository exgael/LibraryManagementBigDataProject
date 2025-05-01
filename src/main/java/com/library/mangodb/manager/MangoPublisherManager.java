package com.library.mangodb.manager;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;

/**
 * Classe de gestion avancée des éditeurs (publishers)
 */
public class MangoPublisherManager {

    private final MongoCollection<Document> publisherCollection;

    public MangoPublisherManager() {
        this.publisherCollection = MongoConfig.getDatabase().getCollection("publishers");
    }

    // 1. Regrouper les éditeurs par première lettre de leur nom
    public AggregateIterable<Document> groupByFirstLetter() {
        return publisherCollection.aggregate(Arrays.asList(
                project(new Document("firstLetter", new Document("$substr", Arrays.asList("$name", 0, 1)))),
                group("$firstLetter", sum("count", 1)),
                sort(ascending("_id"))
        ));
    }

    // 2. Nombre total de livres publiés par chaque éditeur (nécessite une collection "books" avec un champ "publisherId")
    public AggregateIterable<Document> totalBooksPerPublisher() {
        return publisherCollection.aggregate(Arrays.asList(
                lookup("books", "_id", "publisherId", "books"),
                project(fields(include("name"), computed("totalBooks", new Document("$size", "$books")))),
                sort(descending("totalBooks"))
        ));
    }

    // 3. Jointure : Liste des éditeurs avec les titres des livres associés
    public AggregateIterable<Document> publishersWithBooks() {
        return publisherCollection.aggregate(Arrays.asList(
                lookup("books", "_id", "publisherId", "books"),
                project(fields(include("name"), include("books.title")))
        ));
    }

    // 4. Recherche des éditeurs ayant publié des livres contenant un mot-clé dans le titre
    public AggregateIterable<Document> publishersByBookTitleKeyword(String keyword) {
        return publisherCollection.aggregate(Arrays.asList(
                lookup("books", "_id", "publisherId", "books"),
                match(expr(new Document("$gt", Arrays.asList(
                        new Document("$size", new Document("$filter", new Document("input", "$books")
                                .append("as", "b")
                                .append("cond", new Document("$regexMatch", new Document("input", "$$b.title")
                                        .append("regex", keyword)
                                        .append("options", "i"))))),
                        0 // ✅ second argument to $gt
                )))),
                project(fields(include("name")))
        ));
    }


    // 5. Editeurs classés par nombre moyen de pages de leurs livres
    public AggregateIterable<Document> publishersByAvgPages() {
        return publisherCollection.aggregate(Arrays.asList(
                lookup("books", "_id", "publisherId", "books"),
                project(fields(include("name"), computed("avgPages", new Document("$avg", "$books.pageCount")))),
                sort(descending("avgPages"))
        ));
    }

    // 6. Nombre de livres par année pour chaque éditeur
    public AggregateIterable<Document> booksPerYearPerPublisher() {
        return publisherCollection.aggregate(Arrays.asList(
                lookup("books", "_id", "publisherId", "books"),
                unwind("$books"),
                project(new Document("name", 1)
                        .append("year", new Document("$year", "$books.publishedDate"))),
                group(new Document("publisher", "$name").append("year", "$year"), sum("count", 1)),
                sort(ascending("_id.year"))
        ));
    }
}
