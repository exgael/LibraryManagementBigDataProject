package com.library.mangodb.crud;

import com.library.common.model.Member;
import com.library.common.util.ModelDataGenerator;
import com.library.mangodb.MangoUtils;
import com.library.mangodb.MongoConfig;
import com.mongodb.client.model.UpdateOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Question 7 - CRUD Operations
 */
public class MangoMemberRepository extends MongoGenericRepository<Member> {
		private static final Logger logger = LogManager.getLogger();

		public MangoMemberRepository() {
				super("members", "member");
		}

		public static void main(String[] args) {
				MangoMemberRepository repository = new MangoMemberRepository();

				// Drop existing db for clean testing
				logger.info("Resetting database");
				MongoConfig.resetDatabase();

				// Generate test members
				logger.info("Generating test members");
				List<Member> testMembers = ModelDataGenerator.generateMembers(10);

				// =====================
				// CREATE OPERATIONS
				// =====================

				// Test insertOne
				logger.info("=== Testing insertOne ===");
				Member firstMember = testMembers.get(0);
				ObjectId insertedId = repository.insertOne(firstMember);
				logger.info("Inserted member ID: {}", insertedId);

				// Test insertMany
				logger.info("=== Testing insertMany ===");
				List<Member> membersToInsert = testMembers.subList(1, testMembers.size());
				List<ObjectId> insertedIds = repository.insertMany(membersToInsert);
				logger.info("Inserted {} members with IDs: {}", insertedIds.size(), insertedIds);

				// =====================
				// READ OPERATIONS
				// =====================

				// Test find all
				logger.info("=== Testing find all members ===");
				List<Member> allMembers = repository.find(new Document());
				logger.info("Found {} members", allMembers.size());

				if (!allMembers.isEmpty()) {
						Member sample = allMembers.get(0);
						logger.info("Sample member: Name={} {}, Email={}",
										sample.getFirstName(), sample.getLastName(), sample.getEmail());
				}

				// Test find with projection
				logger.info("=== Testing find with projection ===");
				Document projection = new Document("firstName", 1).append("lastName", 1).append("email", 1);
				List<Member> membersWithProjection = repository.find(new Document(), projection);
				logger.info("Found {} members with projection", membersWithProjection.size());

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

				// Test update many - update notification preferences
				logger.info("=== Testing update many members ===");
				Document query = new Document();
				Document update = new Document("$set",
								new Document("preferences.notificationPreferences.email", true)
												.append("preferences.notificationPreferences.overdueReminders", true));
				UpdateOptions updateOptions = new UpdateOptions();
				boolean updateManyResult = repository.updateMany(query, update, updateOptions);
				logger.info("Update many result: {}", updateManyResult);

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

				// Test delete many members
				logger.info("=== Testing delete many members ===");

				// Get count before deletion
				List<Member> membersBeforeDeletion = repository.find(new Document());
				logger.info("Members before deletion: {}", membersBeforeDeletion.size());

				// Delete members with a specific email pattern
				Document deleteQuery = new Document("email", new Document("$regex", "gmail\\.com$").append("$options", "i"));
				boolean deleteManyResult = repository.deleteMany(deleteQuery);
				logger.info("Delete many result: {}", deleteManyResult);

				// Get count after deletion
				List<Member> membersAfterDeletion = repository.find(new Document());
				logger.info("Members after deletion: {}", membersAfterDeletion.size());
				logger.info("Deleted {} members", membersBeforeDeletion.size() - membersAfterDeletion.size());


				logger.info("All member tests completed successfully!");
		}

