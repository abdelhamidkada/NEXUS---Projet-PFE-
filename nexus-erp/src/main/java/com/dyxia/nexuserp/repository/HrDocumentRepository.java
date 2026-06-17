package com.dyxia.nexuserp.repository;

import com.dyxia.nexuserp.model.HrDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface Repository pour l'accès aux données des documents RH (HrDocument).
 */
@Repository
public interface HrDocumentRepository extends JpaRepository<HrDocument, Long> {
}
