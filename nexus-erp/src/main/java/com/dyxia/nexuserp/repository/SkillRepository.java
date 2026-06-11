package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface Repository pour l'accès aux données de l'entité Skill.
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
}
