package com.library.mangodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * MongoDB configuration class
 * Singleton class to manage MongoDB connection
 */
public class MongoConfig {
		private static final Logger logger = LogManager.getLogger();
		private static final String CONNECTION_STRING = "mongodb://localhost:27017";
		private static final String DATABASE_NAME = "library_management";

		private static MongoClient mongoClient;

		public static MongoDatabase getDatabase() {
				if (mongoClient == null) {
						logger.info("Initializing MongoDB connection");
						mongoClient = MongoClients.create(CONNECTION_STRING);
				}
				return mongoClient.getDatabase(DATABASE_NAME);
		}

		public static void resetDatabase() {
				logger.info("Dropping database");
				getDatabase().drop();
		}

		public static void closeConnection() {
				if (mongoClient != null) {
						logger.info("Closing MongoDB connection");
						mongoClient.close();
						mongoClient = null;
				}
		}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException("Error converting to JSON", e);
		}
	}
}