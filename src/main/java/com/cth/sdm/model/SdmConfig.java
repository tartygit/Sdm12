package com.cth.sdm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sdm_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdmConfig {
    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 500)
    private String value;

    @Column(name = "description", length = 1000)
    private String description;
}
