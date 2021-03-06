package com.tsmms.skoop.user.notification;

import com.tsmms.skoop.common.AbstractControllerTests;
import com.tsmms.skoop.community.Community;
import com.tsmms.skoop.community.CommunityChangedNotification;
import com.tsmms.skoop.community.CommunityDeletedNotification;
import com.tsmms.skoop.community.CommunityDetails;
import com.tsmms.skoop.community.CommunityRole;
import com.tsmms.skoop.community.CommunityType;
import com.tsmms.skoop.communityuser.CommunityUserRoleChangedNotification;
import com.tsmms.skoop.communityuser.UserKickedOutFromCommunityNotification;
import com.tsmms.skoop.communityuser.UserLeftCommunityNotification;
import com.tsmms.skoop.communityuser.registration.AcceptanceToCommunityNotification;
import com.tsmms.skoop.communityuser.registration.CommunityUserRegistration;
import com.tsmms.skoop.communityuser.registration.InvitationToJoinCommunityNotification;
import com.tsmms.skoop.communityuser.registration.RequestToJoinCommunityNotification;
import com.tsmms.skoop.skill.Skill;
import com.tsmms.skoop.user.User;
import com.tsmms.skoop.notification.query.NotificationQueryService;
import com.tsmms.skoop.userproject.UserProject;
import com.tsmms.skoop.userproject.UserProjectNeedsApprovalNotification;
import com.tsmms.skoop.userskill.UserSkillsEstimationNotification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.tsmms.skoop.common.JwtAuthenticationFactory.withUser;

