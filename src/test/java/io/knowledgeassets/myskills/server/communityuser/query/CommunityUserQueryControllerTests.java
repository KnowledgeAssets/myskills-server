package io.knowledgeassets.myskills.server.communityuser.query;

import io.knowledgeassets.myskills.server.common.AbstractControllerTests;
import io.knowledgeassets.myskills.server.community.CommunityRole;
import io.knowledgeassets.myskills.server.communityuser.CommunityUser;
import io.knowledgeassets.myskills.server.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static io.knowledgeassets.myskills.server.common.JwtAuthenticationFactory.withUser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommunityUserQueryController.class)
class CommunityUserQueryControllerTests extends AbstractControllerTests {

	@MockBean
	private CommunityUserQueryService communityUserQueryService;

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("Users of community are fetched.")
	@Test
	void communityUsersAreFetched() throws Exception {

		final User tester = User.builder()
				.id("1f37fb2a-b4d0-4119-9113-4677beb20ae2")
				.userName("tester")
				.build();

		given(securityService.isCommunityManager("123")).willReturn(true);
		given(securityService.isCommunityMember("123")).willReturn(true);

		given(communityUserQueryService.getCommunityUsers("123", null))
				.willReturn(Stream.of(CommunityUser.builder()
								.role(CommunityRole.MANAGER)
								.user(tester)
								.build(),
						CommunityUser.builder()
								.role(CommunityRole.MEMBER)
								.user(tester)
								.build()
				));

		mockMvc.perform(get("/communities/123/users")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester)))
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].role", is(equalTo("MANAGER"))))
				.andExpect(jsonPath("$[0].user.id", is(equalTo("1f37fb2a-b4d0-4119-9113-4677beb20ae2"))))
				.andExpect(jsonPath("$[0].user.userName", is(equalTo("tester"))))
				.andExpect(jsonPath("$[1].role", is(equalTo("MEMBER"))))
				.andExpect(jsonPath("$[1].user.id", is(equalTo("1f37fb2a-b4d0-4119-9113-4677beb20ae2"))))
				.andExpect(jsonPath("$[1].user.userName", is(equalTo("tester"))));
	}

	@DisplayName("Users of community are fetched.")
	@Test
	void communityUsersAreFilteredAndFetched() throws Exception {

		final User tester = User.builder()
				.id("1f37fb2a-b4d0-4119-9113-4677beb20ae2")
				.userName("tester")
				.build();

		given(securityService.isCommunityManager("123")).willReturn(true);
		given(securityService.isCommunityMember("123")).willReturn(true);

		given(communityUserQueryService.getCommunityUsers("123", CommunityRole.MEMBER))
				.willReturn(
						Stream.of(CommunityUser.builder()
								.role(CommunityRole.MEMBER)
								.user(tester)
								.build())
				);

		mockMvc.perform(get("/communities/123/users")
				.param("role", "MEMBER")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester)))
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].role", is(equalTo("MEMBER"))))
				.andExpect(jsonPath("$[0].user.id", is(equalTo("1f37fb2a-b4d0-4119-9113-4677beb20ae2"))))
				.andExpect(jsonPath("$[0].user.userName", is(equalTo("tester"))));
	}

	@DisplayName("FORBIDDEN status code is returned when user is not authorized to fetch community users.")
	@Test
	void forbiddenStatusWhenUserIsNotAuthorizedToFetchCommunityUsers() throws Exception {

		final User tester = User.builder()
				.id("1f37fb2a-b4d0-4119-9113-4677beb20ae2")
				.userName("tester")
				.build();

		given(securityService.isCommunityManager("123")).willReturn(false);
		given(securityService.isCommunityMember("123")).willReturn(false);

		mockMvc.perform(get("/communities/123/users")
				.param("role", "MEMBER")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester)))
				.with(csrf()))
				.andExpect(status().isForbidden());
	}

	@DisplayName("Gets user suggestions for the community.")
	@Test
	void getUserSuggestionsForCommunity() throws Exception {
		final User tester = User.builder()
				.id("1f37fb2a-b4d0-4119-9113-4677beb20ae2")
				.userName("tester")
				.build();

		given(communityUserQueryService.getUsersNotRelatedToCommunity("123", "doe")).willReturn(
				Stream.of(
						User.builder()
								.id("1f37fb2a-b4d0-4119-9113-4677beb20ae2")
								.userName("firstUser")
								.firstName("John")
								.lastName("Doe The First")
								.email("firstUser@mail.com")
								.coach(false)
								.build(),
						User.builder()
								.id("2edc1229-b4d0-4119-9113-4677beb20ae2")
								.userName("secondUser")
								.firstName("John")
								.lastName("Doe The Second")
								.email("secondUser@mail.com")
								.coach(false)
								.build()
				)
		);

		mockMvc.perform(get("/communities/123/user-suggestions")
				.param("search", "doe")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester)))
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(equalTo("1f37fb2a-b4d0-4119-9113-4677beb20ae2"))))
				.andExpect(jsonPath("$[0].userName", is(equalTo("firstUser"))))
				.andExpect(jsonPath("$[0].firstName", is(equalTo("John"))))
				.andExpect(jsonPath("$[0].lastName", is(equalTo("Doe The First"))))
				.andExpect(jsonPath("$[0].email", is(equalTo("firstUser@mail.com"))))
				.andExpect(jsonPath("$[0].coach", is(equalTo(false))))
				.andExpect(jsonPath("$[1].id", is(equalTo("2edc1229-b4d0-4119-9113-4677beb20ae2"))))
				.andExpect(jsonPath("$[1].userName", is(equalTo("secondUser"))))
				.andExpect(jsonPath("$[1].firstName", is(equalTo("John"))))
				.andExpect(jsonPath("$[1].lastName", is(equalTo("Doe The Second"))))
				.andExpect(jsonPath("$[1].email", is(equalTo("secondUser@mail.com"))))
				.andExpect(jsonPath("$[1].coach", is(equalTo(false))));
	}

	@DisplayName("Not authenticated user cannot get user suggestions for the community.")
	@Test
	void notAuthenticatedUserCannotGetUserSuggestionsForCommunity() throws Exception {
		mockMvc.perform(get("/communities/123/user-suggestions")
				.param("search", "doe")
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf()))
				.andExpect(status().isUnauthorized());
	}

}