package com.tsmms.skoop.communityuser.registration.command;

import com.tsmms.skoop.community.Community;
import com.tsmms.skoop.community.CommunityRole;
import com.tsmms.skoop.notification.Notification;
import com.tsmms.skoop.notification.query.NotificationQueryService;
import com.tsmms.skoop.user.User;
import com.tsmms.skoop.communityuser.command.CommunityUserCommandService;
import com.tsmms.skoop.communityuser.registration.AcceptanceToCommunityNotification;
import com.tsmms.skoop.communityuser.registration.InvitationToJoinCommunityNotification;
import com.tsmms.skoop.communityuser.registration.RequestToJoinCommunityNotification;
import com.tsmms.skoop.notification.command.NotificationCommandService;
import com.tsmms.skoop.communityuser.registration.CommunityUserRegistration;
import com.tsmms.skoop.communityuser.registration.CommunityUserRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

@Service
public class CommunityUserRegistrationCommandService {

	private final CommunityUserRegistrationRepository communityUserRegistrationRepository;
	private final CommunityUserCommandService communityUserCommandService;
	private final NotificationCommandService notificationCommandService;
	private final NotificationQueryService notificationQueryService;

	public CommunityUserRegistrationCommandService(CommunityUserRegistrationRepository communityUserRegistrationRepository,
												   CommunityUserCommandService communityUserCommandService,
												   NotificationCommandService notificationCommandService,
												   NotificationQueryService notificationQueryService) {
		this.communityUserRegistrationRepository = requireNonNull(communityUserRegistrationRepository);
		this.communityUserCommandService = requireNonNull(communityUserCommandService);
		this.notificationCommandService = requireNonNull(notificationCommandService);
		this.notificationQueryService = requireNonNull(notificationQueryService);
	}

	@Transactional
	public CommunityUserRegistration approve(CommunityUserRegistration registration, CommunityUserRegistrationApprovalCommand command) {
		if (command.getApprovedByUser() != null && command.getApprovedByCommunity() != null) {
			throw new IllegalArgumentException("No one can approve / decline both as community and user at the same time.");
		}
		if (command.getApprovedByUser() == null && command.getApprovedByCommunity() == null) {
			throw new IllegalArgumentException("The command to approve user registration will not affect user registration as it have both flags set to null.");
		}
		final boolean isAcceptanceToCommunity = registration.getApprovedByCommunity() == null && Boolean.TRUE.equals(command.getApprovedByCommunity());
		if (command.getApprovedByCommunity() != null) {
			registration.setApprovedByCommunity(command.getApprovedByCommunity());
		}
		if (command.getApprovedByUser() != null) {
			registration.setApprovedByUser(command.getApprovedByUser());
		}
		if (Boolean.FALSE.equals(registration.getApprovedByUser()) || Boolean.FALSE.equals(registration.getApprovedByCommunity())) {
			deleteToDosConnectedWithCommunityUserRegistration(registration);
			communityUserRegistrationRepository.delete(registration);
			return registration;
		}
		final CommunityUserRegistration communityUserRegistration = communityUserRegistrationRepository.save(registration);
		if (Boolean.TRUE.equals(communityUserRegistration.getApprovedByCommunity()) &&
				Boolean.TRUE.equals(communityUserRegistration.getApprovedByUser())) {
			communityUserRegistration.setCommunityUser(communityUserCommandService.create(registration.getCommunity(), registration.getRegisteredUser(), CommunityRole.MEMBER));
		}
		if (Boolean.TRUE.equals(communityUserRegistration.getApprovedByCommunity()) &&
				Boolean.TRUE.equals(communityUserRegistration.getApprovedByUser())) {
			deleteToDosConnectedWithCommunityUserRegistration(communityUserRegistration);
			if (isAcceptanceToCommunity) {
				notificationCommandService.save(AcceptanceToCommunityNotification.builder()
						.id(UUID.randomUUID().toString())
						.creationDatetime(LocalDateTime.now())
						.registration(communityUserRegistration)
						.communityName(communityUserRegistration.getCommunity().getTitle())
						.build()
				);
			} else {
				communityUserRegistrationRepository.delete(communityUserRegistration);
			}
		}
		return communityUserRegistration;
	}

