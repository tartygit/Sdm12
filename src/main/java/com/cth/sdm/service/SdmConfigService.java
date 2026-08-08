package com.cth.sdm.service;

import com.cth.sdm.model.SdmConfig;
import com.cth.sdm.repository.SdmConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SdmConfigService {

    @Autowired
    private SdmConfigRepository configRepository;

    @Value("${app.auth.strategy:DB}")
    private String defaultAuthStrategy;

    @Value("${app.mfa.enabled:false}")
    private boolean defaultMfaEnabled;

    @Value("${app.notifications.enabled:false}")
    private boolean defaultNotificationsEnabled;

    public String getAuthStrategy() {
        return configRepository.findById("AUTH_STRATEGY")
                .map(SdmConfig::getValue)
                .orElse(defaultAuthStrategy);
    }

    @Transactional
    public void setAuthStrategy(String strategy) {
        SdmConfig config = configRepository.findById("AUTH_STRATEGY")
                .orElse(new SdmConfig("AUTH_STRATEGY", strategy, "Authentication Strategy: DB or LDAP"));
        config.setValue(strategy);
        configRepository.save(config);
    }

    public boolean isMfaEnabled() {
        return configRepository.findById("MFA_ENABLED")
                .map(config -> Boolean.parseBoolean(config.getValue()))
                .orElse(defaultMfaEnabled);
    }

    @Transactional
    public void setMfaEnabled(boolean enabled) {
        SdmConfig config = configRepository.findById("MFA_ENABLED")
                .orElse(new SdmConfig("MFA_ENABLED", String.valueOf(enabled), "SMS / Email MFA Toggle"));
        config.setValue(String.valueOf(enabled));
        configRepository.save(config);
    }

    public boolean isNotificationsEnabled() {
        return configRepository.findById("NOTIFICATIONS_ENABLED")
                .map(config -> Boolean.parseBoolean(config.getValue()))
                .orElse(defaultNotificationsEnabled);
    }

    @Transactional
    public void setNotificationsEnabled(boolean enabled) {
        SdmConfig config = configRepository.findById("NOTIFICATIONS_ENABLED")
                .orElse(new SdmConfig("NOTIFICATIONS_ENABLED", String.valueOf(enabled), "Approver Alert Notifications Toggle"));
        config.setValue(String.valueOf(enabled));
        configRepository.save(config);
    }
}
