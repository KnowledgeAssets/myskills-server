package com.tsmms.skoop.membership.command;

import com.tsmms.skoop.skill.Skill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembershipUpdateCommand {

	private String name;
	private String description;
	private String link;
	private LocalDate startDate;
	private LocalDate endDate;
	private Set<Skill> skills;

}
