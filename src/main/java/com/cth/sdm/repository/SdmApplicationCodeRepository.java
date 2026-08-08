package com.cth.sdm.repository;

import com.cth.sdm.model.SdmApplicationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SdmApplicationCodeRepository extends JpaRepository<SdmApplicationCode, String> {
}
