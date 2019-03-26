package io.knowledgeassets.myskills.server.communityuser.registration;

import io.knowledgeassets.myskills.server.user.UserSimpleResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(
		value = "CommunityUserRegistrationResponse",
		description = "This holds community user registration data. It is used to transfer community user registration data to a client."
)
public class CommunityUserRegistrationResponse {

	@ApiModelProperty("Registered user")
	private UserSimpleResponse user;
	@ApiModelProperty("Flag indicating if user registration approved by user.")
	private Boolean approvedByUser;
	@ApiModelProperty("Flag indicating if user registration approved by community.")
	private Boolean approvedByCommunity;

	public static CommunityUserRegistrationResponse of(CommunityUserRegistration communityUserRegistration) {
		return CommunityUserRegistrationResponse.builder()
				.user(UserSimpleResponse.of(communityUserRegistration.getRegisteredUser()))
				.approvedByCommunity(communityUserRegistration.getApprovedByCommunity())
				.approvedByUser(communityUserRegistration.getApprovedByUser())
				.build();
	}

}