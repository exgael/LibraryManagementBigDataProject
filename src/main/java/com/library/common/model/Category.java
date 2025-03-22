package com.library.common.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
		private String name;
		private String path; // e.g., "Fiction/Science Fiction"
		private String id;
}
