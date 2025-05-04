package com.library.common.model;

public class Category {
		private String name;
		private String path; // e.g., "Fiction/Science Fiction"
		private String id;

		public String getName() {
				return name;
		}

		public void setName(String name) {
				this.name = name;
		}

		public String getPath() {
				return path;
		}

		public void setPath(String path) {
				this.path = path;
		}

		public String getId() {
				return id;
		}

		public void setId(String id) {
				this.id = id;
		}
}
