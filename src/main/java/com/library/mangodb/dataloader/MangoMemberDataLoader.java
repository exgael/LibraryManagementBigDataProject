package com.library.mangodb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.mangodb.crud.MangoMemberRepository;
import com.library.common.model.Member;

import java.util.List;

/**
 * Question 6 - Member data loading via MongoDB
 */
public class MangoMemberDataLoader extends MongoGenericDataLoader<Member> {
		private static final String MEMBERS_FILENAME = "members.json";

		public MangoMemberDataLoader(String dataPath, int memberCount) {
				super(dataPath, memberCount, new MangoMemberRepository());
		}

		public static void main(String[] args) {
				MangoMemberDataLoader loader = new MangoMemberDataLoader("generated-data", 50);
				loader.loadData();
		}

		@Override
		protected String getEntityName() {
				return "member";
		}

		@Override
		protected String getEntityFileName() {
				return MEMBERS_FILENAME;
		}

		@Override
		protected TypeReference<List<Member>> getTypeReference() {
				return new TypeReference<>() {
				};
		}
}