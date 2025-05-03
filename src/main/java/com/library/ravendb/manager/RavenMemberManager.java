package com.library.ravendb.manager;

import com.library.common.model.Category;
import com.library.common.model.Member;
import com.library.common.util.ModelDataGenerator;
import com.library.ravendb.RavenConfig;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.documents.session.IDocumentQuery;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class RavenMemberManager {

    private final DocumentStore store;

    public RavenMemberManager() {
        this.store = RavenConfig.getDocumentStore();
    }

    // 1. Count members by registration year
    public void countMembersByRegistrationYear() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();

            Map<Integer, Long> countByYear = members.stream()
                    .map(m -> LocalDateTime.ofInstant(Instant.ofEpochMilli(m.getRegistrationDate()), ZoneId.systemDefault()).getYear())
                    .collect(Collectors.groupingBy(y -> y, TreeMap::new, Collectors.counting()));

            countByYear.forEach((year, count) -> System.out.println("Year: " + year + ", Count: " + count));
        }
    }

    // 2. List members with overdue loans
    public void listMembersWithOverdueLoans() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();

            members.stream()
                    .filter(m -> m.getActiveLoans() != null && m.getActiveLoans().stream().anyMatch(Member.ActiveLoan::getIsOverdue))
                    .forEach(m -> {
                        List<Member.ActiveLoan> overdueLoans = m.getActiveLoans().stream()
                                .filter(Member.ActiveLoan::getIsOverdue)
                                .collect(Collectors.toList());

                        System.out.println("Member: " + m.getFirstName() + " " + m.getLastName() + ", Email: " + m.getEmail());
                        overdueLoans.forEach(loan -> System.out.println("  Overdue Book: " + loan.getBookTitle()));
                    });
        }
    }

    // 3. Count number of active loans per member
    public void countLoansPerMember() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();

            members.forEach(m -> {
                int count = m.getActiveLoans() == null ? 0 : m.getActiveLoans().size();
                System.out.println(m.getFirstName() + " " + m.getLastName() + ": " + count + " loans");
            });
        }
    }

    // 4. Most preferred categories
    public void mostPreferredCategories() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();
            Map<String, Integer> categoryScores = new HashMap<>();

            for (Member m : members) {
                if (m.getReadingStats() != null && m.getReadingStats().getCategoryPreferences() != null) {
                    for (Map.Entry<String, Integer> entry : m.getReadingStats().getCategoryPreferences().entrySet()) {
                        categoryScores.merge(entry.getKey(), entry.getValue(), Integer::sum);
                    }
                }
            }

            categoryScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> System.out.println("Category: " + e.getKey() + ", Score: " + e.getValue()));
        }
    }

    // 5. Top 5 favorite authors
    public void topFavoriteAuthors() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();
            Map<String, Long> authorCounts = new HashMap<>();

            for (Member m : members) {
                if (m.getReadingStats() != null && m.getReadingStats().getFavoriteAuthors() != null) {
                    for (String author : m.getReadingStats().getFavoriteAuthors()) {
                        authorCounts.merge(author, 1L, Long::sum);
                    }
                }
            }

            authorCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> System.out.println("Author: " + e.getKey() + ", Count: " + e.getValue()));
        }
    }

    // 6. Members with emergency contact info
    public void listMembersWithEmergencyContact() {
        try (IDocumentSession session = RavenConfig.getDocumentStore().openSession()) {
            List<Member> members = session.query(Member.class).toList();

            members.stream()
                    .filter(m -> m.getContactInfo() != null && m.getContactInfo().getEmergencyContact() != null)
                    .forEach(m -> {
                        Member.ContactInfo.EmergencyContact ec = m.getContactInfo().getEmergencyContact();
                        System.out.println(m.getFirstName() + " " + m.getLastName() +
                                " | Emergency Contact: " + ec.getName() + " - " + ec.getPhone());
                    });
        }
    }

    public static void main(String[] args) {
        // Reset RavenDB database
        RavenConfig.resetDatabase();

        RavenMemberManager manager = new RavenMemberManager();

        // Génère quelques membres pour test
        List<Member> members = ModelDataGenerator.generateMembers(10);

        try (IDocumentSession session = manager.store.openSession()) {
            for (Member member : members) {
                session.store(member);
            }
            session.saveChanges();
        }

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
