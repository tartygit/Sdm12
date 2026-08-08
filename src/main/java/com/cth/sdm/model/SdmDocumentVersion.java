package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sdm_document_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdmDocumentVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", nullable = false)
    private SdmDocument document;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_code", nullable = false)
    private SdmApplicationCode applicationCode;

    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    @Column(name = "document_code", nullable = false, length = 100)
    private String documentCode;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType; // EXCEL, WORD, POWERPOINT, XML

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING_APPROVAL, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maker_username", nullable = false)
    private SdmUser maker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checker_username")
    private SdmUser checker;

    @Column(name = "rejection_remarks", length = 1000)
    private String rejectionRemarks;

    @Lob
    @Column(name = "parsed_sections_json")
    private String parsedSectionsJson; // JSON representation of the tabs/sections and text details inside

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
