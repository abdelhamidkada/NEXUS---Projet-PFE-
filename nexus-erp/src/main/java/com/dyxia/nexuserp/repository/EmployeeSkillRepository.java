package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.EmployeeSkill;
import com.dyxia.nexuserp.model.EmployeeSkillId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface Repository pour l'accès aux données de l'entité de jointure EmployeeSkill.
 */
@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, EmployeeSkillId> {
}