		@Override
		protected Document entityToDocument(Member member) {
				Document doc = new Document();

				if (member.getId() != null) {
						doc.append("_id", toObjectId(member.getId()));
				}

				// Add basic fields
				doc.append("firstName", member.getFirstName())
								.append("lastName", member.getLastName())
								.append("email", member.getEmail())
								.append("address", member.getAddress())
								.append("registrationDate", member.getRegistrationDate());

				// Add contact info
				if (member.getContactInfo() != null) {
						Document contactDoc = new Document()
										.append("phone", member.getContactInfo().getPhone())
										.append("alternateEmail", member.getContactInfo().getAlternateEmail());

						// Add emergency contact
						if (member.getContactInfo().getEmergencyContact() != null) {
								Document emergencyDoc = new Document()
												.append("name", member.getContactInfo().getEmergencyContact().getName())
												.append("relationship", member.getContactInfo().getEmergencyContact().getRelationship())
												.append("phone", member.getContactInfo().getEmergencyContact().getPhone());

								contactDoc.append("emergencyContact", emergencyDoc);
						}

						doc.append("contactInfo", contactDoc);
				}

				// Add active loans
				if (member.getActiveLoans() != null && !member.getActiveLoans().isEmpty()) {
						List<Document> loanDocs = new ArrayList<>();
						for (Member.ActiveLoan loan : member.getActiveLoans()) {
								Document loanDoc = new Document()
												.append("bookId", toObjectId(loan.getBookId()))
												.append("bookTitle", loan.getBookTitle())
												.append("isbn", loan.getIsbn())
												.append("loanDate", loan.getLoanDate())
												.append("dueDate", loan.getDueDate())
												.append("isOverdue", loan.getIsOverdue());

								loanDocs.add(loanDoc);
						}
						doc.append("activeLoans", loanDocs);
				}

				// Add reading stats
				if (member.getReadingStats() != null) {
						Document statsDoc = new Document()
										.append("totalBooksRead", member.getReadingStats().getTotalBooksRead())
										.append("booksReadThisYear", member.getReadingStats().getBooksReadThisYear())
										.append("averageDaysToReturn", member.getReadingStats().getAverageDaysToReturn());

						// Add category preferences
						if (member.getReadingStats().getCategoryPreferences() != null) {
								Document prefsDoc = new Document();
								for (Map.Entry<String, Integer> entry : member.getReadingStats().getCategoryPreferences().entrySet()) {
										prefsDoc.append(entry.getKey(), entry.getValue());
								}
								statsDoc.append("categoryPreferences", prefsDoc);
						}

						// Add favorite authors
						if (member.getReadingStats().getFavoriteAuthors() != null) {
								statsDoc.append("favoriteAuthors", member.getReadingStats().getFavoriteAuthors());
						}

						doc.append("readingStats", statsDoc);
				}

				// Add preferences
				if (member.getPreferences() != null && !member.getPreferences().isEmpty()) {
						Document prefsDoc = new Document();

						// Handle string preferences
						if (member.getPreferences().containsKey("preferredFormat")) {
								prefsDoc.append("preferredFormat", member.getPreferences().get("preferredFormat"));
						}

						// Handle notification preferences map
						if (member.getPreferences().containsKey("notificationPreferences") &&
										member.getPreferences().get("notificationPreferences") instanceof Map) {

								@SuppressWarnings("unchecked")
								Map<String, Boolean> notificationPrefs =
												(Map<String, Boolean>) member.getPreferences().get("notificationPreferences");

								Document notifDoc = new Document();
								for (Map.Entry<String, Boolean> entry : notificationPrefs.entrySet()) {
										notifDoc.append(entry.getKey(), entry.getValue());
								}
								prefsDoc.append("notificationPreferences", notifDoc);
						}

						// Handle favorite subjects list
						if (member.getPreferences().containsKey("favoriteSubjects") &&
										member.getPreferences().get("favoriteSubjects") instanceof List) {

								@SuppressWarnings("unchecked")
								List<String> favoriteSubjects = (List<String>) member.getPreferences().get("favoriteSubjects");
								prefsDoc.append("favoriteSubjects", favoriteSubjects);
						}

						doc.append("preferences", prefsDoc);
				}

				return doc;
		}