	private void deleteToDosConnectedWithCommunityUserRegistration(CommunityUserRegistration communityUserRegistration) {
		final List<Notification> notifications = notificationQueryService.getNotificationsByCommunityUserRegistrationId(communityUserRegistration.getId())
				.collect(Collectors.toList());
		notificationCommandService.deleteAll(notifications);
	}

	/**
	 * Invite users to join a community.
	 * @param users - invited users
	 * @param community - community to join
	 * @return user invitations
	 */
	@Transactional
	public List<CommunityUserRegistration> createUserRegistrationsOnBehalfOfCommunity(List<User> users, Community community) {
		if (CollectionUtils.isEmpty(users)) {
			throw new IllegalArgumentException("There are no users no invite.");
		}
		final LocalDateTime now = LocalDateTime.now();
		final List<CommunityUserRegistration> communityUserRegistrations = users.stream().map(user -> CommunityUserRegistration.builder()
				.id(UUID.randomUUID().toString())
				.approvedByCommunity(true)
				.approvedByUser(null)
				.registeredUser(user)
				.community(community)
				.creationDatetime(now)
				.build()
		).collect(Collectors.toList());
		List<CommunityUserRegistration> registrations = StreamSupport.stream(communityUserRegistrationRepository.saveAll(communityUserRegistrations).spliterator(), false)
				.collect(Collectors.toList());
		registrations.stream().map(registration -> InvitationToJoinCommunityNotification.builder()
				.id(UUID.randomUUID().toString())
				.registration(registration)
				.creationDatetime(LocalDateTime.now())
				.communityName(registration.getCommunity().getTitle())
				.build()
		).forEach(notificationCommandService::save);
		return registrations;
	}

	/**
	 * Sends a request on behalf of a user to join a community.
	 * @param user - user to send a request on behalf of
	 * @param community - community to join
	 * @return user notification
	 */
	@Transactional
	public CommunityUserRegistration createUserRegistrationsOnBehalfOfUser(User user, Community community) {
		if (user == null) {
			throw new IllegalArgumentException("User cannot be null.");
		}
		if (community == null) {
			throw new IllegalArgumentException("Community cannot be null.");
		}
		CommunityUserRegistration communityUserRegistration = CommunityUserRegistration.builder()
				.community(community)
				.approvedByUser(true)
				.approvedByCommunity(null)
				.registeredUser(user)
				.id(UUID.randomUUID().toString())
				.creationDatetime(LocalDateTime.now())
				.build();
		final CommunityUserRegistration registration = communityUserRegistrationRepository.save(communityUserRegistration);
		notificationCommandService.save(RequestToJoinCommunityNotification.builder()
				.id(UUID.randomUUID().toString())
				.registration(registration)
				.communityName(registration.getCommunity().getTitle())
				.creationDatetime(LocalDateTime.now())
				.build()
		);
		return registration;
	}

	@Transactional
	public void deleteAll(Collection<CommunityUserRegistration> communityUserRegistrations) {
		if (communityUserRegistrations == null) {
			throw new IllegalArgumentException("The collection with community user registrations cannot be null.");
		}
		if (!communityUserRegistrations.isEmpty()) {
			communityUserRegistrationRepository.deleteAll(communityUserRegistrations);
		}
	}

	@Transactional
	public void delete(CommunityUserRegistration communityUserRegistration) {
		if (communityUserRegistration == null) {
			throw new IllegalArgumentException("Community user registration cannot be null.");
		}
		communityUserRegistrationRepository.delete(communityUserRegistration);
	}

}
