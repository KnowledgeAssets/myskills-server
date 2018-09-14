package io.knowledgeassets.myskills.server.skill.query;

import io.knowledgeassets.myskills.server.exception.EmptyInputException;
import io.knowledgeassets.myskills.server.skill.Skill;
import io.knowledgeassets.myskills.server.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class SkillQueryService {
	private SkillRepository skillRepository;

	public SkillQueryService(SkillRepository skillRepository) {
		this.skillRepository = skillRepository;
	}

	@Transactional(readOnly = true)
	public Stream<Skill> getSkills() {
		return StreamSupport.stream(skillRepository.findAll().spliterator(), false);
	}

	@Transactional(readOnly = true)
	public Optional<Skill> getSkillById(String skillId) {
		return skillRepository.findById(skillId);
	}

	/**
	 * It ignores case sensitivity
	 *
	 * @param skillName
	 * @return
	 */
	@Transactional(readOnly = true)
	public Optional<Skill> findByNameIgnoreCase(String skillName) {
		return skillRepository.findByNameIgnoreCase(skillName);
	}

	@Transactional(readOnly = true)
	public boolean exists(String skillId) throws EmptyInputException {
		if (skillId == null) {
			throw EmptyInputException.builder()
					.code(111111L)
					.message("skillId is null.")
					.build();
		}
		return skillRepository.existsById(skillId);
	}

	public Boolean isSkillExist(String search) {
		return skillRepository.isSkillExistByNameIgnoreCase(search);
	}
}
