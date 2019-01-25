package io.knowledgeassets.myskills.server.user.profile;

import io.knowledgeassets.myskills.server.exception.UserProfileDocumentException;
import io.knowledgeassets.myskills.server.skill.Skill;
import io.knowledgeassets.myskills.server.user.User;
import io.knowledgeassets.myskills.server.user.UserRepository;
import io.knowledgeassets.myskills.server.userskill.UserSkill;
import io.knowledgeassets.myskills.server.userskill.UserSkillRepository;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserSkillRepository userSkillRepository;

	private UserProfileService userProfileService;

	@Test
	@DisplayName("Tests if user profile document is built")
	void testIfUserProfileDocumentIsBuilt() throws IOException {

		this.userProfileService = new UserProfileService(userRepository, userSkillRepository, null);

		User user = User.builder()
				.id("adac977c-8e0d-4e00-98a8-da7b44aa5dd6")
				.referenceId("5acc24df-792a-4458-8d01-0c67033eceff")
				.userName("johndoe")
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@mail.com")
				.coach(false)
				.academicDegree("Diplom-Wirtschaftsinformatiker")
				.positionProfile("Software Developer")
				.summary("Developer")
				.industrySectors(Arrays.asList("Automotive", "Telecommunication"))
				.specializations(Arrays.asList("IT Consulting", "Software Integration"))
				.certificates(Collections.singletonList("Java Certified Programmer"))
				.languages(Collections.singletonList("Deutsch"))
				.build();

		given(userRepository.findByReferenceId("5acc24df-792a-4458-8d01-0c67033eceff")).willReturn(
				Optional.of(user)
		);

		given(userSkillRepository.findByUserIdOrderByCurrentLevelDesc("adac977c-8e0d-4e00-98a8-da7b44aa5dd6"))
				.willReturn(Arrays.asList(
						UserSkill.builder()
								.id(1L)
								.user(user)
								.skill(Skill.builder()
										.id("e441613b-319f-4698-917d-6a4037c8e330")
										.name("Angular")
										.description("JavaScript Framework")
										.build())
								.currentLevel(2)
								.desiredLevel(3)
								.priority(4)
								.build(),
						UserSkill.builder()
								.id(2L)
								.user(user)
								.skill(Skill.builder()
										.id("3d4236c9-d84a-420a-baee-27b263118a28")
										.name("Spring Boot")
										.description("Java Framework")
										.build())
								.currentLevel(1)
								.desiredLevel(2)
								.priority(3)
								.build()
				));

		try (InputStream is = userProfileService.getAnonymousUserProfileDocument("5acc24df-792a-4458-8d01-0c67033eceff")) {
			assertThat(is).isNotNull();
			XWPFDocument document = new XWPFDocument(is);
			assertThat(document).isNotNull();
		}
	}

	@Test
	@DisplayName("Tests if user profile document is built when the optional fields are nulls.")
	void testIfUserProfileDocumentIsBuiltWhenOptionalFieldsAreNulls() throws IOException {

		this.userProfileService = new UserProfileService(userRepository, userSkillRepository, null);

		User user = User.builder()
				.id("adac977c-8e0d-4e00-98a8-da7b44aa5dd6")
				.referenceId("5acc24df-792a-4458-8d01-0c67033eceff")
				.userName("johndoe")
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@mail.com")
				.coach(false)
				.build();

		given(userRepository.findByReferenceId("5acc24df-792a-4458-8d01-0c67033eceff")).willReturn(
				Optional.of(user)
		);

		given(userSkillRepository.findByUserIdOrderByCurrentLevelDesc("adac977c-8e0d-4e00-98a8-da7b44aa5dd6"))
				.willReturn(Arrays.asList(
						UserSkill.builder()
								.id(1L)
								.user(user)
								.skill(Skill.builder()
										.id("e441613b-319f-4698-917d-6a4037c8e330")
										.name("Angular")
										.description("JavaScript Framework")
										.build())
								.currentLevel(2)
								.desiredLevel(3)
								.priority(4)
								.build(),
						UserSkill.builder()
								.id(2L)
								.user(user)
								.skill(Skill.builder()
										.id("3d4236c9-d84a-420a-baee-27b263118a28")
										.name("Spring Boot")
										.description("Java Framework")
										.build())
								.currentLevel(1)
								.desiredLevel(2)
								.priority(3)
								.build()
				));

		try (InputStream is = userProfileService.getAnonymousUserProfileDocument("5acc24df-792a-4458-8d01-0c67033eceff")) {
			assertThat(is).isNotNull();
			XWPFDocument document = new XWPFDocument(is);
			assertThat(document).isNotNull();
		}
	}

	@Test
	@DisplayName("Tests if user profile document is built falling back to the default template when a wrong path to a template is set.")
	void testIfUserProfileDocumentIsBuiltFallingBackToDefaultTemplateWhenWrongPathToTemplateIsSet() throws IOException {
		this.userProfileService = new UserProfileService(userRepository, userSkillRepository, "some/not/existing/path");

		User user = User.builder()
				.id("adac977c-8e0d-4e00-98a8-da7b44aa5dd6")
				.referenceId("5acc24df-792a-4458-8d01-0c67033eceff")
				.userName("johndoe")
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@mail.com")
				.coach(false)
				.build();

		given(userRepository.findByReferenceId("5acc24df-792a-4458-8d01-0c67033eceff")).willReturn(
				Optional.of(user)
		);

		given(userSkillRepository.findByUserIdOrderByCurrentLevelDesc("adac977c-8e0d-4e00-98a8-da7b44aa5dd6"))
				.willReturn(Arrays.asList(
						UserSkill.builder()
								.id(1L)
								.user(user)
								.skill(Skill.builder()
										.id("e441613b-319f-4698-917d-6a4037c8e330")
										.name("Angular")
										.description("JavaScript Framework")
										.build())
								.currentLevel(2)
								.desiredLevel(3)
								.priority(4)
								.build(),
						UserSkill.builder()
								.id(2L)
								.user(user)
								.skill(Skill.builder()
										.id("3d4236c9-d84a-420a-baee-27b263118a28")
										.name("Spring Boot")
										.description("Java Framework")
										.build())
								.currentLevel(1)
								.desiredLevel(2)
								.priority(3)
								.build()
				));

		try (InputStream is = userProfileService.getAnonymousUserProfileDocument("5acc24df-792a-4458-8d01-0c67033eceff")) {
			assertThat(is).isNotNull();
			XWPFDocument document = new XWPFDocument(is);
			assertThat(document).isNotNull();
		}
	}

	@Test
	@DisplayName("Tests if an exception is thrown when invalid template is used.")
	void testIfExceptionIsThrownWhenInvalidTemplateIsUsed() throws IOException {
		this.userProfileService = new UserProfileService(userRepository, userSkillRepository, new ClassPathResource("fake-template.docx").getFile().getAbsolutePath());

		User user = User.builder()
				.id("adac977c-8e0d-4e00-98a8-da7b44aa5dd6")
				.referenceId("5acc24df-792a-4458-8d01-0c67033eceff")
				.userName("johndoe")
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@mail.com")
				.coach(false)
				.build();

		given(userRepository.findByReferenceId("5acc24df-792a-4458-8d01-0c67033eceff")).willReturn(
				Optional.of(user)
		);

		assertThrows(UserProfileDocumentException.class, () -> userProfileService.getAnonymousUserProfileDocument("5acc24df-792a-4458-8d01-0c67033eceff"));
	}

}
