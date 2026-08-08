package com.cth.sdm.service;

import com.cth.sdm.handler.DocumentHandler;
import com.cth.sdm.model.*;
import com.cth.sdm.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DirectoryPollingService {

    @Value("${app.poll.dir:input-documents}")
    private String pollDirName;

    @Autowired
    private List<DocumentHandler> handlers;

    @Autowired
    private SdmDocumentRepository documentRepository;

    @Autowired
    private SdmApplicationCodeRepository applicationCodeRepository;

    @Autowired
    private SdmDocumentVersionRepository documentVersionRepository;

    @Autowired
    private SdmUserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        File dir = new File(pollDirName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void pollDirectory() {
        File dir = new File(pollDirName);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        // We assume dropped files are named following a standard pattern:
        // [APP]_[DOC_ID]_[VERSION]_[DOC_CODE].[ext]
        // E.g.: PRJ_P001_V1.0_BRD-001.xlsx
        for (File file : files) {
            if (file.isFile()) {
                try {
                    processFile(file);
                } catch (Exception e) {
                    auditLogService.logAction("SYSTEM", "FILE_PARSE_ERROR",
                            "Failed to process file: " + file.getName() + " - " + e.getMessage());
                } finally {
                    // Always delete or move the file to avoid endless processing loop
                    file.delete();
                }
            }
        }
    }

    private void processFile(File file) throws Exception {
        String fileName = file.getName();
        String extension = "";
        int idx = fileName.lastIndexOf('.');
        if (idx > 0) {
            extension = fileName.substring(idx + 1).toLowerCase();
        }

        final String finalExtension = extension;
        DocumentHandler handler = handlers.stream()
                .filter(h -> h.supports(finalExtension))
                .findFirst()
                .orElse(null);

        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for extension: " + extension);
        }

        // Parse filename metadata
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        String[] parts = nameWithoutExt.split("_");

        String appCode = "PRJ";
        String docId = "P001";
        String version = "1.0.0";
        String docCode = "DOC-CODE";

        if (parts.length >= 4) {
            appCode = parts[0].toUpperCase();
            docId = parts[1].toUpperCase();
            version = parts[2];
            docCode = parts[3];
        } else if (parts.length == 3) {
            appCode = parts[0].toUpperCase();
            docId = parts[1].toUpperCase();
            version = parts[2];
        } else if (parts.length == 2) {
            appCode = parts[0].toUpperCase();
            docId = parts[1].toUpperCase();
        }

        // Ensure 3-char application code limit
        if (appCode.length() > 3) {
            appCode = appCode.substring(0, 3);
        }

        // Validate Document ID exists, otherwise default to P001
        Optional<SdmDocument> docOpt = documentRepository.findById(docId);
        if (docOpt.isEmpty()) {
            docId = "P001";
        }
        SdmDocument document = documentRepository.findById(docId).orElse(null);

        // Ensure Application Code exists in database
        Optional<SdmApplicationCode> appCodeOpt = applicationCodeRepository.findById(appCode);
        SdmApplicationCode applicationCode;
        if (appCodeOpt.isEmpty()) {
            SdmUser adminUser = userRepository.findById("admin").orElse(null);
            applicationCode = SdmApplicationCode.builder()
                    .appCode(appCode)
                    .appName("Application " + appCode)
                    .createdBy(adminUser)
                    .createdAt(LocalDateTime.now())
                    .build();
            applicationCodeRepository.save(applicationCode);
        } else {
            applicationCode = appCodeOpt.get();
        }

        // Invoke the designated parser handler
        Map<String, String> parsedMap = handler.handle(file);
        String jsonStr = objectMapper.writeValueAsString(parsedMap);

        // Fetch a default maker
        SdmUser maker = userRepository.findById("maker").orElse(null);

        // Save new Document Version as PENDING
        SdmDocumentVersion docVersion = SdmDocumentVersion.builder()
                .document(document)
                .applicationCode(applicationCode)
                .versionNumber(version)
                .documentCode(docCode)
                .fileName(fileName)
                .fileType(extension.toUpperCase())
                .status("PENDING_APPROVAL")
                .maker(maker)
                .parsedSectionsJson(jsonStr)
                .submittedAt(LocalDateTime.now())
                .build();

        documentVersionRepository.save(docVersion);

        auditLogService.logAction("SYSTEM", "AUTO_INGEST",
                "Successfully ingested document: " + fileName + " under Application Code " + appCode + " for deliverable " + docId);
    }
}
