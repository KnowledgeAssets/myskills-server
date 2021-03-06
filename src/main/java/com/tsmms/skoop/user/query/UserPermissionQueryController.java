package com.tsmms.skoop.user.query;

import com.google.common.base.Enums;
import com.tsmms.skoop.user.UserPermission;
import com.tsmms.skoop.user.UserPermissionResponse;
import com.tsmms.skoop.user.UserPermissionScope;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Api(tags = "UserPermissions")
@RestController
public class UserPermissionQueryController {
	private UserPermissionQueryService userPermissionQueryService;

	public UserPermissionQueryController(UserPermissionQueryService userPermissionQueryService) {
		this.userPermissionQueryService = userPermissionQueryService;
	}

	@ApiOperation(
			value = "Get all user permissions granted to other users",
			notes = "Get all user permissions the given user has granted to other users in the system."
	)
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to access resource, e.g. foreign user data"),
			@ApiResponse(code = 404, message = "Resource not found"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@PreAuthorize("isPrincipalUserId(#userId)")
	@GetMapping(path = "/users/{userId}/outbound-permissions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserPermissionResponse> getOutboundUserPermissions(@PathVariable("userId") String userId,
																   @RequestParam(name = "scope", required = false) String scope) {
		final Optional<UserPermissionScope> userPermissionScope = scope != null ? Enums.getIfPresent(UserPermissionScope.class, scope)
				.transform(Optional::of).or(Optional.empty()) : Optional.empty();
		final Stream<UserPermission> userPermissionStream;
		if (userPermissionScope.isPresent()) {
			userPermissionStream = userPermissionQueryService.getOutboundUserPermissionsByOwnerIdAndScope(userId, userPermissionScope.get());
		} else {
			userPermissionStream = userPermissionQueryService.getOutboundUserPermissionsByOwnerId(userId);
		}
		return userPermissionStream.map(UserPermissionResponse::of).collect(toList());
	}

	@ApiOperation(
			value = "Get all user permissions granted to the user.",
			notes = "Get all user permissions the given user has been granted by other users in the system."
	)
	@ApiResponses({
			@ApiResponse(code = 200, message = "Successful execution"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to access resource, e.g. foreign user data"),
			@ApiResponse(code = 404, message = "Resource not found"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@PreAuthorize("isPrincipalUserId(#userId)")
	@GetMapping(path = "/users/{userId}/inbound-permissions", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UserPermissionResponse> getInboundUserPermissions(@PathVariable("userId") String userId,
																  @RequestParam(name = "scope", required = false) String scope) {
		final Optional<UserPermissionScope> userPermissionScope = scope != null ? Enums.getIfPresent(UserPermissionScope.class, scope)
				.transform(Optional::of).or(Optional.empty()) : Optional.empty();
		final Stream<UserPermission> userPermissionStream;
		if (userPermissionScope.isPresent()) {
			userPermissionStream = userPermissionQueryService.getInboundUserPermissionsByAuthorizedUserIdAndScope(userId, userPermissionScope.get());
		} else {
			userPermissionStream = userPermissionQueryService.getInboundUserPermissionsByAuthorizedUserId(userId);
		}
		return userPermissionStream.map(UserPermissionResponse::of).collect(toList());
	}

}
