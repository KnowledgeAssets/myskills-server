package io.knowledgeassets.myskills.server.notification;

import io.knowledgeassets.myskills.server.community.Community;
import io.knowledgeassets.myskills.server.community.CommunityRepository;
import io.knowledgeassets.myskills.server.community.CommunityRole;
import io.knowledgeassets.myskills.server.community.CommunityType;
import io.knowledgeassets.myskills.server.communityuser.CommunityUser;
import io.knowledgeassets.myskills.server.communityuser.CommunityUserRepository;
import io.knowledgeassets.myskills.server.communityuser.UserKickedOutFromCommunityNotification;
import io.knowledgeassets.myskills.server.communityuser.UserLeftCommunityNotification;
import io.knowledgeassets.myskills.server.communityuser.registration.CommunityUserRegistration;
import io.knowledgeassets.myskills.server.communityuser.registration.CommunityUserRegistrationRepository;
import io.knowledgeassets.myskills.server.communityuser.registration.InvitationToJoinCommunityNotification;
import io.knowledgeassets.myskills.server.communityuser.registration.RequestToJoinCommunityNotification;
import io.knowledgeassets.myskills.server.user.User;
import io.knowledgeassets.myskills.server.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.stream.Collectors.toList;

@DataNeo4jTest
class NotificationRepositoryTests {

	@Autowired
	private CommunityRepository communityRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CommunityUserRegistrationRepository communityUserRegistrationRepository;