@WebMvcTest(UserNotificationQueryController.class)
class UserNotificationQueryControllerTests extends AbstractControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NotificationQueryService notificationQueryService;

	@DisplayName("Gets user notifications.")
	@Test
	void getUserNotifications() throws Exception {
		final User tester = User.builder()
				.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
				.userName("tester")
				.build();

		given(notificationQueryService.getUserNotifications("56ef4778-a084-4509-9a3e-80b7895cf7b0")).willReturn(Stream.of(
				InvitationToJoinCommunityNotification.builder()
						.id("123")
						.creationDatetime(LocalDateTime.of(2019, 3, 27, 9, 34))
						.registration(CommunityUserRegistration.builder()
								.approvedByUser(null)
								.approvedByCommunity(true)
								.registeredUser(User.builder()
										.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
										.userName("tester")
										.build())
								.community(Community.builder()
										.id("836713dc-a10d-4c85-8613-abe0fa0cf210")
										.type(CommunityType.CLOSED)
										.title("Community")
										.build()
								)
								.build())
						.communityName("Community")
						.build(),
				RequestToJoinCommunityNotification.builder()
						.id("456")
						.creationDatetime(LocalDateTime.of(2019, 3, 27, 10, 34))
						.registration(CommunityUserRegistration.builder()
								.approvedByUser(true)
								.approvedByCommunity(null)
								.registeredUser(User.builder()
										.id("47fd5fb1-ccf5-4163-986f-6eeb9ef37280")
										.userName("anotherTester")
										.build())
								.community(Community.builder()
										.id("7d616eaa-8a09-420a-b4f2-99b74b360308")
										.type(CommunityType.CLOSED)
										.title("AnotherCommunity")
										.build()
								)
								.build())
						.communityName("AnotherCommunity")
						.build(),
				UserKickedOutFromCommunityNotification.builder()
						.id("789")
						.creationDatetime(LocalDateTime.of(2019, 3, 25, 10, 0))
						.user(tester)
						.community(Community.builder()
								.id("773e8cce-fc06-4a62-a23e-58b52c097600")
								.title("Community the user was kicked out from")
								.type(CommunityType.CLOSED)
								.build()
						)
						.communityName("Community the user was kicked out from")
						.build(),
				UserLeftCommunityNotification.builder()
						.id("901")
						.creationDatetime(LocalDateTime.of(2019, 3, 21, 15, 30))
						.community(Community.builder()
								.id("7d616eaa-8a09-420a-b4f2-99b74b360308")
								.type(CommunityType.CLOSED)
								.title("AnotherCommunity")
								.build())
						.user(User.builder()
								.id("fddd5bdd-8931-4eb6-a875-b52e10e07d35")
								.userName("UserLeftCommunity")
								.build())
						.communityName("AnotherCommunity")
						.build(),
				CommunityDeletedNotification.builder()
						.id("902")
						.creationDatetime(LocalDateTime.of(2019, 1, 3, 10, 0))
						.communityName("Deleted community")
						.recipients(singletonList(tester))
						.build(),
				CommunityUserRoleChangedNotification.builder()
						.id("yyy")
						.role(CommunityRole.MANAGER)
						.communityName("Community the role has been changed in")
						.creationDatetime(LocalDateTime.of(2018, 4, 3, 12, 0))
						.user(tester)
						.build(),
				AcceptanceToCommunityNotification.builder()
						.communityName("Community the user was accepted in")
						.creationDatetime(LocalDateTime.of(2018, 4, 3, 12, 0))
						.id("zzz")
						.registration(CommunityUserRegistration.builder()
								.approvedByUser(true)
								.approvedByCommunity(true)
								.registeredUser(User.builder()
										.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
										.userName("tester")
										.build())
								.community(Community.builder()
										.id("999913dc-a10d-4c85-8613-abe0fa0cf210")
										.type(CommunityType.CLOSED)
										.title("Community the user was accepted in")
										.build()
								)
								.build())
						.build(),
				CommunityChangedNotification.builder()
						.id("ttt")
						.communityName("Changed community")
						.creationDatetime(LocalDateTime.of(2017, 1, 3, 10, 0))
						.recipients(singletonList(tester))
						.communityDetails(new HashSet<>(Arrays.asList(CommunityDetails.DESCRIPTION, CommunityDetails.TYPE)))
						.community(Community.builder()
								.id("123456789")
								.title("Changed community")
								.build())
						.build(),
				UserWelcomeNotification.builder()
						.id("777")
						.creationDatetime(LocalDateTime.of(2016, 5, 8, 16, 30))
						.user(tester)
						.build(),
				UserSkillsEstimationNotification.builder()
						.id("896")
						.creationDatetime(LocalDateTime.of(2015, 5, 8, 16, 30))
						.user(tester)
						.skills(new HashSet<>(Arrays.asList(
								Skill.builder()
										.id("123")
										.name("Spring Boot")
										.build(),
								Skill.builder()
										.id("456")
										.name("Angular")
										.build()
						)))
						.build(),
				UserProjectNeedsApprovalNotification.builder()
						.id("aaa")
						.creationDatetime(LocalDateTime.of(2014, 5, 8, 16, 30))
						.userProject(
								UserProject.builder()
										.id("abc")
										.user(User.builder()
												.id("123456")
												.userName("firstSubordinate")
												.build()
										)
										.build()
						)
						.build(),
				UserProjectNeedsApprovalNotification.builder()
						.id("bbb")
						.creationDatetime(LocalDateTime.of(2014, 5, 8, 16, 29))
						.userProject(
								UserProject.builder()
										.id("def")
										.user(User.builder()
												.id("654321")
												.userName("secondSubordinate")
												.build()
										)
										.build()
						)
						.build()
		));

		mockMvc.perform(get("/users/56ef4778-a084-4509-9a3e-80b7895cf7b0/notifications")
				.accept(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester))))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()", is(equalTo(12))))
				.andExpect(jsonPath("$[0].id", is(equalTo("123"))))
				.andExpect(jsonPath("$[0].type", is(equalTo("InvitationToJoinCommunityNotification"))))
				.andExpect(jsonPath("$[0].creationDatetime", is(equalTo("2019-03-27T09:34:00"))))
				.andExpect(jsonPath("$[0].registration.approvedByUser", nullValue()))
				.andExpect(jsonPath("$[0].registration.approvedByCommunity", is(equalTo(true))))
				.andExpect(jsonPath("$[0].registration.user.id", is(equalTo("56ef4778-a084-4509-9a3e-80b7895cf7b0"))))
				.andExpect(jsonPath("$[0].registration.user.userName", is(equalTo("tester"))))
				.andExpect(jsonPath("$[0].registration.community.id", is(equalTo("836713dc-a10d-4c85-8613-abe0fa0cf210"))))
				.andExpect(jsonPath("$[0].registration.community.type", is(equalTo("CLOSED"))))
				.andExpect(jsonPath("$[0].registration.community.title", is(equalTo("Community"))))
				.andExpect(jsonPath("$[0].communityName", is(equalTo("Community"))))
				.andExpect(jsonPath("$[1].id", is(equalTo("456"))))
				.andExpect(jsonPath("$[1].type", is(equalTo("RequestToJoinCommunityNotification"))))
				.andExpect(jsonPath("$[1].creationDatetime", is(equalTo("2019-03-27T10:34:00"))))
				.andExpect(jsonPath("$[1].registration.approvedByUser", is(equalTo(true))))
				.andExpect(jsonPath("$[1].registration.approvedByCommunity", nullValue()))
				.andExpect(jsonPath("$[1].registration.user.id", is(equalTo("47fd5fb1-ccf5-4163-986f-6eeb9ef37280"))))
				.andExpect(jsonPath("$[1].registration.user.userName", is(equalTo("anotherTester"))))
				.andExpect(jsonPath("$[1].registration.community.id", is(equalTo("7d616eaa-8a09-420a-b4f2-99b74b360308"))))
				.andExpect(jsonPath("$[1].registration.community.type", is(equalTo("CLOSED"))))
				.andExpect(jsonPath("$[1].registration.community.title", is(equalTo("AnotherCommunity"))))
				.andExpect(jsonPath("$[1].communityName", is(equalTo("AnotherCommunity"))))
				.andExpect(jsonPath("$[2].id", is(equalTo("789"))))
				.andExpect(jsonPath("$[2].type", is(equalTo("UserKickedOutFromCommunityNotification"))))
				.andExpect(jsonPath("$[2].creationDatetime", is(equalTo("2019-03-25T10:00:00"))))
				.andExpect(jsonPath("$[2].community.id", is(equalTo("773e8cce-fc06-4a62-a23e-58b52c097600"))))
				.andExpect(jsonPath("$[2].community.title", is(equalTo("Community the user was kicked out from"))))
				.andExpect(jsonPath("$[2].community.type", is(equalTo("CLOSED"))))
				.andExpect(jsonPath("$[2].communityName", is(equalTo("Community the user was kicked out from"))))
				.andExpect(jsonPath("$[3].id", is(equalTo("901"))))
				.andExpect(jsonPath("$[3].type", is(equalTo("UserLeftCommunityNotification"))))
				.andExpect(jsonPath("$[3].creationDatetime", is(equalTo("2019-03-21T15:30:00"))))
				.andExpect(jsonPath("$[3].user.id", is(equalTo("fddd5bdd-8931-4eb6-a875-b52e10e07d35"))))
				.andExpect(jsonPath("$[3].user.userName", is(equalTo("UserLeftCommunity"))))
				.andExpect(jsonPath("$[3].community.id", is(equalTo("7d616eaa-8a09-420a-b4f2-99b74b360308"))))
				.andExpect(jsonPath("$[3].community.title", is(equalTo("AnotherCommunity"))))
				.andExpect(jsonPath("$[3].community.type", is(equalTo("CLOSED"))))
				.andExpect(jsonPath("$[3].communityName", is(equalTo("AnotherCommunity"))))
				.andExpect(jsonPath("$[4].id", is(equalTo("902"))))
				.andExpect(jsonPath("$[4].type", is(equalTo("CommunityDeletedNotification"))))
				.andExpect(jsonPath("$[4].creationDatetime", is(equalTo("2019-01-03T10:00:00"))))
				.andExpect(jsonPath("$[4].communityName", is(equalTo("Deleted community"))))
				.andExpect(jsonPath("$[5].id", is(equalTo("yyy"))))
				.andExpect(jsonPath("$[5].type", is(equalTo("CommunityUserRoleChangedNotification"))))
				.andExpect(jsonPath("$[5].creationDatetime", is(equalTo("2018-04-03T12:00:00"))))
				.andExpect(jsonPath("$[5].communityName", is(equalTo("Community the role has been changed in"))))
				.andExpect(jsonPath("$[5].role", is(equalTo("MANAGER"))))
				.andExpect(jsonPath("$[6].id", is(equalTo("zzz"))))
				.andExpect(jsonPath("$[6].type", is(equalTo("AcceptanceToCommunityNotification"))))
				.andExpect(jsonPath("$[6].creationDatetime", is(equalTo("2018-04-03T12:00:00"))))
				.andExpect(jsonPath("$[6].communityName", is(equalTo("Community the user was accepted in"))))
				.andExpect(jsonPath("$[6].registration.approvedByUser", is(equalTo(true))))
				.andExpect(jsonPath("$[6].registration.approvedByCommunity", is(equalTo(true))))
				.andExpect(jsonPath("$[6].registration.user.id", is(equalTo("56ef4778-a084-4509-9a3e-80b7895cf7b0"))))
				.andExpect(jsonPath("$[6].registration.user.userName", is(equalTo("tester"))))
				.andExpect(jsonPath("$[6].registration.community.id", is(equalTo("999913dc-a10d-4c85-8613-abe0fa0cf210"))))
				.andExpect(jsonPath("$[6].registration.community.type", is(equalTo("CLOSED"))))
				.andExpect(jsonPath("$[6].registration.community.title", is(equalTo("Community the user was accepted in"))))
				.andExpect(jsonPath("$[7].id", is(equalTo("ttt"))))
				.andExpect(jsonPath("$[7].type", is(equalTo("CommunityChangedNotification"))))
				.andExpect(jsonPath("$[7].creationDatetime", is(equalTo("2017-01-03T10:00:00"))))
				.andExpect(jsonPath("$[7].communityName", is(equalTo("Changed community"))))
				.andExpect(jsonPath("$[7].communityDetails", containsInAnyOrder(CommunityDetails.DESCRIPTION.toString(), CommunityDetails.TYPE.toString())))
				.andExpect(jsonPath("$[7].community.id", is(equalTo("123456789"))))
				.andExpect(jsonPath("$[7].community.title", is(equalTo("Changed community"))))
				.andExpect(jsonPath("$[8].id", is(equalTo("777"))))
				.andExpect(jsonPath("$[8].type", is(equalTo("UserWelcomeNotification"))))
				.andExpect(jsonPath("$[8].creationDatetime", is(equalTo("2016-05-08T16:30:00"))))
				.andExpect(jsonPath("$[9].id", is(equalTo("896"))))
				.andExpect(jsonPath("$[9].type", is(equalTo("UserSkillsEstimationNotification"))))
				.andExpect(jsonPath("$[9].creationDatetime", is(equalTo("2015-05-08T16:30:00"))))
				.andExpect(jsonPath("$[9].skills[0].id", is(equalTo("123"))))
				.andExpect(jsonPath("$[9].skills[0].name", is(equalTo("Spring Boot"))))
				.andExpect(jsonPath("$[9].skills[1].id", is(equalTo("456"))))
				.andExpect(jsonPath("$[9].skills[1].name", is(equalTo("Angular"))))
				.andExpect(jsonPath("$[10].id", is(equalTo("aaa"))))
				.andExpect(jsonPath("$[10].type", is(equalTo("UserProjectNeedsApprovalNotification"))))
				.andExpect(jsonPath("$[10].creationDatetime", is(equalTo("2014-05-08T16:30:00"))))
				.andExpect(jsonPath("$[10].userProject.user.id", is(equalTo(("123456")))))
				.andExpect(jsonPath("$[10].userProject.user.userName", is(equalTo(("firstSubordinate")))))
				.andExpect(jsonPath("$[11].id", is(equalTo("bbb"))))
				.andExpect(jsonPath("$[11].type", is(equalTo("UserProjectNeedsApprovalNotification"))))
				.andExpect(jsonPath("$[11].creationDatetime", is(equalTo("2014-05-08T16:29:00"))))
				.andExpect(jsonPath("$[11].userProject.user.id", is(equalTo(("654321")))))
				.andExpect(jsonPath("$[11].userProject.user.userName", is(equalTo(("secondSubordinate")))));
	}

	@DisplayName("Not authenticated user cannot get notifications.")
	@Test
	void notAuthenticatedUserCannotGetNotifications() throws Exception {
		mockMvc.perform(get("/users/56ef4778-a084-4509-9a3e-80b7895cf7b0/notifications")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@DisplayName("A user cannot get notifications of another user.")
	@Test
	void userCannotGetNotificationsOfAnotherUser() throws Exception {
		final User tester = User.builder()
				.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
				.userName("tester")
				.build();
		mockMvc.perform(get("/users/47fd5fb1-ccf5-4163-986f-6eeb9ef37280/notifications")
				.accept(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester))))
				.andExpect(status().isForbidden());
	}

	@DisplayName("Gets user notification counter.")
	@Test
	void getUserNotificationCounter() throws Exception {
		final User tester = User.builder()
				.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
				.userName("tester")
				.build();

		given(notificationQueryService.getUserNotificationCounter(tester.getId()))
				.willReturn(12);

		mockMvc.perform(get("/users/56ef4778-a084-4509-9a3e-80b7895cf7b0/notification-counter")
				.accept(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester))))
				.andExpect(status().isOk());
	}

	@DisplayName("Not authenticated user cannot get user notification counter.")
	@Test
	void notAuthenticatedUserCannotGetUserNotificationCounter() throws Exception {
		mockMvc.perform(get("/users/56ef4778-a084-4509-9a3e-80b7895cf7b0/notification-counter")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@DisplayName("A user cannot get user notification counter of another user.")
	@Test
	void userCannotGetUserNotificationCounterOfAnotherUser() throws Exception {
		final User tester = User.builder()
				.id("56ef4778-a084-4509-9a3e-80b7895cf7b0")
				.userName("tester")
				.build();
		mockMvc.perform(get("/users/47fd5fb1-ccf5-4163-986f-6eeb9ef37280/notification-counter")
				.accept(MediaType.APPLICATION_JSON)
				.with(authentication(withUser(tester))))
				.andExpect(status().isForbidden());
	}

}
