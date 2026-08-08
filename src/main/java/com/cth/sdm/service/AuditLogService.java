package com.cth.sdm.service;

import com.cth.sdm.model.SdmAuditLog;
import com.cth.sdm.repository.SdmAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private SdmAuditLogRepository auditLogRepository;

    public void logAction(String username, String action, String details) {
        SdmAuditLog log = SdmAuditLog.builder()
                .username(username)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<SdmAuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
