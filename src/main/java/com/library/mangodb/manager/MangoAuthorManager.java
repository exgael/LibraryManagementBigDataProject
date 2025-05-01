package com.library.mangodb.manager;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MangoAuthorManager {

    private final MongoCollection<Document> authorsCollection;

    public MangoAuthorManager() {
        this.authorsCollection = MongoConfig.getDatabase().getCollection("authors");
    }

    // 1. Nombre total d'auteurs par nationalité
    public void countAuthorsByNationality() {
        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$nationality")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 2. Liste des noms d'auteurs avec une certaine nationalité
    public void listAuthorsByNationality(String nationality) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("nationality", nationality)),
                new Document("$project", new Document("name", 1).append("_id", 0))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toString()));

    }

    // 3. Rechercher les auteurs dont le nom commence par une lettre donnée (hiérarchique / regex)
    public void findAuthorsStartingWith(char letter) {
        String regex = "^" + letter;
        List<Document> pipeline = List.of(
                new Document("$match", new Document("name", new Document("$regex", regex).append("$options", "i"))),
                new Document("$project", new Document("name", 1).append("nationality", 1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 4. Ajouter un champ "name_length" (longueur du nom de l'auteur)
    public void computeNameLengthForAuthors() {
        List<Document> pipeline = List.of(
                new Document("$addFields", new Document("name_length", new Document("$strLenCP", "$name"))),
                new Document("$project", new Document("name", 1).append("name_length", 1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 5. Compter le nombre d'auteurs avec une nationalité inconnue (null ou vide)
    public void countAuthorsWithUnknownNationality() {
        List<Document> pipeline = List.of(
                new Document("$match", new Document("$or", Arrays.asList(
                        new Document("nationality", new Document("$exists", false)),
                        new Document("nationality", null),
                        new Document("nationality", "")
                ))),
                new Document("$count", "unknownNationalityCount")
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 6. Trier les auteurs par nom (croissant)
    public void sortAuthorsByName() {
        List<Document> pipeline = List.of(
                new Document("$sort", new Document("name", 1)),
                new Document("$project", new Document("name", 1).append("_id", 0))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    /**
     * Jointure entre les auteurs et leurs livres via $lookup
     * Récupère les auteurs avec un tableau de leurs livres associés
     */
    public List<Document> getAuthorsWithBooks() {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.lookup("books", // collection cible
                        "_id",   // champ local (auteur._id)
                        "authorId", // champ étranger (book.authorId)
                        "books")    // nom du champ ajouté dans le résultat
        );

        return MongoConfig.getDatabase()
                .getCollection("authors")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }

}
