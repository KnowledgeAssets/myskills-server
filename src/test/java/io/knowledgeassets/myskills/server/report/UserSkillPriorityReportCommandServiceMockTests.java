package io.knowledgeassets.myskills.server.report;

import io.knowledgeassets.myskills.server.common.Neo4jSessionFactoryConfiguration;
import io.knowledgeassets.myskills.server.report.userskillpriorityaggregationreport.UserSkillPriorityAggregationReport;
import io.knowledgeassets.myskills.server.report.userskillpriorityreport.UserSkillPriorityReport;
import io.knowledgeassets.myskills.server.report.userskillpriorityreport.command.UserSkillPriorityReportCommandController;
import io.knowledgeassets.myskills.server.report.userskillpriorityreport.command.UserSkillPriorityReportCommandService;
import io.knowledgeassets.myskills.server.report.userskillreport.UserSkillReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserSkillPriorityReportCommandController.class)
// Additional configuration is required to workaround missing SessionFactory issue!
@Import(Neo4jSessionFactoryConfiguration.class)
public class UserSkillPriorityReportCommandServiceMockTests {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private UserSkillPriorityReportCommandService userSkillPriorityReportCommandService;

	@Test
	public void createReport() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		given(userSkillPriorityReportCommandService.createPriorityReport()).willReturn(
				UserSkillPriorityReport.builder()
						.id("8f5634b8-783f-4503-b40f-ca93d8db7e72")
						.date(now)
						.userSkillPriorityAggregationReports(singletonList(
								UserSkillPriorityAggregationReport.builder()
										.id("123")
										.skillName("Neo4j")
										.userCount(2)
										.averagePriority(3D)
										.maximumPriority(4D)
										.userSkillReports(asList(
												UserSkillReport.builder()
														.skillName("Neo4j")
														.userName("tester")
														.currentLevel(2)
														.desiredLevel(4)
														.priority(2)
														.build(),
												UserSkillReport.builder()
														.skillName("Neo4j")
														.userName("tester2")
														.currentLevel(1)
														.desiredLevel(3)
														.priority(4)
														.build()
												)
										)
										.build()
								)
						)
						.build()
		);
		mockMvc.perform(post("/reports")
				.accept(MediaType.APPLICATION_JSON)
				.with(csrf())
				.with(user("tester").password("123").roles("USER")))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id", is(equalTo("8f5634b8-783f-4503-b40f-ca93d8db7e72"))))
				.andExpect(jsonPath("$.skillCount", is(equalTo(1))));
		// TODO: Add assertion for "date" property.
	}
}
