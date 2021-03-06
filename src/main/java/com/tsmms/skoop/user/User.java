package com.tsmms.skoop.user;

import com.tsmms.skoop.communityuser.CommunityUser;
import com.tsmms.skoop.userproject.UserProject;
import com.tsmms.skoop.userskill.UserSkill;
import lombok.*;
import org.neo4j.ogm.annotation.*;

import java.util.List;

import static org.neo4j.ogm.annotation.Relationship.UNDIRECTED;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NodeEntity
public class User {
	@Id
	@Property(name = "id")
	private String id;

	/**
	 * This field is forbidden to expose to any external system together with personal user data!!!
	 */
	@Property(name = "referenceId")
	private String referenceId;

	@Property(name = "userName")
	@Index(unique = true)
	private String userName;

	@Property(name = "firstName")
	private String firstName;

	@Property(name = "lastName")
	private String lastName;

	@Property(name = "email")
	private String email;

	@Property(name = "academicDegree")
	private String academicDegree;

	@Property(name = "positionProfile")
	private String positionProfile;

	@Property(name = "summary")
	private String summary;

	@Property(name = "industrySectors")
	private List<String> industrySectors;

	@Property(name = "specializations")
	private List<String> specializations;

	@Property(name = "certificates")
	private List<String> certificates;

	@Property(name = "languages")
	private List<String> languages;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Relationship(type = "RELATED_TO", direction = UNDIRECTED)
	private List<UserSkill> userSkills;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Relationship(type = "USER_PROJECT", direction = UNDIRECTED)
	private List<UserProject> userProjects;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Relationship(type = "COMMUNITY_USER", direction = UNDIRECTED)
	private List<CommunityUser> communityUsers;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Relationship(type = "HAS_GRANTED")
	private List<UserPermission> userPermissions;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@Relationship(type = "MANAGER")
	private User manager;
}
