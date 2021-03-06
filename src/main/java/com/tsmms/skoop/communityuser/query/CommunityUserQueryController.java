package com.tsmms.skoop.communityuser.query;

import com.tsmms.skoop.community.CommunityRole;
import com.tsmms.skoop.community.query.CommunityQueryService;
import com.tsmms.skoop.exception.UserCommunityException;
import com.tsmms.skoop.security.CurrentUserService;
import com.tsmms.skoop.user.UserSimpleResponse;
import com.tsmms.skoop.communityuser.CommunityUserResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Api(tags = { "CommunityUsers" })
@RestController
public class CommunityUserQueryController {

	private final CommunityUserQueryService communityUserQueryService;
	private final CommunityQueryService communityQueryService;
	private final CurrentUserService currentUserService;

	public CommunityUserQueryController(CommunityUserQueryService communityUserQueryService,
										CommunityQueryService communityQueryService,
										CurrentUserService currentUserService) {
		this.communityUserQueryService = requireNonNull(communityUserQueryService);
		this.currentUserService = requireNonNull(currentUserService);
		this.communityQueryService = requireNonNull(communityQueryService);
	}


	@ApiOperation(value = "Gets users of the specified community.",
			notes = "Gets users of the specified community. Only managers and members of the given community are allowed to access this endpoint.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 400, message = "Invalid input data, e.g. missing mandatory data or community name exists"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to perform this operation"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@GetMapping(path = "/communities/{communityId}/users")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<CommunityUserResponse>> getCommunityUsers(@PathVariable("communityId") String communityId,
																		 @RequestParam(name = "role", required = false) String role) {
		if (communityQueryService.isCommunityMember(currentUserService.getCurrentUserId(), communityId)) {
			final CommunityRole communityRole;
			if (!StringUtils.isEmpty(role)) {
				communityRole = CommunityRole.valueOf(role);
			} else {
				communityRole = null;
			}
			return ResponseEntity.ok(communityUserQueryService
					.getCommunityUsers(communityId, communityRole).map(CommunityUserResponse::of).collect(toList()));
		} else {
			throw new UserCommunityException("Only managers and members of the given community are allowed to access the endpoint.");
		}
	}

	@ApiOperation(value = "Gets users who are not members of the community.",
			notes = "Gets users who are not members of the community. The endpoint can be used to choose users to invite to join community.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 400, message = "Invalid input data, e.g. missing mandatory data or community name exists"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to perform this operation"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@GetMapping(path = "/communities/{communityId}/user-suggestions")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<UserSimpleResponse>> getCommunityUserSuggestions(@PathVariable("communityId") String communityId,
																				@RequestParam(name = "search", required = false) String search) {
		return ResponseEntity.ok(communityUserQueryService
				.getUsersNotRelatedToCommunity(communityId, search).map(UserSimpleResponse::of).collect(toList()));
	}

	@ApiOperation(value = "Gets users who are not members of the community and are recommended to be invited.",
			notes = "Gets users who are not member of the community and are recommended to be invited.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 400, message = "Invalid input data, e.g. missing mandatory data or community name exists"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to perform this operation"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@GetMapping(path = "/communities/{communityId}/recommended-users")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<UserSimpleResponse>> getUsersRecommendedToBeInvitedToJoinCommunity(@PathVariable("communityId") String communityId) {
		return ResponseEntity.ok(communityUserQueryService
				.getUsersRecommendedToBeInvitedToJoinCommunity(communityId).map(UserSimpleResponse::of).collect(toList()));
	}

}
