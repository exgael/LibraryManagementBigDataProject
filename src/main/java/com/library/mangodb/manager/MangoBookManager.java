package com.library.mangodb.manager;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class MangoBookManager {

    private final MongoCollection<Document> bookCollection;
    private final MongoDatabase db;

    public MangoBookManager() {
        this.db = MongoConfig.getDatabase();
        this.bookCollection = db.getCollection("books");
    }

    /**
     * 1. Nombre de livres par année de publication
     */
    public void countBooksPerPublicationYear() {
        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$publicationYear")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 2. Moyenne de pages par catégorie (jointure sur categoryId)
     */
    public void averagePageCountPerCategory() {
        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document()
                        .append("from", "categories")
                        .append("localField", "categoryId")
                        .append("foreignField", "_id")
                        .append("as", "category")),
                new Document("$unwind", "$category"),
                new Document("$group", new Document()
                        .append("_id", "$category.name")
                        .append("avgPageCount", new Document("$avg", "$pageCount"))),
                new Document("$sort", new Document("avgPageCount", -1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 3. Liste des livres avec leur(s) auteur(s)
     */
    public void listBooksWithAuthors() {
        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document()
                        .append("from", "authors")
                        .append("localField", "authorsId")
                        .append("foreignField", "_id")
                        .append("as", "authors")),
                new Document("$project", new Document()
                        .append("title", 1)
                        .append("authors.name", 1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 4. Recherche hiérarchique : emprunts par membre et par livre
     */
    public void loansGroupedByMemberAndBook() {
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind", "$loanHistory"),
                new Document("$group", new Document()
                        .append("_id", new Document()
                                .append("memberId", "$loanHistory.memberId")
                                .append("bookTitle", "$title"))
                        .append("totalLoans", new Document("$sum", 1))),
                new Document("$sort", new Document("totalLoans", -1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 5. Détails des livres jamais retournés
     */
    public void listUnreturnedBooks() {
        List<Document> pipeline = Arrays.asList(
                new Document("$unwind", "$loanHistory"),
                new Document("$match", new Document("loanHistory.returnDate", new Document("$exists", false))),
                new Document("$project", new Document("title", 1)
                        .append("loanHistory.memberName", 1)
                        .append("loanHistory.loanDate", 1)
                        .append("loanHistory.dueDate", 1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * 6. Nombre total de prêts par livre
     */
    public void countLoansPerBook() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("title", 1)
                        .append("loanCount", new Document("$size", "$loanHistory"))),
                new Document("$sort", new Document("loanCount", -1))
        );

        AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }
}
