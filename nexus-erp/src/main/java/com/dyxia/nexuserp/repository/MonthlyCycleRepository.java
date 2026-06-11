package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.MonthlyCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'accès aux données des cycles mensuels {@link MonthlyCycle}.
 */
@Repository
public interface MonthlyCycleRepository extends JpaRepository<MonthlyCycle, Long> {
    List<MonthlyCycle> findByEmployeeProfileId(Long employeeProfileId);
    List<MonthlyCycle> findByValidatedAsWorkedTrueAndProcessedForAccrualFalse();
}
