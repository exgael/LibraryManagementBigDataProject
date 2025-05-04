package com.library.ravendb;

import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.serverwide.DatabaseRecord;
import net.ravendb.client.serverwide.operations.CreateDatabaseOperation;
import net.ravendb.client.serverwide.operations.DeleteDatabasesOperation;
import net.ravendb.client.serverwide.operations.GetDatabaseNamesOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RavenConfig {
		private static final Logger logger = LogManager.getLogger();
		private static final String DATABASE_NAME = "LibraryDB";
		private static final String[] URLS = new String[]{"http://localhost:8080"};
		private static final String[] KNOWN_COLLECTIONS = new String[]{
						"Members", "Books", "Authors", "Categories", "Publishers"
		};
		private static DocumentStore store;

		public static synchronized DocumentStore getDocumentStore() {
				if (store == null) {
						logger.info("Initializing RavenDB DocumentStore for {}", DATABASE_NAME);
						store = new DocumentStore(URLS, DATABASE_NAME);

						// Configure conventions if needed
						store.getConventions().setIdentityPartsSeparator('/');
						store.getConventions().setMaxNumberOfRequestsPerSession(5000);

						// Initialize store
						store.initialize();

						// Ensure database exists
						ensureDatabaseExists();
				}
				return store;
		}

		private static void ensureDatabaseExists() {
				try {
						// Get list of database names
						List<String> databaseNames = Arrays.asList(store.maintenance().server()
										.send(new GetDatabaseNamesOperation(0, 100)));

						boolean exists = databaseNames.contains(DATABASE_NAME);

						if (!exists) {
								logger.info("Creating database: {}", DATABASE_NAME);
								store.maintenance().server()
												.send(new CreateDatabaseOperation(new DatabaseRecord(DATABASE_NAME)));
						}
				} catch (Exception e) {
						logger.error("Error ensuring database exists: ", e);
				}
		}

		public static void resetDatabase() {
				try {
						logger.info("Resetting database: {}", DATABASE_NAME);

						DocumentStore store = getDocumentStore();

						// Delete all documents from known collections
						for (String collection : KNOWN_COLLECTIONS) {
								logger.info("Clearing collection: {}", collection);

								// Use a separate session for each collection to avoid transaction size issues
								try (var session = store.openSession()) {
										// Use the operation directly with the collection name
										session.advanced().documentQuery(Object.class)
														.waitForNonStaleResults()
														.toList()
														.forEach(document -> {
																String id = session.advanced().getDocumentId(document);
																session.delete(id);
														});

										// Save changes for this collection
										session.saveChanges();
								}
						}

						logger.info("Database reset completed (collections cleared): {}", DATABASE_NAME);
				} catch (Exception e) {
						logger.error("Error resetting database: ", e);
				}
		}

		public static void shutdown() {
				if (store != null) {
						store.close();
						store = null;
				}
		}
}