package com.tsmms.skoop.search;

import com.tsmms.skoop.userskill.UserSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.annotation.QueryResult;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@QueryResult
public class AnonymousUserSkillResult {

	/**
	 * Anonymous reference to a user.
	 */
	private String referenceId;
	private List<UserSkill> userSkills;

}
