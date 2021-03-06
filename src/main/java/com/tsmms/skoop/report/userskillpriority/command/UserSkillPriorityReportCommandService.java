package com.tsmms.skoop.report.userskillpriority.command;

import com.tsmms.skoop.report.userskill.UserSkillReport;
import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityAggregationReport;
import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityAggregationReportResult;
import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityReport;
import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityReportRepository;
import com.tsmms.skoop.report.userskillpriority.query.UserSkillPriorityReportQueryService;
import com.tsmms.skoop.userskill.UserSkill;
import com.tsmms.skoop.userskill.query.UserSkillQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
public class UserSkillPriorityReportCommandService {
	private UserSkillPriorityReportRepository userSkillPriorityReportRepository;
	private UserSkillPriorityReportQueryService userSkillPriorityReportQueryService;
	private UserSkillQueryService userSkillQueryService;

	public UserSkillPriorityReportCommandService(UserSkillPriorityReportRepository userSkillPriorityReportRepository,
												 UserSkillPriorityReportQueryService userSkillPriorityReportQueryService,
												 UserSkillQueryService userSkillQueryService) {
		this.userSkillPriorityReportRepository = userSkillPriorityReportRepository;
		this.userSkillPriorityReportQueryService = userSkillPriorityReportQueryService;
		this.userSkillQueryService = userSkillQueryService;
	}

	/**
	 * Creates a new user skill priority report.
	 *
	 * @return Created report.
	 */
	@Transactional
	public UserSkillPriorityReport createUserSkillPriorityReport() {
		Stream<UserSkillPriorityAggregationReportResult> aggregationResults =
				userSkillPriorityReportQueryService.getAllUserSkillPriorityAggregationResults();
		List<UserSkillPriorityAggregationReport> aggregationReports = convertToAggregationReports(aggregationResults);
		return saveReport(aggregationReports);
	}

	/**
	 * Creates an aggregation report for each aggregation result in the given stream.
	 *
	 * @param aggregationResults Stream of results to create aggregation reports from.
	 * @return Aggregation reports for the given stream of results.
	 */
	private List<UserSkillPriorityAggregationReport> convertToAggregationReports(
			Stream<UserSkillPriorityAggregationReportResult> aggregationResults) {
		return aggregationResults.map(aggregationResult -> UserSkillPriorityAggregationReport.builder()
				.id(UUID.randomUUID().toString())
				.averagePriority(aggregationResult.getAveragePriority())
				.maximumPriority(aggregationResult.getMaximumPriority())
				.userCount(aggregationResult.getUserCount())
				.skillName(aggregationResult.getSkill().getName())
				.skillDescription(aggregationResult.getSkill().getDescription())
				.userSkillReports(convertToUserSkillReports(aggregationResult))
				.build()
		).collect(toList());
	}

	/**
	 * Creates a user skill report for each user assigned to the skill represented by the given aggregation result.
	 *
	 * @param aggregationResult Aggregation result to create user skill reports from.
	 * @return User skill reports for the users given in the aggregation result.
	 */
	private List<UserSkillReport> convertToUserSkillReports(UserSkillPriorityAggregationReportResult aggregationResult) {
		return aggregationResult.getUsers().stream().map(user -> {
			Optional<UserSkill> userSkillResult = userSkillQueryService
					.getUserSkillByUserIdAndSkillId(user.getId(), aggregationResult.getSkill().getId());
			UserSkill userSkill = userSkillResult.orElseThrow(() ->
					new IllegalStateException(format("User with ID '%s' is not assigned to skill with ID '%s'",
							user.getId(), aggregationResult.getSkill().getId())));
			return UserSkillReport.builder()
					.id(UUID.randomUUID().toString())
					.currentLevel(userSkill.getCurrentLevel())
					.desiredLevel(userSkill.getDesiredLevel())
					.priority(userSkill.getPriority())
					.userName(user.getUserName())
					.userFirstName(user.getFirstName())
					.userLastName(user.getLastName())
					.skillName(aggregationResult.getSkill().getName())
					.skillDescription(aggregationResult.getSkill().getDescription())
					.build();
		}).collect(toList());
	}

	private UserSkillPriorityReport saveReport(List<UserSkillPriorityAggregationReport> aggregationReports) {
		UserSkillPriorityReport userSkillPriorityReport = UserSkillPriorityReport.builder()
				.id(UUID.randomUUID().toString())
				.date(LocalDateTime.now())
				.aggregationReports(aggregationReports)
				.build();
		return userSkillPriorityReportRepository.save(userSkillPriorityReport);
	}
}
