package com.library.ravendb.dataloader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.library.common.model.Member;
import com.library.ravendb.crud.RavenMemberRepository;

import java.util.List;

/**
 * Member data loading via RavenDB
 */
public class RavenMemberDataLoader extends RavenGenericDataLoader<Member> {
		private static final String MEMBERS_FILENAME = "members.json";

		public RavenMemberDataLoader(String dataPath, int memberCount) {
				super(dataPath, memberCount, new RavenMemberRepository());
		}

		public static void main(String[] args) {
				RavenMemberDataLoader loader = new RavenMemberDataLoader("generated-data", 50);
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