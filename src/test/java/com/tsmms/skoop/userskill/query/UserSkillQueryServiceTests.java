package com.tsmms.skoop.userskill.query;

import com.tsmms.skoop.skill.Skill;
import com.tsmms.skoop.skill.query.SkillQueryService;
import com.tsmms.skoop.user.User;
import com.tsmms.skoop.user.query.UserQueryService;
import com.tsmms.skoop.userskill.UserSkill;
import com.tsmms.skoop.userskill.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserSkillQueryServiceTests {
	@Mock
	private UserSkillRepository userSkillRepository;
	@Mock
	private UserQueryService userQueryService;
	@Mock
	private SkillQueryService skillQueryService;

	private UserSkillQueryService userSkillQueryService;

	@BeforeEach
	void prepareTest() {
		userSkillQueryService = new UserSkillQueryService(userSkillRepository, userQueryService, skillQueryService);
	}

	@Test
	@DisplayName("Provides the skills related to the user given by user ID")
	void providesSkillsRelatedToGivenUserId() {
		User tester = User.builder()
				.id("123")
				.userName("tester")
				.build();
		UserSkill testerAngular = UserSkill.builder()
				.id(1L)
				.user(tester)
				.skill(Skill.builder()
						.id("ABC")
						.name("Angular")
						.description("JavaScript Framework")
						.build())
				.currentLevel(1)
				.desiredLevel(2)
				.priority(3)
				.build();
		UserSkill testerSpringBoot = UserSkill.builder()
				.id(2L)
				.user(tester)
				.skill(Skill.builder()
						.id("DEF")
						.name("Spring Boot")
						.description("Java Application Framework")
						.build())
				.currentLevel(2)
				.desiredLevel(3)
				.priority(4)
				.build();
		given(userSkillRepository.findByUserId("123")).willReturn(asList(testerAngular, testerSpringBoot));

		Stream<UserSkill> userSkills = userSkillQueryService.getUserSkillsByUserId("123");

		assertThat(userSkills).containsExactly(testerAngular, testerSpringBoot);
	}

	@Test
	@DisplayName("Provides the existing relationship for the given user ID and skill ID")
	void providesRelationshipForGivenUserIdAndSkillId() {
		UserSkill testerAngular = UserSkill.builder()
				.id(1L)
				.user(User.builder()
						.id("123")
						.userName("tester")
						.build())
				.skill(Skill.builder()
						.id("ABC")
						.name("Angular")
						.description("JavaScript Framework")
						.build())
				.currentLevel(1)
				.desiredLevel(2)
				.priority(3)
				.build();
		given(userSkillRepository.findByUserIdAndSkillId("123", "ABC")).willReturn(Optional.of(testerAngular));

		Optional<UserSkill> userSkill = userSkillQueryService.getUserSkillByUserIdAndSkillId("123", "ABC");

		assertThat(userSkill).isNotNull();
		assertThat(userSkill).contains(testerAngular);
	}
}
