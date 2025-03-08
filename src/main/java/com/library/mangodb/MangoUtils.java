package com.library.mangodb;

import org.bson.Document;
import org.bson.types.ObjectId;

public class MangoUtils {

		/**
		 * Convert a String ID to ObjectId if possible, otherwise return the original string
		 */
		public static Object toObjectId(String id) {
				if (id == null) {
						return null;
				}

				try {
						return new ObjectId(id);
				} catch (IllegalArgumentException e) {
						// If the ID is not a valid ObjectId, just use it as a string
						return id;
				}
		}

		/**
		 * Extract ID value from document
		 */
		public static String getIdAsString(Document doc, String fieldName) {
				if (!doc.containsKey(fieldName)) {
						return null;
				}

				Object idObj = doc.get(fieldName);
				if (idObj instanceof ObjectId) {
						return ((ObjectId) idObj).toHexString();
				} else if (idObj instanceof String) {
						return (String) idObj;
				}
				return idObj != null ? idObj.toString() : null;
		}
}