	@Autowired
	private CommunityUserRepository communityUserRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@DisplayName("Get notifications sent to the user and to the communities she / he is the manager of.")
	@Test
	void getsUserNotifications() {

		User communityManager = userRepository.save(User.builder()
				.id("123")
				.userName("communityManager")
				.build());

		User commonUser = userRepository.save(User.builder()
				.id("456")
				.userName("commonUser")
				.build());

		Community javascriptUserGroup = communityRepository.save(Community.builder()
				.id("567")
				.title("JavaScript User Group")
				.type(CommunityType.CLOSED)
				.build()
		);

		Community frontendDevelopers = communityRepository.save(Community.builder()
				.id("567123")
				.title("Frontend developers")
				.type(CommunityType.CLOSED)
				.build()
		);

		// communityManager is a community manager of "JavaScript User Group"

		communityUserRepository.save(CommunityUser.builder()
				.creationDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.lastModifiedDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.community(javascriptUserGroup)
				.user(communityManager)
				.role(CommunityRole.MANAGER)
				.build()
		);

		// communityManager is a member of "JavaScript User Group"

		communityUserRepository.save(CommunityUser.builder()
				.creationDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.lastModifiedDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.community(frontendDevelopers)
				.user(communityManager)
				.role(CommunityRole.MEMBER)
				.build()
		);

		// communityManager was invited to join "Java User Group"

		CommunityUserRegistration firstRegistration = communityUserRegistrationRepository.save(CommunityUserRegistration.builder()
				.id("654321")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 10, 0))
				.approvedByCommunity(true)
				.approvedByUser(null)
				.registeredUser(communityManager)
				.community(Community.builder()
						.id("987")
						.title("Java User Group")
						.type(CommunityType.CLOSED)
						.build()
				)
				.build()
		);

		// commonUser sent request to join "JavaScript User Group"

		CommunityUserRegistration secondRegistration = communityUserRegistrationRepository.save(CommunityUserRegistration.builder()
				.id("123456")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 11, 0))
				.approvedByCommunity(null)
				.approvedByUser(true)
				.registeredUser(commonUser)
				.community(javascriptUserGroup)
				.build()
		);

		// commonUser sent request to join "Frontend developers"

		CommunityUserRegistration thirdRegistration = communityUserRegistrationRepository.save(CommunityUserRegistration.builder()
				.id("123456789")
				.creationDatetime(LocalDateTime.of(2019, 3, 27, 11, 0))
				.approvedByCommunity(null)
				.approvedByUser(true)
				.registeredUser(commonUser)
				.community(frontendDevelopers)
				.build()
		);

		// notification that communityManager was invited to join "Java User Group". It will be shown to communityManager.

		notificationRepository.save(InvitationToJoinCommunityNotification.builder()
				.id("abc")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 10, 0))
				.registration(firstRegistration)
				.build());

		// notification that commonUser sent request to join "JavaScript User Group". It will be shown to communityManager.

		notificationRepository.save(RequestToJoinCommunityNotification.builder()
				.id("def")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 11, 0))
				.registration(secondRegistration)
				.build()
		);

		// notification that commonUser sent request to join "Frontend developers". It will not be shown to communityManager.

		notificationRepository.save(RequestToJoinCommunityNotification.builder()
				.id("ghi")
				.creationDatetime(LocalDateTime.of(2019, 3, 27, 11, 0))
				.registration(thirdRegistration)
				.build()
		);

		notificationRepository.save(UserLeftCommunityNotification.builder()
				.id("zyx")
				.creationDatetime(LocalDateTime.of(2019, 3, 21, 15, 30))
				.community(javascriptUserGroup)
				.user(User.builder()
						.id("0123")
						.userName("UserLeftCommunity")
						.build())
				.build()
		);

		final List<Notification> notifications = notificationRepository.getUserNotifications("123").collect(toList());

		assertThat(notifications).hasSize(3);
		Notification notification = notifications.get(0);
		assertThat(notification.getId()).isEqualTo("def");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 11, 0));
		assertThat(notification).isInstanceOf(RequestToJoinCommunityNotification.class);
		RequestToJoinCommunityNotification requestToJoinCommunityNotification = (RequestToJoinCommunityNotification) notification;
		assertThat(requestToJoinCommunityNotification.getRegistration()).isNotNull();
		assertThat(requestToJoinCommunityNotification.getRegistration().getApprovedByUser()).isTrue();
		assertThat(requestToJoinCommunityNotification.getRegistration().getApprovedByCommunity()).isNull();
		assertThat(requestToJoinCommunityNotification.getRegistration().getCommunity()).isEqualTo(javascriptUserGroup);
		assertThat(requestToJoinCommunityNotification.getRegistration().getRegisteredUser()).isEqualTo(commonUser);
		assertThat(requestToJoinCommunityNotification.getRegistration().getId()).isEqualTo("123456");
		assertThat(requestToJoinCommunityNotification.getRegistration().getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 11, 0));

		notification = notifications.get(1);
		assertThat(notification.getId()).isEqualTo("abc");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 10, 0));
		assertThat(notification).isInstanceOf(InvitationToJoinCommunityNotification.class);
		InvitationToJoinCommunityNotification invitationToJoinCommunityNotification = (InvitationToJoinCommunityNotification) notification;
		assertThat(invitationToJoinCommunityNotification.getRegistration()).isNotNull();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getApprovedByUser()).isNull();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getApprovedByCommunity()).isTrue();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getCommunity()).isEqualTo(Community.builder()
				.id("987")
				.title("Java User Group")
				.type(CommunityType.CLOSED)
				.build());
		assertThat(invitationToJoinCommunityNotification.getRegistration().getRegisteredUser()).isEqualTo(communityManager);
		assertThat(invitationToJoinCommunityNotification.getRegistration().getId()).isEqualTo("654321");
		assertThat(invitationToJoinCommunityNotification.getRegistration().getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 10, 0));

		notification = notifications.get(2);
		assertThat(notification.getId()).isEqualTo("zyx");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 21, 15, 30));
		assertThat(notification).isInstanceOf(UserLeftCommunityNotification.class);
		final UserLeftCommunityNotification userLeftCommunityNotification = (UserLeftCommunityNotification) notification;
		assertThat(userLeftCommunityNotification.getCommunity()).isEqualTo(javascriptUserGroup);
		assertThat(userLeftCommunityNotification.getUser()).isEqualTo(User.builder()
				.id("0123")
				.userName("UserLeftCommunity")
				.build());
	}

	@DisplayName("Get notifications sent to the user.")
	@Test
	void getsNotificationsSentToUser() {

		User commonUser = userRepository.save(User.builder()
				.id("123")
				.userName("commonUser")
				.build());

		// commonUser was invited to join "Java User Group"

		CommunityUserRegistration firstRegistration = communityUserRegistrationRepository.save(CommunityUserRegistration.builder()
				.id("654321")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 10, 0))
				.approvedByCommunity(true)
				.approvedByUser(null)
				.registeredUser(commonUser)
				.community(Community.builder()
						.id("987")
						.title("Java User Group")
						.type(CommunityType.CLOSED)
						.build()
				)
				.build()
		);

		// notification that commonUser was invited to join "Java User Group". It will be shown to commonUser.

		notificationRepository.save(InvitationToJoinCommunityNotification.builder()
				.id("abc")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 10, 0))
				.registration(firstRegistration)
				.build());

		notificationRepository.save(UserKickedOutFromCommunityNotification.builder()
				.id("def")
				.creationDatetime(LocalDateTime.of(2019, 3, 25, 10, 0))
				.user(commonUser)
				.community(Community.builder()
						.id("145")
						.title("JavaScript User Group")
						.type(CommunityType.CLOSED)
						.build()
				)
				.build()
		);

		final List<Notification> notifications = notificationRepository.getUserNotifications("123").collect(toList());

		assertThat(notifications).hasSize(2);

		Notification notification = notifications.get(0);
		assertThat(notification.getId()).isEqualTo("abc");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 10, 0));
		assertThat(notification).isInstanceOf(InvitationToJoinCommunityNotification.class);
		InvitationToJoinCommunityNotification invitationToJoinCommunityNotification = (InvitationToJoinCommunityNotification) notification;
		assertThat(invitationToJoinCommunityNotification.getRegistration()).isNotNull();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getApprovedByUser()).isNull();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getApprovedByCommunity()).isTrue();
		assertThat(invitationToJoinCommunityNotification.getRegistration().getCommunity()).isEqualTo(Community.builder()
				.id("987")
				.title("Java User Group")
				.type(CommunityType.CLOSED)
				.build());
		assertThat(invitationToJoinCommunityNotification.getRegistration().getRegisteredUser()).isEqualTo(commonUser);
		assertThat(invitationToJoinCommunityNotification.getRegistration().getId()).isEqualTo("654321");
		assertThat(invitationToJoinCommunityNotification.getRegistration().getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 10, 0));

		notification = notifications.get(1);
		assertThat(notification.getId()).isEqualTo("def");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 25, 10, 0));
		assertThat(notification).isInstanceOf(UserKickedOutFromCommunityNotification.class);
		final UserKickedOutFromCommunityNotification userKickedOutFromCommunityNotification = (UserKickedOutFromCommunityNotification) notification;
		assertThat(userKickedOutFromCommunityNotification.getUser()).isEqualTo(commonUser);
		assertThat(userKickedOutFromCommunityNotification.getCommunity()).isEqualTo(Community.builder()
				.id("145")
				.title("JavaScript User Group")
				.type(CommunityType.CLOSED)
				.build());
	}

	@DisplayName("Get notifications sent to the communities the user is the manager of.")
	@Test
	void getsNotificationsSentToCommunitiesUserIsManagerOf() {

		User communityManager = userRepository.save(User.builder()
				.id("123")
				.userName("communityManager")
				.build());

		User commonUser = userRepository.save(User.builder()
				.id("456")
				.userName("commonUser")
				.build());

		Community javascriptUserGroup = communityRepository.save(Community.builder()
				.id("567")
				.title("JavaScript User Group")
				.type(CommunityType.CLOSED)
				.build()
		);

		// communityManager is a community manager of "JavaScript User Group"

		communityUserRepository.save(CommunityUser.builder()
				.creationDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.lastModifiedDate(LocalDateTime.of(2019, 1, 20, 10, 0))
				.community(javascriptUserGroup)
				.user(communityManager)
				.role(CommunityRole.MANAGER)
				.build()
		);

		// commonUser sent request to join "JavaScript User Group"

		CommunityUserRegistration firstRegistration = communityUserRegistrationRepository.save(CommunityUserRegistration.builder()
				.id("123456")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 11, 0))
				.approvedByCommunity(null)
				.approvedByUser(true)
				.registeredUser(commonUser)
				.community(javascriptUserGroup)
				.build()
		);

		// notification that commonUser sent request to join "JavaScript User Group".

		notificationRepository.save(RequestToJoinCommunityNotification.builder()
				.id("def")
				.creationDatetime(LocalDateTime.of(2019, 3, 26, 11, 0))
				.registration(firstRegistration)
				.build()
		);

		final List<Notification> notifications = notificationRepository.getUserNotifications("123").collect(toList());

		assertThat(notifications).hasSize(1);
		Notification notification = notifications.get(0);
		assertThat(notification.getId()).isEqualTo("def");
		assertThat(notification.getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 11, 0));
		assertThat(notification).isInstanceOf(RequestToJoinCommunityNotification.class);
		RequestToJoinCommunityNotification requestToJoinCommunityNotification = (RequestToJoinCommunityNotification) notification;
		assertThat(requestToJoinCommunityNotification.getRegistration()).isNotNull();
		assertThat(requestToJoinCommunityNotification.getRegistration().getApprovedByUser()).isTrue();
		assertThat(requestToJoinCommunityNotification.getRegistration().getApprovedByCommunity()).isNull();
		assertThat(requestToJoinCommunityNotification.getRegistration().getCommunity()).isEqualTo(javascriptUserGroup);
		assertThat(requestToJoinCommunityNotification.getRegistration().getRegisteredUser()).isEqualTo(commonUser);
		assertThat(requestToJoinCommunityNotification.getRegistration().getId()).isEqualTo("123456");
		assertThat(requestToJoinCommunityNotification.getRegistration().getCreationDatetime()).isEqualTo(LocalDateTime.of(2019, 3, 26, 11, 0));
	}

}
