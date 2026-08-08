package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sdm_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdmApplicationCode {
    @Id
    @Column(name = "app_code", length = 3)
    private String appCode; // 3-character app code

    @Column(name = "app_name", nullable = false)
    private String appName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private SdmUser createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
