package com.library.ravendb.manager;

import com.library.common.model.Author;
import com.library.common.model.Book;
import com.library.common.model.Category;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;

import java.util.*;
import java.util.stream.Collectors;

public class RavenBookManager {

    private final DocumentStore store;

    public RavenBookManager() {
        this.store = RavenConfig.getDocumentStore();
    }

    // 1. Count the number of books published per year
    public void countBooksPerPublicationYear() {
        try (IDocumentSession session = store.openSession()) {
            List<Book> books = session.query(Book.class).toList();
            Map<Integer, Long> countByYear = books.stream()
                    .collect(Collectors.groupingBy(Book::getPublicationYear, TreeMap::new, Collectors.counting()));

            countByYear.forEach((year, count) ->
                    System.out.println("Year: " + year + ", Count: " + count));
        }
    }

    // 2. Compute the average page count of books per author
    public void averagePageCountPerAuthor() {
        try (IDocumentSession session = store.openSession()) {
            List<Book> books = session.query(Book.class).toList();
            Map<String, List<Integer>> authorPageCounts = new HashMap<>();

            for (Book book : books) {
                for (String authorId : book.getAuthorsId()) {
                    authorPageCounts.computeIfAbsent(authorId, k -> new ArrayList<>()).add(book.getPageCount());
                }
            }

            for (Map.Entry<String, List<Integer>> entry : authorPageCounts.entrySet()) {
                String authorId = entry.getKey();
                List<Integer> pages = entry.getValue();
                double avg = pages.stream().mapToInt(i -> i).average().orElse(0);
                Author author = session.load(Author.class, authorId);
                String authorName = author != null ? author.getName() : "Unknown";
                System.out.println("Author: " + authorName + ", Avg Pages: " + avg);
            }
        }
    }

    // 3. List all books with their associated authors
    public void listBooksWithAuthors() {
        try (IDocumentSession session = store.openSession()) {
            List<Book> books = session.query(Book.class).toList();
            for (Book book : books) {
                List<String> authorNames = book.getAuthorsId().stream()
                        .map(id -> session.load(Author.class, id))
                        .filter(Objects::nonNull)
                        .map(Author::getName)
                        .collect(Collectors.toList());
                System.out.println("Title: " + book.getTitle() +
                        ", Year: " + book.getPublicationYear() +
                        ", Authors: " + authorNames);
            }
        }
    }

    // 4. Count the number of books written by each author
    public void countBooksByAuthor() {
        try (IDocumentSession session = store.openSession()) {
            List<Book> books = session.query(Book.class).toList();
            Map<String, Integer> authorCounts = new HashMap<>();

            for (Book book : books) {
                for (String authorId : book.getAuthorsId()) {
                    authorCounts.merge(authorId, 1, Integer::sum);
                }
            }

            for (Map.Entry<String, Integer> entry : authorCounts.entrySet()) {
                String authorId = entry.getKey();
                Author author = session.load(Author.class, authorId);
                String name = author != null ? author.getName() : "Unknown";
                System.out.println("Author: " + name + ", Book Count: " + entry.getValue());
            }
        }
    }

    // 5. Find all books whose title starts with a given letter (case-insensitive)
    public void findBooksStartingWith(char letter) {
        try (IDocumentSession session = store.openSession()) {
            List<Book> books = session.query(Book.class)
                    .whereStartsWith("title", String.valueOf(letter))
                    .toList();

            for (Book book : books) {
                System.out.println("Title: " + book.getTitle() + ", Year: " + book.getPublicationYear());
            }
        }
    }

    public static void main(String[] args) {
        // Reset database
        RavenConfig.resetDatabase();

        // Managers
        RavenBookManager manager = new RavenBookManager();
        DocumentStore store = RavenConfig.getDocumentStore();

        // Insert authors
        List<Author> authors = ModelDataGenerator.generateAuthors(5);
        try (IDocumentSession session = store.openSession()) {
            for (Author author : authors) {
                session.store(author);
            }
            session.saveChanges();
        }

        // Insert categories
        List<Category> categories = ModelDataGenerator.generateCategories();
        try (IDocumentSession session = store.openSession()) {
            for (Category category : categories) {
                session.store(category);
            }
            session.saveChanges();
        }

        // Get stored authors and categories
        List<Author> storedAuthors;
        List<Category> storedCategories;
        try (IDocumentSession session = store.openSession()) {
            storedAuthors = session.query(Author.class).toList();
            storedCategories = session.query(Category.class).toList();
        }

        // Insert books
        List<Book> books = ModelDataGenerator.generateBooks(10);
        Random random = new Random();
        try (IDocumentSession session = store.openSession()) {
            for (Book book : books) {
                List<String> randomAuthorIds = storedAuthors.stream()
                        .limit(2)
                        .map(Author::getId)
                        .collect(Collectors.toList());

                String randomCategoryId = storedCategories.get(random.nextInt(storedCategories.size())).getId();

                book.setAuthorsId(randomAuthorIds);
                book.setCategoryId(randomCategoryId);
                book.setPublisherId("publishers/1"); // dummy publisher
                book.setAvailable(true);
                session.store(book);
            }
            session.saveChanges();
        }

        // Run tests
        System.out.println("1. Books by year:");
        manager.countBooksPerPublicationYear();

        System.out.println("\n2. Avg pages per author:");
        manager.averagePageCountPerAuthor();

        System.out.println("\n3. Books with authors:");
        manager.listBooksWithAuthors();

        System.out.println("\n4. Books by author:");
        manager.countBooksByAuthor();

        System.out.println("\n5. Books starting with 'L':");
        manager.findBooksStartingWith('L');
    }
}
