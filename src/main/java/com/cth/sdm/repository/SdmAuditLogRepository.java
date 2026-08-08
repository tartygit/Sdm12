package com.cth.sdm.repository;

import com.cth.sdm.model.SdmAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SdmAuditLogRepository extends JpaRepository<SdmAuditLog, Long> {
    List<SdmAuditLog> findAllByOrderByTimestampDesc();
}
