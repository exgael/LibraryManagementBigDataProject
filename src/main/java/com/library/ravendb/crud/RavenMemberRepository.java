package com.library.ravendb.crud;

import com.library.common.model.Member;
import com.library.ravendb.RavenConfig;
import com.library.common.util.ModelDataGenerator;
import net.ravendb.client.documents.session.IDocumentSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RavenDB Repository for Member entities
 */
public class RavenMemberRepository extends RavenGenericRepository<Member> {
		private static final Logger logger = LogManager.getLogger();

		public RavenMemberRepository() {
				super(Member.class, "member", "members");
		}

		/**
		 * Main method for testing CRUD operations
		 */
		public static void main(String[] args) {
				RavenMemberRepository repository = new RavenMemberRepository();

				// Reset database for clean testing
				logger.info("Resetting database");
				RavenConfig.resetDatabase();

				// Generate test members
				logger.info("Generating test members");
				List<Member> testMembers = ModelDataGenerator.generateMembers(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Member firstMember = testMembers.get(0);
				String insertedId = repository.insertOne(firstMember);
				logger.info("Inserted member ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Member> membersToInsert = testMembers.subList(1, testMembers.size());
				List<String> insertedIds = repository.insertMany(membersToInsert);
				logger.info("Inserted {} members with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all members ===");
				List<Member> allMembers = repository.find();
				logger.info("Found {} members", allMembers.size());

				if (!allMembers.isEmpty()) {
						Member sample = allMembers.get(0);
						logger.info("Sample member: Name={} {}, Email={}",
										sample.getFirstName(), sample.getLastName(), sample.getEmail());
				}

				// Test find by name
				if (!allMembers.isEmpty()) {
						Member sampleMember = allMembers.get(0);

						logger.info("=== Testing findByName ===");
						List<Member> membersByName = repository.findByName(sampleMember.getFirstName());
						logger.info("Found {} members with name containing '{}'", membersByName.size(), sampleMember.getFirstName());
				}

				// Test find by email
				if (!allMembers.isEmpty()) {
						logger.info("=== Testing findByEmail ===");
						// Find members with gmail in email
						List<Member> membersByEmail = repository.findByEmail("gmail");
						logger.info("Found {} members with email containing 'gmail'", membersByEmail.size());
				}

				// Test find members with overdue books
				logger.info("=== Testing findMembersWithOverdueBooks ===");
				List<Member> membersWithOverdueBooks = repository.findMembersWithOverdueBooks();
				logger.info("Found {} members with overdue books", membersWithOverdueBooks.size());

				// =====================
				// UPDATE OPERATIONS
				// =====================

				// Test update
				if (!allMembers.isEmpty()) {
						logger.info("=== Testing update member ===");
						Member memberToUpdate = allMembers.get(0);
						String originalFirstName = memberToUpdate.getFirstName();
						String originalLastName = memberToUpdate.getLastName();
						memberToUpdate.setFirstName(originalFirstName + "-Updated");
						memberToUpdate.setLastName(originalLastName + "-Updated");
						memberToUpdate.setEmail("updated." + memberToUpdate.getEmail());

						boolean updateResult = repository.update(memberToUpdate);
						logger.info("Update result: {}", updateResult);

						// Verify update
						Member updatedMember = repository.findById(memberToUpdate.getId());
						if (updatedMember != null) {
								logger.info("Original name: {} {}, Updated name: {} {}",
												originalFirstName, originalLastName,
												updatedMember.getFirstName(), updatedMember.getLastName());
								logger.info("Updated email: {}", updatedMember.getEmail());
						}
				}

				// Test update notification preferences
				logger.info("=== Testing update notification preferences ===");
				boolean updatePrefsResult = repository.updateNotificationPreferences(true, true);
				logger.info("Update notification preferences result: {}", updatePrefsResult);

				// =====================
				// DELETE OPERATIONS
				// =====================

				// Test delete one member
				if (!allMembers.isEmpty()) {
						logger.info("=== Testing delete member ===");
						Member memberToDelete = allMembers.get(0);
						boolean deleteResult = repository.delete(memberToDelete);
						logger.info("Delete result for member {}: {}", memberToDelete.getId(), deleteResult);

						// Verify member was deleted
						Member deletedMember = repository.findById(memberToDelete.getId());
						logger.info("Member still exists? {}", deletedMember != null);
				}

				// Test delete many members with gmail email
				logger.info("=== Testing delete many members ===");
				List<Member> gmailMembers = repository.findByEmail("gmail.com");
				logger.info("Gmail members before deletion: {}", gmailMembers.size());

				if (!gmailMembers.isEmpty()) {
						boolean deleteManyResult = repository.deleteMany("email", "gmail.com");
						logger.info("Delete many result: {}", deleteManyResult);

						// Verify deletion
						gmailMembers = repository.findByEmail("gmail.com");
						logger.info("Gmail members after deletion: {}", gmailMembers.size());
				}

				logger.info("All member tests completed successfully!");

				// Shutdown the RavenDB store when done
				RavenConfig.shutdown();
		}

		@Override
		protected String getEntityId(Member member) {
				return member.getId();
		}

		@Override
		protected void setEntityId(Member member, String id) {
				member.setId(id);
		}

		/**
		 * Find member by name
		 */
		public List<Member> findByName(String name) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Member.class)
										.whereRegex("firstName", "(?i).*" + name + ".*")
										.orElse()
										.whereRegex("lastName", "(?i).*" + name + ".*")
										.toList();
				}
		}

		/**
		 * Find member by email
		 */
		public List<Member> findByEmail(String email) {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Member.class)
										.whereRegex("email", "(?i).*" + email + ".*")
										.toList();
				}
		}

		/**
		 * Find members with overdue books
		 */
		public List<Member> findMembersWithOverdueBooks() {
				try (IDocumentSession session = store.openSession()) {
						return session.advanced().documentQuery(Member.class)
										.whereEquals("activeLoans[].isOverdue", true)
										.toList();
				}
		}

		/**
		 * Update notification preferences for all members
		 */
		public boolean updateNotificationPreferences(boolean emailEnabled, boolean overdueRemindersEnabled) {
				try (IDocumentSession session = store.openSession()) {
						List<Member> members = find();

						for (Member member : members) {
								// Create preferences map if it doesn't exist
								if (member.getPreferences() == null) {
										member.setPreferences(new HashMap<>());
								}

								// Create notification preferences map if it doesn't exist
								if (!member.getPreferences().containsKey("notificationPreferences")) {
										member.getPreferences().put("notificationPreferences", new HashMap<String, Boolean>());
								}

								// Update notification preferences
								@SuppressWarnings("unchecked")
								Map<String, Boolean> notificationPrefs =
												(Map<String, Boolean>) member.getPreferences().get("notificationPreferences");

								notificationPrefs.put("email", emailEnabled);
								notificationPrefs.put("overdueReminders", overdueRemindersEnabled);

								session.store(member);
						}

						session.saveChanges();
						logger.info("Updated notification preferences for {} members", members.size());
						return true;
				} catch (Exception e) {
						logger.error("Failed to update notification preferences: ", e);
						return false;
				}
		}
}