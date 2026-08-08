package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sdm_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdmDocument {
    @Id
    @Column(name = "id", length = 50)
    private String id; // e.g. P001, P002 ... P016

    @Column(name = "phase_number", nullable = false)
    private int phaseNumber; // 1 to 7

    @Column(name = "phase_name", nullable = false, length = 100)
    private String phaseName;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 1000)
    private String description; // Configurable
}
