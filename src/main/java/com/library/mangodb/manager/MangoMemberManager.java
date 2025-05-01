package com.library.mangodb.manager;

import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

/**
 * Classe de gestion avancée des membres avec pipelines d’agrégation
 */
public class MangoMemberManager {

    private final MongoCollection<Document> memberCollection;

    public MangoMemberManager() {
        this.memberCollection = MongoConfig.getDatabase().getCollection("members");
    }

    // 1. Nombre de membres inscrits par année
    public AggregateIterable<Document> countMembersByYear() {
        return memberCollection.aggregate(Arrays.asList(
                // Convertir registrationDate en Date si nécessaire
                Aggregates.project(new Document("year", new Document("$year", new Document("$toDate", "$registrationDate")))),
                Aggregates.group("$year", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.ascending("_id"))
        ));
    }

    // 2. Membres avec au moins un emprunt en retard
    public AggregateIterable<Document> membersWithOverdueLoans() {
        return memberCollection.aggregate(Arrays.asList(
                Aggregates.match(Filters.elemMatch("activeLoans", Filters.eq("isOverdue", true))),
                Aggregates.project(Projections.fields(
                        Projections.include("firstName", "lastName", "email", "activeLoans")
                ))
        ));
    }

    // 3. Classement des membres ayant lu le plus de livres cette année
    public AggregateIterable<Document> topReadersThisYear(int limit) {
        return memberCollection.aggregate(Arrays.asList(
                Aggregates.sort(Sorts.descending("readingStats.booksReadThisYear")),
                Aggregates.limit(limit),
                Aggregates.project(Projections.fields(
                        Projections.include("firstName", "lastName", "readingStats.booksReadThisYear")
                ))
        ));
    }

    // 4. Regroupement des membres par format de lecture préféré (préférence utilisateur)
    public AggregateIterable<Document> groupByPreferredFormat() {
        return memberCollection.aggregate(Arrays.asList(
                Aggregates.group("$preferences.preferredFormat", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
        ));
    }

    // 5. Membres ayant "Fantasy" dans leurs sujets favoris
    public AggregateIterable<Document> membersWhoLoveFantasy() {
        return memberCollection.aggregate(Arrays.asList(
                Aggregates.match(Filters.in("preferences.favoriteSubjects", "Fantasy")),
                Aggregates.project(Projections.fields(
                        Projections.include("firstName", "lastName", "preferences.favoriteSubjects")
                ))
        ));
    }

    // 6. Nombre moyen de jours pour retourner un livre par tranche de 10 livres lus
    public AggregateIterable<Document> avgReturnDaysByReadingVolume() {
        return memberCollection.aggregate(Arrays.asList(
                Aggregates.project(new Document("volumeBucket",
                        new Document("$multiply",
                                Arrays.asList(new Document("$floor", new Document("$divide", Arrays.asList("$readingStats.totalBooksRead", 10))), 10)))
                        .append("averageDaysToReturn", "$readingStats.averageDaysToReturn")),
                Aggregates.group("$volumeBucket", Accumulators.avg("avgReturnDays", "$averageDaysToReturn")),
                Aggregates.sort(Sorts.ascending("_id"))
        ));
    }
}
