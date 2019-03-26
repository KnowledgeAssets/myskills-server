package io.knowledgeassets.myskills.server.communityuser;

import io.knowledgeassets.myskills.server.community.CommunityRole;
import io.knowledgeassets.myskills.server.user.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface CommunityUserRepository extends Neo4jRepository<CommunityUser, Long> {

	Optional<CommunityUser> findByUserIdAndCommunityId(String userId, String communityId);

	Optional<CommunityUser> findByUserIdAndCommunityIdAndRole(String userId, String communityId, CommunityRole role);

	Stream<CommunityUser> findByCommunityId(String communityId);

	Stream<CommunityUser> findByCommunityIdAndRole(String communityId, CommunityRole role);

	@Query("MATCH (u:User) WHERE NOT (u)-[:COMMUNITY_USER]-(:Community {id: {communityId}}) " +
			"AND ((toLower(u.userName) CONTAINS toLower({search}) " +
			" OR toLower(u.firstName) CONTAINS toLower({search}) " +
			" OR toLower(u.lastName) CONTAINS toLower({search})) " +
			" OR {search} IS NULL) RETURN u")
	Iterable<User> getUsersNotRelatedToCommunity(@Param("communityId") String communityId, @Param("search") String search);

}