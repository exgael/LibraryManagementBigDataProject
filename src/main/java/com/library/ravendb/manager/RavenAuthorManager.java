package com.library.ravendb.manager;

import com.library.common.model.Author;
import com.library.common.model.Book;
import com.library.common.util.ModelDataGenerator;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;

import java.util.*;
import java.util.stream.Collectors;

public class RavenAuthorManager {

    private final IDocumentStore store;

    public RavenAuthorManager() {
        this.store = new DocumentStore("http://localhost:8080", "LibraryDB");
        this.store.initialize();
    }

    // 1. Count authors by nationality
    public void countAuthorsByNationality() {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class).toList();

            Map<String, Long> countByNationality = authors.stream()
                    .collect(Collectors.groupingBy(Author::getNationality, Collectors.counting()));

            countByNationality.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));
        }
    }

    // 2. List authors by nationality
    public void listAuthorsByNationality(String nationality) {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class)
                    .whereEquals("nationality", nationality)
                    .toList();

            authors.forEach(author -> System.out.println(author.getName()));
        }
    }

    // 3. Authors starting with a given letter
    public void findAuthorsStartingWith(char letter) {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class).toList();

            authors.stream()
                    .filter(author -> author.getName() != null && author.getName().toLowerCase().startsWith(String.valueOf(letter).toLowerCase()))
                    .forEach(author -> System.out.println(author.getName() + " (" + author.getNationality() + ")"));
        }
    }

    // 4. Compute name length
    public void computeNameLengthForAuthors() {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class).toList();

            authors.forEach(author -> {
                int length = author.getName() != null ? author.getName().length() : 0;
                System.out.println(author.getName() + " (Length: " + length + ")");
            });
        }
    }

    // 5. Sort authors by name
    public void sortAuthorsByName() {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class).toList();

            authors.stream()
                    .sorted(Comparator.comparing(Author::getName, String.CASE_INSENSITIVE_ORDER))
                    .forEach(author -> System.out.println(author.getName()));
        }
    }

    // 6. Simulate join with books using authorsId
    public void getAuthorsWithBooks() {
        try (IDocumentSession session = store.openSession()) {
            List<Author> authors = session.query(Author.class).toList();
            List<Book> books = session.query(Book.class).toList();

            for (Author author : authors) {
                String authorId = author.getId();
                List<Book> authorBooks = books.stream()
                        .filter(book -> book.getAuthorsId() != null && book.getAuthorsId().contains(authorId))
                        .collect(Collectors.toList());

                System.out.println("Author: " + author.getName());
                authorBooks.forEach(book -> System.out.println("  - " + book.getTitle()));
            }
        }
    }

    public static void main(String[] args) {
        RavenAuthorManager manager = new RavenAuthorManager();

        // Generate test data
        List<Author> testAuthors = ModelDataGenerator.generateAuthors(10);
        List<Book> testBooks = new ArrayList<>();

        try (IDocumentSession session = manager.store.openSession()) {
            for (int i = 0; i < testAuthors.size(); i++) {
                Author author = testAuthors.get(i);
                author.setId(UUID.randomUUID().toString());
                session.store(author);

                // Create book for each author
                Book book = new Book();
                book.setId(UUID.randomUUID().toString());
                book.setTitle("Book by " + author.getName());
                book.setAuthorsId(List.of(author.getId()));
                testBooks.add(book);
                session.store(book);
            }
            session.saveChanges();
        }

        System.out.println("1. Count Authors by Nationality:");
        manager.countAuthorsByNationality();

        String nationality = testAuthors.get(0).getNationality();
        System.out.println("\n2. List Authors by Nationality (" + nationality + "):");
        manager.listAuthorsByNationality(nationality);

        System.out.println("\n3. Authors Starting with 'A':");
        manager.findAuthorsStartingWith('A');

        System.out.println("\n4. Compute Name Length for Authors:");
        manager.computeNameLengthForAuthors();

        System.out.println("\n5. Sort Authors by Name:");
        manager.sortAuthorsByName();

        System.out.println("\n6. Authors with Books:");
        manager.getAuthorsWithBooks();
    }
}
