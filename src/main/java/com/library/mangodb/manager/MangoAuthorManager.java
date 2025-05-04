package com.library.mangodb.manager;

import com.library.common.model.Author;
import com.library.mangodb.MongoConfig;
import com.library.common.util.ModelDataGenerator;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;

public class MangoAuthorManager {

    private final MongoCollection<Document> authorsCollection;

    public MangoAuthorManager() {
        this.authorsCollection = MongoConfig.getDatabase().getCollection("authors");
    }

    // 1. Count total number of authors per nationality
    // Uses $group to group authors by nationality and count them using $sum
    // Then $sort sorts the result in descending order by count
    public void countAuthorsByNationality() {
        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$nationality")
                        .append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 2. List author names by a given nationality
    // Uses $match to filter authors by nationality
    // Then $project to keep only the name field (excluding _id)
    public void listAuthorsByNationality(String nationality) {
        List<Document> pipeline = Arrays.asList(
                new Document("$match", new Document("nationality", nationality)),
                new Document("$project", new Document("name", 1).append("_id", 0))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toString()));
    }

    // 3. Find authors whose names start with a given letter (case-insensitive)
    // Uses $match with a regular expression ($regex) to filter names
    // Then $project to return the name and nationality fields
    public void findAuthorsStartingWith(char letter) {
        String regex = "^" + letter;
        List<Document> pipeline = List.of(
                new Document("$match", new Document("name", new Document("$regex", regex).append("$options", "i"))),
                new Document("$project", new Document("name", 1).append("nationality", 1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 4. Add a new field "name_length" that stores the length of the author's name
    // Uses $addFields with $strLenCP to compute the length of the string
    // Then $project to include only name and name_length
    public void computeNameLengthForAuthors() {
        List<Document> pipeline = List.of(
                new Document("$addFields", new Document("name_length", new Document("$strLenCP", "$name"))),
                new Document("$project", new Document("name", 1).append("name_length", 1))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }

    // 5. Sort authors by name in ascending order
    // Uses $sort to order by name (1 = ascending)
    // Then $project to include only the name field (excluding _id)
    public void sortAuthorsByName() {
        List<Document> pipeline = List.of(
                new Document("$sort", new Document("name", 1)),
                new Document("$project", new Document("name", 1).append("_id", 0))
        );

        AggregateIterable<Document> results = authorsCollection.aggregate(pipeline);
        results.forEach(doc -> System.out.println(doc.toJson()));
    }


     // 6. Join authors with their books using $lookup
     // Performs a left outer join between authors and books
     // "authors._id" is matched with "books.authorId"
     // Result includes an additional field "books" which is an array of matching book documents
    public List<Document> getAuthorsWithBooks() {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.lookup("books",       // target collection
                        "_id",                  // local field in "authors"
                        "authorId",             // foreign field in "books"
                        "books")                // name of the new array field containing matched documents
        );

        return MongoConfig.getDatabase()
                .getCollection("authors")
                .aggregate(pipeline)
                .into(new ArrayList<>());
    }

    public static void main(String[] args) {
        // Reset the database to ensure clean test data
        MongoConfig.resetDatabase();

        MangoAuthorManager manager = new MangoAuthorManager();

        // Generate a list of test authors
        List<Author> testAuthors = ModelDataGenerator.generateAuthors(10);

        // Convert authors to MongoDB documents and collect nationalities
        List<Document> authorDocuments = new ArrayList<>();
        Set<String> nationalities = new HashSet<>();
        for (Author author : testAuthors) {
            Document authorDoc = new Document("name", author.getName())
                    .append("nationality", author.getNationality());
            authorDocuments.add(authorDoc);
            nationalities.add(author.getNationality());
        }

        // Insert authors into MongoDB
        manager.authorsCollection.insertMany(authorDocuments);

        // Insert test books into "books" collection to test $lookup join
        MongoConfig.getDatabase().getCollection("books").insertMany(Arrays.asList(
                new Document("title", "Harry Potter").append("authorId", 1),
                new Document("title", "1984").append("authorId", 2),
                new Document("title", "Kafka on the Shore").append("authorId", 3)
        ));

        // Pick a nationality for testing listAuthorsByNationality
        String testNationality = nationalities.stream().findFirst().orElse("Unknown");

        // Run aggregation methods
        System.out.println("1. Count Authors by Nationality:");
        manager.countAuthorsByNationality();

        System.out.println("\n2. List Authors by Nationality (" + testNationality + "):");
        manager.listAuthorsByNationality(testNationality);

        System.out.println("\n3. Authors Starting with 'H':");
        manager.findAuthorsStartingWith('H');

        System.out.println("\n4. Compute Name Length for Authors:");
        manager.computeNameLengthForAuthors();

        System.out.println("\n5. Sort Authors by Name:");
        manager.sortAuthorsByName();

        System.out.println("\n6. Authors with Books:");
        List<Document> authorsWithBooks = manager.getAuthorsWithBooks();
        authorsWithBooks.forEach(author -> System.out.println(author.toJson()));
    }
}
