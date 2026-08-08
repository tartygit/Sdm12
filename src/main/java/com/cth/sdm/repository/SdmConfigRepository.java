package com.cth.sdm.repository;

import com.cth.sdm.model.SdmConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SdmConfigRepository extends JpaRepository<SdmConfig, String> {
}
