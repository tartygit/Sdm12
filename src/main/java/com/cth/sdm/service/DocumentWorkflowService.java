package com.cth.sdm.service;

import com.cth.sdm.handler.DocumentHandler;
import com.cth.sdm.model.*;
import com.cth.sdm.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocumentWorkflowService {

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

    @Autowired
    private List<DocumentHandler> handlers;

    @Value("${app.notifications.enabled:false}")
    private boolean notificationsEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SdmDocumentVersion submitDocument(String docId, String appCode, String version, String docCode, MultipartFile file, String makerUsername) throws Exception {
        SdmDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Deliverable Document ID: " + docId));

        SdmUser maker = userRepository.findById(makerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + makerUsername));

        // Format app code to 3-char Limit
        if (appCode == null || appCode.length() != 3) {
            throw new IllegalArgumentException("Application code must be exactly 3 characters.");
        }
        appCode = appCode.toUpperCase();

        Optional<SdmApplicationCode> appCodeOpt = applicationCodeRepository.findById(appCode);
        SdmApplicationCode applicationCode;
        if (appCodeOpt.isEmpty()) {
            applicationCode = SdmApplicationCode.builder()
                    .appCode(appCode)
                    .appName("Application " + appCode)
                    .createdBy(maker)
                    .createdAt(LocalDateTime.now())
                    .build();
            applicationCodeRepository.save(applicationCode);
        } else {
            applicationCode = appCodeOpt.get();
        }

        String fileName = file.getOriginalFilename();
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1).toLowerCase();
        }

        final String finalExtension = extension;
        DocumentHandler handler = handlers.stream()
                .filter(h -> h.supports(finalExtension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("File type not supported: ." + finalExtension));

        // Convert MultipartFile to temp file for handler processing
        File tempFile = File.createTempFile("upload-", "." + extension);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        Map<String, String> parsedMap;
        try {
            parsedMap = handler.handle(tempFile);
        } finally {
            tempFile.delete();
        }

        String jsonStr = objectMapper.writeValueAsString(parsedMap);

        SdmDocumentVersion docVersion = SdmDocumentVersion.builder()
                .document(doc)
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

        SdmDocumentVersion saved = documentVersionRepository.save(docVersion);

        auditLogService.logAction(makerUsername, "SUBMIT_DOCUMENT",
                "Submitted document " + fileName + " (" + docId + ") for approval. App code: " + appCode);

        // Notify Approvers/Checkers if toggle switch is turned ON
        if (notificationsEnabled) {
            sendNotificationToApprovers(docVersion);
        }

        return saved;
    }

    @Transactional
    public void approveDocument(Long versionId, String checkerUsername) {
        SdmDocumentVersion docVersion = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid document version ID: " + versionId));

        SdmUser checker = userRepository.findById(checkerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Checker not found: " + checkerUsername));

        docVersion.setStatus("APPROVED");
        docVersion.setChecker(checker);
        docVersion.setReviewedAt(LocalDateTime.now());
        documentVersionRepository.save(docVersion);

        auditLogService.logAction(checkerUsername, "APPROVE_DOCUMENT",
                "Approved document " + docVersion.getFileName() + " (" + docVersion.getDocument().getId() + ")");
    }

    @Transactional
    public void rejectDocument(Long versionId, String checkerUsername, String remarks) {
        SdmDocumentVersion docVersion = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid document version ID: " + versionId));

        SdmUser checker = userRepository.findById(checkerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Checker not found: " + checkerUsername));

        docVersion.setStatus("REJECTED");
        docVersion.setChecker(checker);
        docVersion.setRejectionRemarks(remarks);
        docVersion.setReviewedAt(LocalDateTime.now());
        documentVersionRepository.save(docVersion);

        auditLogService.logAction(checkerUsername, "REJECT_DOCUMENT",
                "Rejected document " + docVersion.getFileName() + " (" + docVersion.getDocument().getId() + ") due to: " + remarks);
    }

    private void sendNotificationToApprovers(SdmDocumentVersion docVersion) {
        // Mock notification logic
        System.out.println(">>> SIMULATED NOTIFICATION <<<");
        System.out.println("TO: checkers@cth.com");
        System.out.println("SUBJECT: New SDM Document Submission PENDING Approval");
        System.out.println("MESSAGE: A new deliverable '" + docVersion.getDocument().getTitle() +
                           "' (ID: " + docVersion.getDocument().getId() + ") has been submitted by maker " +
                           docVersion.getMaker().getUsername() + " under App Code " + docVersion.getApplicationCode().getAppCode());
        System.out.println(">>> END NOTIFICATION <<<");
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
