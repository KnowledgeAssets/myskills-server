package com.tsmms.skoop.communityuser.query;

import com.tsmms.skoop.community.CommunityRole;
import com.tsmms.skoop.user.User;
import com.tsmms.skoop.communityuser.CommunityUser;
import com.tsmms.skoop.communityuser.CommunityUserRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class CommunityUserQueryService {

	private final CommunityUserRepository communityUserRepository;

	public CommunityUserQueryService(CommunityUserRepository communityUserRepository) {
		this.communityUserRepository = communityUserRepository;
	}

	@Transactional(readOnly = true)
	public Stream<CommunityUser> getCommunityUsers(String communityId, CommunityRole role) {
		if (communityId == null) {
			throw new IllegalArgumentException("Community ID cannot be null");
		}
		if (role == null) {
			return communityUserRepository.findByCommunityId(communityId);
		} else {
			return communityUserRepository.findByCommunityIdAndRole(communityId, role);
		}
	}

	@Transactional(readOnly = true)
	public Stream<User> getUsersNotRelatedToCommunity(String communityId, String search) {
		if (StringUtils.isEmpty(communityId)) {
			throw new IllegalArgumentException("Community ID cannot be empty.");
		}
		return StreamSupport.stream(communityUserRepository.getUsersNotRelatedToCommunity(communityId, search).spliterator(), false);
	}

	@Transactional(readOnly = true)
	public Stream<User> getUsersRecommendedToBeInvitedToJoinCommunity(String communityId) {
		if (StringUtils.isEmpty(communityId)) {
			throw new IllegalArgumentException("Community ID cannot be empty.");
		}
		return StreamSupport.stream(communityUserRepository.getUsersRecommendedToBeInvitedToJoinCommunity(communityId).spliterator(), false);
	}

}
