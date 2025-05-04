package com.library.mangodb.manager;

import com.library.common.model.Author;
import com.library.common.model.Book;
import com.library.common.model.Publisher;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.MongoConfig;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class MangoPublisherManager {

		private final MongoCollection<Document> publisherCollection;

		public MangoPublisherManager() {
				this.publisherCollection = MongoConfig.getDatabase().getCollection("publishers");
		}

		public static void main(String[] args) {
				// Drop and initialize the collections
				MongoCollection<Document> publisherCollection = MongoConfig.getDatabase().getCollection("publishers");
				MongoCollection<Document> bookCollection = MongoConfig.getDatabase().getCollection("books");
				MongoCollection<Document> authorCollection = MongoConfig.getDatabase().getCollection("authors");

				publisherCollection.drop();
				bookCollection.drop();
				authorCollection.drop();

				// Générer et insérer des éditeurs, des auteurs et des livres
				List<Publisher> publishers = ModelDataGenerator.generatePublishers(10);
				List<Author> authors = ModelDataGenerator.generateAuthors(10);  // Générer des auteurs
				List<Book> books = ModelDataGenerator.generateBooks(30);  // Générer des livres

				List<Document> publisherDocs = publishers.stream().map(publisher -> Document.parse(MongoConfig.toJson(publisher))).toList();
				List<Document> authorDocs = authors.stream().map(author -> Document.parse(MongoConfig.toJson(author))).toList();
				List<Document> bookDocs = books.stream().map(book -> Document.parse(MongoConfig.toJson(book))).toList();

				publisherCollection.insertMany(publisherDocs);
				authorCollection.insertMany(authorDocs);
				bookCollection.insertMany(bookDocs);

				// Initialiser le manager et exécuter les tests
				MangoPublisherManager manager = new MangoPublisherManager();

				System.out.println("\n--- Nombre de livres publiés par éditeur ---");
				manager.countBooksPerPublisher();

				System.out.println("\n--- Liste unique des auteurs publiés par éditeur ---");
				manager.listAuthorsPerPublisher();

				System.out.println("\n--- Moyenne du nombre de pages des livres par éditeur ---");
				manager.averagePagesPerPublisher();

				System.out.println("\n--- Éditeurs ayant publié plus de N livres ---");
				int n = 5; // Exemple : éditeurs ayant publié plus de 5 livres
				manager.publishersWithMoreThanNBooks(n);

				System.out.println("\n--- Classement par nombre total d’emprunts dans loanHistory ---");
				manager.rankPublishersByTotalLoans();

		}

		// 1. Nombre de livres publiés par éditeur
		public void countBooksPerPublisher() {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "books")
												.append("localField", "id")
												.append("foreignField", "publisherId")
												.append("as", "books")),
								new Document("$project", new Document("name", 1)
												.append("bookCount", new Document("$size", "$books"))),
								new Document("$sort", new Document("bookCount", -1))
				);
				publisherCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
		}

		// 2. Liste unique des auteurs publiés par éditeur
		public void listAuthorsPerPublisher() {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "books")
												.append("localField", "id")
												.append("foreignField", "publisherId")
												.append("as", "books")),
								new Document("$unwind", "$books"),
								new Document("$unwind", "$books.authorsId"),
								new Document("$lookup", new Document("from", "authors")
												.append("localField", "books.authorsId")
												.append("foreignField", "id")
												.append("as", "author")),
								new Document("$unwind", "$author"),
								new Document("$group", new Document("_id", "$name")
												.append("authors", new Document("$addToSet", "$author.name"))),
								new Document("$project", new Document("publisher", "$_id")
												.append("authors", 1)
												.append("_id", 0))
				);
				publisherCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
		}

		// 3. Moyenne du nombre de pages des livres par éditeur
		public void averagePagesPerPublisher() {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "books")
												.append("localField", "id")
												.append("foreignField", "publisherId")
												.append("as", "books")),
								new Document("$unwind", "$books"),
								new Document("$group", new Document("_id", "$name")
												.append("averagePages", new Document("$avg", "$books.pageCount"))),
								new Document("$sort", new Document("averagePages", -1))
				);
				publisherCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
		}

		// 4. Éditeurs ayant publié plus de N livres
		public void publishersWithMoreThanNBooks(int n) {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "books")
												.append("localField", "id")
												.append("foreignField", "publisherId")
												.append("as", "books")),
								new Document("$project", new Document("name", 1)
												.append("bookCount", new Document("$size", "$books"))),
								new Document("$match", new Document("bookCount", new Document("$gt", n))),
								new Document("$sort", new Document("bookCount", -1))
				);
				publisherCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
		}

		// 5. Classement par nombre total d’emprunts dans loanHistory
		public void rankPublishersByTotalLoans() {
				List<Document> pipeline = Arrays.asList(
								new Document("$lookup", new Document("from", "books")
												.append("localField", "id")
												.append("foreignField", "publisherId")
												.append("as", "books")),
								new Document("$unwind", "$books"),
								new Document("$project", new Document("name", 1)
												.append("loanCount", new Document("$cond", Arrays.asList(
																new Document("$isArray", "$books.loanHistory"),
																new Document("$size", "$books.loanHistory"),
																0)))),
								new Document("$group", new Document("_id", "$name")
												.append("totalLoans", new Document("$sum", "$loanCount"))),
								new Document("$sort", new Document("totalLoans", -1))
				);
				publisherCollection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
		}


}
