package com.cth.sdm.service;

import com.cth.sdm.model.SdmUser;
import com.cth.sdm.repository.SdmUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private SdmUserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SdmConfigService sdmConfigService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public SdmUser createUser(String username, String password, String email, String fullName, String role) {
        // Validate password rules
        validatePasswordStrength(password);

        if (userRepository.existsById(username)) {
            throw new IllegalArgumentException("Username already exists!");
        }

        SdmUser user = SdmUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .email(email)
                .fullName(fullName)
                .role(role)
                .locked(false)
                .failedAttempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        SdmUser saved = userRepository.save(user);
        auditLogService.logAction(username, "CREATE_ACCOUNT", "User account created successfully for " + username);
        return saved;
    }

    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasDigit = false;
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        if (!hasDigit || !hasUpper || !hasLower || !hasSpecial) {
            throw new IllegalArgumentException("Password must contain at least one digit, one uppercase letter, one lowercase letter, and one special character.");
        }
    }

    @Transactional
    public void recordFailedAttempt(String username) {
        userRepository.findById(username).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                auditLogService.logAction(username, "LOCK_USER", "User account locked due to too many failed login attempts");
            }
            userRepository.save(user);
        });
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findById(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            userRepository.save(user);
        });
    }

    @Transactional
    public void lockUser(String username) {
        userRepository.findById(username).ifPresent(user -> {
            user.setLocked(true);
            userRepository.save(user);
            auditLogService.logAction(username, "ADMIN_LOCK", "User manually locked by administrator");
        });
    }

    @Transactional
    public void unlockUser(String username) {
        userRepository.findById(username).ifPresent(user -> {
            user.setLocked(false);
            user.setFailedAttempts(0);
            userRepository.save(user);
            auditLogService.logAction(username, "ADMIN_UNLOCK", "User unlocked by administrator");
        });
    }

    @Transactional
    public String generatePasswordResetToken(String username, String email) {
        Optional<SdmUser> userOpt = userRepository.findById(username);
        if (userOpt.isPresent() && userOpt.get().getEmail().equalsIgnoreCase(email)) {
            SdmUser user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            auditLogService.logAction(username, "RESET_PASSWORD_REQUEST", "Password reset requested");
            return token;
        }
        throw new IllegalArgumentException("Username and email combination mismatch.");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        validatePasswordStrength(newPassword);
        SdmUser user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid password reset token."));

        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
        auditLogService.logAction(user.getUsername(), "RESET_PASSWORD_SUCCESS", "Password successfully reset");
    }

    @Transactional
    public void initDemoUsers() {
        if (userRepository.count() == 0) {
            // Seed defaults: admin, maker, checker
            createUser("admin", "Admin@123", "admin@cth.com", "Administrator", "ADMIN");
            createUser("maker", "Maker@123", "maker@cth.com", "Project Maker", "MAKER");
            createUser("checker", "Checker@123", "checker@cth.com", "Project Checker", "CHECKER");
        }
    }

    public List<SdmUser> getAllUsers() {
        return userRepository.findAll();
    }

    public String getAuthStrategy() {
        return sdmConfigService.getAuthStrategy();
    }
}
