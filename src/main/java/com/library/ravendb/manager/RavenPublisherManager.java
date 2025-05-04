package com.library.ravendb.manager;

import com.library.common.model.Book;
import com.library.common.model.Publisher;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.session.IDocumentSession;

import java.util.*;
import java.util.stream.Collectors;

public class RavenPublisherManager {

    // 1. Number of books published per publisher
    public void countBooksPerPublisher() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            // Load all publishers and books in one batch
            List<Publisher> publishers = session.query(Publisher.class).toList();
            Map<String, List<Book>> booksByPublisher = new HashMap<>();

            // Load all books whose publisherId is in the list
            List<Book> books = session.query(Book.class)
                    .whereIn("publisherId", publishers.stream().map(Publisher::getId).collect(Collectors.toList()))
                    .toList();

            // Group books by publisherId
            for (Book book : books) {
                booksByPublisher.computeIfAbsent(book.getPublisherId(), k -> new ArrayList<>()).add(book);
            }

            // Print the number of books per publisher
            for (Publisher publisher : publishers) {
                List<Book> publisherBooks = booksByPublisher.getOrDefault(publisher.getId(), Collections.emptyList());
                System.out.println("Publisher: " + publisher.getName() + " | Books Count: " + publisherBooks.size());
            }
        }
    }

    // 2. Unique list of authors per publisher
    public void listAuthorsPerPublisher() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Publisher> publishers = session.query(Publisher.class).toList();

            // Load all books for all publishers at once
            List<Book> books = session.query(Book.class)
                    .whereIn("publisherId", publishers.stream().map(Publisher::getId).collect(Collectors.toList()))
                    .toList();

            // Group authors by publisherId
            Map<String, Set<String>> authorsByPublisher = new HashMap<>();
            for (Book book : books) {
                for (String authorId : book.getAuthorsId()) {
                    authorsByPublisher
                            .computeIfAbsent(book.getPublisherId(), k -> new HashSet<>())
                            .add(authorId);
                }
            }

            // Print unique authors for each publisher
            for (Publisher publisher : publishers) {
                Set<String> authorIds = authorsByPublisher.getOrDefault(publisher.getId(), Collections.emptySet());
                System.out.println("Publisher: " + publisher.getName() + " | Authors: " + authorIds);
            }
        }
    }

    // 3. Average number of pages per publisher
    public void averagePagesPerPublisher() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Publisher> publishers = session.query(Publisher.class).toList();

            // Load all books at once
            List<Book> books = session.query(Book.class)
                    .whereIn("publisherId", publishers.stream().map(Publisher::getId).collect(Collectors.toList()))
                    .toList();

            // Group books by publisherId
            Map<String, List<Book>> booksByPublisher = new HashMap<>();
            for (Book book : books) {
                booksByPublisher.computeIfAbsent(book.getPublisherId(), k -> new ArrayList<>()).add(book);
            }

            // Compute average page count per publisher
            for (Publisher publisher : publishers) {
                List<Book> publisherBooks = booksByPublisher.getOrDefault(publisher.getId(), Collections.emptyList());
                double averagePages = publisherBooks.stream().mapToInt(Book::getPageCount).average().orElse(0.0);
                System.out.println("Publisher: " + publisher.getName() + " | Average Pages: " + averagePages);
            }
        }
    }

    // 4. Publishers who have published more than N books
    public void publishersWithMoreThanNBooks(int n) {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Publisher> publishers = session.query(Publisher.class).toList();

            // Load all books in one query
            List<Book> books = session.query(Book.class)
                    .whereIn("publisherId", publishers.stream().map(Publisher::getId).collect(Collectors.toList()))
                    .toList();

            // Group books by publisherId
            Map<String, List<Book>> booksByPublisher = new HashMap<>();
            for (Book book : books) {
                booksByPublisher.computeIfAbsent(book.getPublisherId(), k -> new ArrayList<>()).add(book);
            }

            // Print publishers that have published more than N books
            for (Publisher publisher : publishers) {
                List<Book> publisherBooks = booksByPublisher.getOrDefault(publisher.getId(), Collections.emptyList());
                if (publisherBooks.size() > n) {
                    System.out.println("Publisher: " + publisher.getName() + " | Books Count: " + publisherBooks.size());
                }
            }
        }
    }

    // 5. Ranking publishers by total loan count in loanHistory
    public void rankPublishersByTotalLoans() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Publisher> publishers = session.query(Publisher.class).toList();

            // Load all books in one query
            List<Book> books = session.query(Book.class)
                    .whereIn("publisherId", publishers.stream().map(Publisher::getId).collect(Collectors.toList()))
                    .toList();

            // Compute total loan count per publisher
            Map<String, Long> loanCountByPublisher = new HashMap<>();
            for (Book book : books) {
                long loanCount = (book.getLoanHistory() != null) ? book.getLoanHistory().size() : 0;
                loanCountByPublisher.merge(book.getPublisherId(), loanCount, Long::sum);
            }

            // Print publishers ranked by total loan count
            loanCountByPublisher.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> System.out.println("Publisher: " + e.getKey() + " | Total Loans: " + e.getValue()));
        }
    }

    public static void main(String[] args) {
        // Reset RavenDB database
        RavenConfig.resetDatabase();

        RavenPublisherManager manager = new RavenPublisherManager();

        // Generate and insert test publishers, books, and authors
        List<Publisher> publishers = ModelDataGenerator.generatePublishers(10);
        List<Book> books = ModelDataGenerator.generateBooks(30);  // Generate books

        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            for (Publisher publisher : publishers) {
                session.store(publisher);
            }
            for (Book book : books) {
                session.store(book);
            }
            session.saveChanges();
        }

        System.out.println("\n--- Number of books published per publisher ---");
        manager.countBooksPerPublisher();

        System.out.println("\n--- Unique list of authors per publisher ---");
        manager.listAuthorsPerPublisher();

        System.out.println("\n--- Average number of pages per publisher ---");
        manager.averagePagesPerPublisher();

        System.out.println("\n--- Publishers with more than N books ---");
        int n = 5;
        manager.publishersWithMoreThanNBooks(n);

        System.out.println("\n--- Ranking publishers by total loans ---");
        manager.rankPublishersByTotalLoans();
    }
}