		@Override
		protected Member documentToEntity(Document doc) {
				if (doc == null) {
						return null;
				}

				Member member = new Member();

				// Set basic fields
				member.setId(MangoUtils.getIdAsString(doc, "_id"));
				member.setFirstName(doc.getString("firstName"));
				member.setLastName(doc.getString("lastName"));
				member.setEmail(doc.getString("email"));
				member.setAddress(doc.getString("address"));

				// Handle primitive types with null checks
				Long registrationDate = doc.getLong("registrationDate");
				if (registrationDate != null) {
						member.setRegistrationDate(registrationDate);
				}

				// Convert contact info
				Document contactDoc = (Document) doc.get("contactInfo");
				if (contactDoc != null) {
						Member.ContactInfo contactInfo = new Member.ContactInfo();
						contactInfo.setPhone(contactDoc.getString("phone"));
						contactInfo.setAlternateEmail(contactDoc.getString("alternateEmail"));

						// Convert emergency contact
						Document emergencyDoc = (Document) contactDoc.get("emergencyContact");
						if (emergencyDoc != null) {
								Member.ContactInfo.EmergencyContact emergency =
												new Member.ContactInfo.EmergencyContact();

								emergency.setName(emergencyDoc.getString("name"));
								emergency.setRelationship(emergencyDoc.getString("relationship"));
								emergency.setPhone(emergencyDoc.getString("phone"));

								contactInfo.setEmergencyContact(emergency);
						}

						member.setContactInfo(contactInfo);
				}

				// Convert active loans
				@SuppressWarnings("unchecked")
				List<Document> loanDocs = (List<Document>) doc.get("activeLoans");
				if (loanDocs != null && !loanDocs.isEmpty()) {
						List<Member.ActiveLoan> activeLoans = new ArrayList<>();
						for (Document loanDoc : loanDocs) {
								Member.ActiveLoan loan = new Member.ActiveLoan();
								loan.setBookId(MangoUtils.getIdAsString(loanDoc, "bookId"));
								loan.setBookTitle(loanDoc.getString("bookTitle"));
								loan.setIsbn(loanDoc.getString("isbn"));
								loan.setLoanDate(loanDoc.getLong("loanDate"));
								loan.setDueDate(loanDoc.getLong("dueDate"));

								Boolean isOverdue = loanDoc.getBoolean("isOverdue");
								if (isOverdue != null) {
										loan.setOverdue(isOverdue);
								}

								activeLoans.add(loan);
						}
						member.setActiveLoans(activeLoans);
				}

				// Convert reading stats
				Document statsDoc = (Document) doc.get("readingStats");
				if (statsDoc != null) {
						Member.ReadingStats stats = new Member.ReadingStats();

						Integer totalBooksRead = statsDoc.getInteger("totalBooksRead");
						if (totalBooksRead != null) {
								stats.setTotalBooksRead(totalBooksRead);
						}

						Integer booksReadThisYear = statsDoc.getInteger("booksReadThisYear");
						if (booksReadThisYear != null) {
								stats.setBooksReadThisYear(booksReadThisYear);
						}

						Integer averageDaysToReturn = statsDoc.getInteger("averageDaysToReturn");
						if (averageDaysToReturn != null) {
								stats.setAverageDaysToReturn(averageDaysToReturn);
						}

						// Convert category preferences
						Document prefsDoc = (Document) statsDoc.get("categoryPreferences");
						if (prefsDoc != null) {
								Map<String, Integer> categoryPreferences = new HashMap<>();
								for (String key : prefsDoc.keySet()) {
										categoryPreferences.put(key, prefsDoc.getInteger(key));
								}
								stats.setCategoryPreferences(categoryPreferences);
						}

						// Convert favorite authors
						@SuppressWarnings("unchecked")
						List<String> favoriteAuthors = (List<String>) statsDoc.get("favoriteAuthors");
						if (favoriteAuthors != null) {
								stats.setFavoriteAuthors(favoriteAuthors);
						}

						member.setReadingStats(stats);
				}

				// Convert preferences
				Document prefsDoc = (Document) doc.get("preferences");
				if (prefsDoc != null) {
						Map<String, Object> preferences = new HashMap<>();

						// Handle string preferences
						if (prefsDoc.containsKey("preferredFormat")) {
								preferences.put("preferredFormat", prefsDoc.getString("preferredFormat"));
						}

						// Handle notification preferences
						Document notifDoc = (Document) prefsDoc.get("notificationPreferences");
						if (notifDoc != null) {
								Map<String, Boolean> notificationPrefs = new HashMap<>();
								for (String key : notifDoc.keySet()) {
										notificationPrefs.put(key, notifDoc.getBoolean(key));
								}
								preferences.put("notificationPreferences", notificationPrefs);
						}

						// Handle favorite subjects
						@SuppressWarnings("unchecked")
						List<String> favoriteSubjects = (List<String>) prefsDoc.get("favoriteSubjects");
						if (favoriteSubjects != null) {
								preferences.put("favoriteSubjects", favoriteSubjects);
						}

						member.setPreferences(preferences);
				}

				return member;
		}

		@Override
		protected String getEntityId(Member member) {
				return member.getId();
		}

		/**
		 * Find member by name
		 */
		public List<Member> findByName(String name) {
				Document query = new Document("$or", List.of(
								new Document("firstName", new Document("$regex", name).append("$options", "i")),
								new Document("lastName", new Document("$regex", name).append("$options", "i"))
				));
				return find(query);
		}

		/**
		 * Find member by email
		 */
		public List<Member> findByEmail(String email) {
				Document query = new Document("email", new Document("$regex", email).append("$options", "i"));
				return find(query);
		}

		/**
		 * Find members with overdue books
		 */
		public List<Member> findMembersWithOverdueBooks() {
				Document query = new Document("activeLoans.overdue", true);
				return find(query);
		}
}