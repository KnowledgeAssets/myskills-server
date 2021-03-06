package com.tsmms.skoop.report.userskillpriority.command;

import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityReport;
import com.tsmms.skoop.report.userskillpriority.UserSkillPriorityReportSimpleResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "UserSkillPriorityReports")
@RestController
public class UserSkillPriorityReportCommandController {
	private UserSkillPriorityReportCommandService userSkillPriorityReportCommandService;

	public UserSkillPriorityReportCommandController(UserSkillPriorityReportCommandService userSkillPriorityReportCommandService) {
		this.userSkillPriorityReportCommandService = userSkillPriorityReportCommandService;
	}

	@ApiOperation(
			value = "Create a new user skill priority report",
			notes = "Create a new user skill priority report based on the current data in the system."
	)
	@ApiResponses({
			@ApiResponse(code = 201, message = "Successful execution"),
			@ApiResponse(code = 401, message = "Invalid authentication"),
			@ApiResponse(code = 403, message = "Insufficient privileges to perform this operation"),
			@ApiResponse(code = 500, message = "Error during execution")
	})
	@PreAuthorize("isAuthenticated()")
	@PostMapping(
			path = "/reports/skills/priority",
			consumes = MediaType.ALL_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<UserSkillPriorityReportSimpleResponse> createReport() {
		UserSkillPriorityReport report = userSkillPriorityReportCommandService.createUserSkillPriorityReport();
		return ResponseEntity.status(HttpStatus.CREATED).body(UserSkillPriorityReportSimpleResponse.builder()
				.id(report.getId())
				.date(report.getDate())
				.skillCount(report.getAggregationReports().size())
				.build());
	}
}
