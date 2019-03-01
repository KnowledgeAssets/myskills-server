package io.knowledgeassets.myskills.server.community.command;

import io.knowledgeassets.myskills.server.community.Community;
import io.knowledgeassets.myskills.server.community.CommunityRepository;
import io.knowledgeassets.myskills.server.exception.DuplicateResourceException;
import io.knowledgeassets.myskills.server.exception.NoSuchResourceException;
import io.knowledgeassets.myskills.server.exception.enums.Model;
import io.knowledgeassets.myskills.server.security.CurrentUserService;
import io.knowledgeassets.myskills.server.skill.Skill;
import io.knowledgeassets.myskills.server.skill.command.SkillCommandService;
import io.knowledgeassets.myskills.server.user.User;
import io.knowledgeassets.myskills.server.usernotification.command.UserNotificationCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
public class CommunityCommandService {

	private final CommunityRepository communityRepository;
	private final CurrentUserService currentUserService;
	private final SkillCommandService skillCommandService;
	private final UserNotificationCommandService userNotificationCommandService;

	public CommunityCommandService(CommunityRepository communityRepository,
								   CurrentUserService currentUserService,
								   SkillCommandService skillCommandService,
								   UserNotificationCommandService userNotificationCommandService) {
		this.communityRepository = communityRepository;
		this.currentUserService = currentUserService;
		this.skillCommandService = skillCommandService;
		this.userNotificationCommandService = userNotificationCommandService;
	}

	@Transactional
	public Community create(Community community, List<User> invitedUsers) {
		communityRepository.findByTitleIgnoreCase(community.getTitle()).ifPresent(skill -> {
			throw DuplicateResourceException.builder()
					.message(format("Community with title '%s' already exists", community.getTitle()))
					.build();
		});
		final LocalDateTime now = LocalDateTime.now();
		community.setManagers(singletonList(currentUserService.getCurrentUser()));
		community.setMembers(singletonList(currentUserService.getCurrentUser()));
		community.setCreationDate(now);
		community.setLastModifiedDate(now);
		community.setId(UUID.randomUUID().toString());
		community.setSkills(createNonExistentSkills(community));
		Community c = communityRepository.save(community);
		if (!CollectionUtils.isEmpty(invitedUsers)) {
			userNotificationCommandService.inviteUsers(invitedUsers, c);
		}
		return c;
	}

	@Transactional
	public Community update(Community community, List<User> invitedUsers) {
		final Community p = communityRepository.findById(community.getId()).orElseThrow(() -> {
			final String[] searchParamsMap = {"id", community.getId()};
			return NoSuchResourceException.builder()
					.model(Model.COMMUNITY)
					.searchParamsMap(searchParamsMap)
					.build();
		});
		community.setMembers(p.getMembers());
		community.setManagers(p.getManagers());
		community.setCreationDate(p.getCreationDate());
		community.setLastModifiedDate(LocalDateTime.now());
		community.setSkills(createNonExistentSkills(community));
		final Community c = communityRepository.save(community);
		if (!CollectionUtils.isEmpty(invitedUsers)) {
			userNotificationCommandService.inviteUsers(invitedUsers, c);
		}
		return c;
	}

	@Transactional
	public void delete(String id) {
		final Community community = communityRepository.findById(id).orElseThrow(() -> {
			final String[] searchParamsMap = {"id", id};
			return NoSuchResourceException.builder()
					.model(Model.COMMUNITY)
					.searchParamsMap(searchParamsMap)
					.build();
		});
		communityRepository.delete(community);
	}

	private List<Skill> createNonExistentSkills(Community community) {
		if (community.getSkills() != null) {
			return community.getSkills().stream().map(skill -> {
				if (skill.getId() == null) {
					return skillCommandService.createSkill(skill.getName(), null, null);
				} else {
					return skill;
				}
			}).collect(toList());
		}
		else {
			return Collections.emptyList();
		}
	}

}
