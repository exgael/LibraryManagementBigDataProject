package com.library.mangodb.manager;

import com.library.common.model.Member;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.MongoConfig;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MangoMemberManager {

    private final MongoCollection<Document> memberCollection;

    public MangoMemberManager() {
        this.memberCollection = MongoConfig.getDatabase().getCollection("members");
    }

    // 1. Count members by registration year (grouping and date transformation)
    public void countMembersByRegistrationYear() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("year", new Document("$year", new Document("$toDate", "$registrationDate")))),
                new Document("$group", new Document("_id", "$year").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("_id", 1))
        );
        memberCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    // 2. List members with overdue loans (array filtering and projection)
    public void listMembersWithOverdueLoans() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("firstName", 1)
                        .append("lastName", 1)
                        .append("email", 1)
                        .append("overdueLoans", new Document("$filter", new Document("input", "$activeLoans")
                                .append("as", "loan")
                                .append("cond", new Document("$eq", Arrays.asList("$$loan.isOverdue", true))))))
        );
        memberCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    // 3. Count number of active loans per member
    public void countLoansPerMember() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("fullName", new Document("$concat", Arrays.asList("$firstName", " ", "$lastName")))
                        .append("loanCount", new Document("$size", new Document("$ifNull", Arrays.asList("$activeLoans", new ArrayList<>()))))),
                new Document("$sort", new Document("loanCount", -1))
        );
        AggregateIterable<Document> results = memberCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }


    // 4. Most preferred categories (unwinding nested map into key-value pairs and counting)
    public void mostPreferredCategories() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("preferences", "$readingStats.categoryPreferences")),
                new Document("$project", new Document("categories", new Document("$objectToArray", "$preferences"))),
                new Document("$unwind", "$categories"),
                new Document("$group", new Document("_id", "$categories.k").append("totalPreferenceScore", new Document("$sum", "$categories.v"))),
                new Document("$sort", new Document("totalPreferenceScore", -1))
        );
        memberCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    // 5. Top 5 most common favorite authors (unwinding and grouping)
    public void topFavoriteAuthors() {
        List<Document> pipeline = Arrays.asList(
                new Document("$project", new Document("authors", "$readingStats.favoriteAuthors")),
                new Document("$unwind", "$authors"),
                new Document("$group", new Document("_id", "$authors").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)),
                new Document("$limit", 5)
        );
        memberCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    // 6. Members with emergency contact info (nested field projection and existence check)
    public void listMembersWithEmergencyContact() {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("contactInfo.emergencyContact", new Document("$exists", true))),
                new Document("$project", new Document("firstName", 1)
                        .append("lastName", 1)
                        .append("emergencyName", "$contactInfo.emergencyContact.name")
                        .append("emergencyPhone", "$contactInfo.emergencyContact.phone"))
        );
        memberCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }

    public static void main(String[] args) {
        // Drop and initialize the collection
        MongoCollection<Document> collection = MongoConfig.getDatabase().getCollection("members");
        collection.drop();

        // Générer et insérer les membres dans la collection
        List<Member> members = ModelDataGenerator.generateMembers(10);
        List<Document> docs = members.stream().map(member -> Document.parse(MongoConfig.toJson(member))).toList();
        collection.insertMany(docs);

        // Initialiser le manager et exécuter les tests
        MangoMemberManager manager = new MangoMemberManager();

        System.out.println("\n--- Members per registration year ---");
        manager.countMembersByRegistrationYear();

        System.out.println("\n--- Members with overdue loans ---");
        manager.listMembersWithOverdueLoans();

        System.out.println("\n--- Loans per member ---");
        manager.countLoansPerMember();

        System.out.println("\n--- Most preferred categories ---");
        manager.mostPreferredCategories();

        System.out.println("\n--- Top favorite authors ---");
        manager.topFavoriteAuthors();

        System.out.println("\n--- Members with emergency contact ---");
        manager.listMembersWithEmergencyContact();
    }
}
