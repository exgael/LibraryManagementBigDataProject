package com.library.mangodb.manager;

import com.library.common.model.Author;
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

public class MangoBookManager {

		private final MongoCollection<Document> bookCollection;

		public MangoBookManager() {
				this.bookCollection = MongoConfig.getDatabase().getCollection("books");
		}

		public static void main(String[] args) {
				// Reset the database for fresh test data
				MongoConfig.resetDatabase();

				// Managers
				MangoBookManager bookManager = new MangoBookManager();
				MongoCollection<Document> authorsCol = MongoConfig.getDatabase().getCollection("authors");
				MongoCollection<Document> booksCol = MongoConfig.getDatabase().getCollection("books");
				MongoCollection<Document> categoriesCol = MongoConfig.getDatabase().getCollection("categories");

				// --- Insert sample authors ---
				List<Author> authors = ModelDataGenerator.generateAuthors(5);
				List<ObjectId> authorObjectIds = new ArrayList<>();
				for (Author a : authors) {
						Document doc = new Document("name", a.getName()).append("nationality", a.getNationality());
						authorsCol.insertOne(doc);
						authorObjectIds.add(doc.getObjectId("_id"));
				}

				// --- Insert sample categories ---
				List<Category> categories = ModelDataGenerator.generateCategories();
				List<ObjectId> categoryObjectIds = new ArrayList<>();
				for (Category c : categories) {
						Document doc = new Document("name", c.getName());
						categoriesCol.insertOne(doc);
						categoryObjectIds.add(doc.getObjectId("_id"));
				}

				// --- Insert sample books with random authors/categories ---
				List<Book> books = ModelDataGenerator.generateBooks(10);
				for (Book b : books) {
						List<ObjectId> randomAuthors = authorObjectIds.subList(0, Math.min(2, authorObjectIds.size()));
						ObjectId randomCategory = categoryObjectIds.get(new Random().nextInt(categoryObjectIds.size()));
						Document doc = new Document("title", b.getTitle())
										.append("publicationYear", b.getPublicationYear())
										.append("pageCount", b.getPageCount())
										.append("available", true)
										.append("authorsId", randomAuthors)
										.append("categoryId", randomCategory)
										.append("publisherId", new ObjectId()); // fake publisher ID
						booksCol.insertOne(doc);
				}

				// --- Run and display aggregation results ---
				System.out.println("1. Number of books by publication year:");
				bookManager.countBooksPerPublicationYear();

				System.out.println("\n2. Average number of pages per author:");
				bookManager.averagePageCountPerAuthor();

				System.out.println("\n3. List of books with their authors:");
				bookManager.listBooksWithAuthors();

				System.out.println("\n4. Number of books per author:");
				bookManager.countBooksByAuthor();

				System.out.println("\n5. Books starting with 'L':");
				bookManager.findBooksStartingWith('L');
		}

		// 1. Count the number of books published per year
		// Uses $group to aggregate books by "publicationYear" and count with $sum
		public void countBooksPerPublicationYear() {
				List<Document> pipeline = Arrays.asList(
								new Document("$group", new Document("_id", "$publicationYear")
												.append("count", new Document("$sum", 1))),
								new Document("$sort", new Document("_id", 1)) // Sort years ascending
				);
				AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
				results.forEach(doc -> System.out.println(doc.toJson()));
		}

		// 2. Compute the average page count of books per author
		// Unwinds authorsId array to handle many-to-many relations
		// Performs a $lookup to join books with authors collection
		// Groups by author name and calculates average page count
		public void averagePageCountPerAuthor() {
				List<Document> pipeline = Arrays.asList(
								new Document("$unwind", "$authorsId"), // Deconstruct authorsId array
								new Document("$lookup", new Document("from", "authors") // Join with authors collection
												.append("localField", "authorsId")
												.append("foreignField", "_id")
												.append("as", "author")),
								new Document("$unwind", "$author"), // Get author object from array
								new Document("$group", new Document("_id", "$author.name") // Group by author name
												.append("averagePages", new Document("$avg", "$pageCount"))), // Compute average
								new Document("$sort", new Document("averagePages", -1)) // Sort descending
				);
				AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
				results.forEach(doc -> System.out.println(doc.toJson()));
		}

		// 3. List all books with their associated authors
		// Uses $lookup to join books with authors on "authorsId"
		// Projects title, year, and author names
		public void listBooksWithAuthors() {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "authors")
												.append("localField", "authorsId")
												.append("foreignField", "_id")
												.append("as", "authors")), // Resulting "authors" field is an array of matched authors
								new Document("$project", new Document("title", 1)
												.append("publicationYear", 1)
												.append("authors.name", 1)) // Only keep author names
				);
				AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
				results.forEach(doc -> System.out.println(doc.toJson()));
		}

		// 4. Count the number of books written by each author
		// Similar to above, but with grouping and counting instead of listing
		public void countBooksByAuthor() {
				List<Document> pipeline = Arrays.asList(
								new Document("$unwind", "$authorsId"), // One author per document
								new Document("$lookup", new Document("from", "authors")
												.append("localField", "authorsId")
												.append("foreignField", "_id")
												.append("as", "author")),
								new Document("$unwind", "$author"),
								new Document("$group", new Document("_id", "$author.name")
												.append("bookCount", new Document("$sum", 1))), // Count books per author
								new Document("$sort", new Document("bookCount", -1)) // Most prolific authors first
				);
				AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
				results.forEach(doc -> System.out.println(doc.toJson()));
		}

		// 5. Find all books whose title starts with a given letter (case-insensitive)
		// Uses $match with regex for pattern filtering
		public void findBooksStartingWith(char letter) {
				String regex = "^" + letter;
				List<Document> pipeline = Arrays.asList(
								new Document("$match", new Document("title", new Document("$regex", regex).append("$options", "i"))),
								new Document("$project", new Document("title", 1).append("publicationYear", 1))
				);
				AggregateIterable<Document> results = bookCollection.aggregate(pipeline);
				results.forEach(doc -> System.out.println(doc.toJson()));
		}
}
