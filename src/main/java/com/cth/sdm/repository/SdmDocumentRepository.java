package com.cth.sdm.repository;

import com.cth.sdm.model.SdmDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SdmDocumentRepository extends JpaRepository<SdmDocument, String> {
    List<SdmDocument> findAllByOrderByPhaseNumberAscIdAsc();
}
