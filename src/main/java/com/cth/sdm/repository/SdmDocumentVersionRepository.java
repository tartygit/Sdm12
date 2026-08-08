package com.cth.sdm.repository;

import com.cth.sdm.model.SdmDocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SdmDocumentVersionRepository extends JpaRepository<SdmDocumentVersion, Long> {
    List<SdmDocumentVersion> findByApplicationCode_AppCode(String appCode);
    List<SdmDocumentVersion> findByStatus(String status);
    List<SdmDocumentVersion> findByApplicationCode_AppCodeAndDocument_IdOrderBySubmittedAtDesc(String appCode, String docId);
}
