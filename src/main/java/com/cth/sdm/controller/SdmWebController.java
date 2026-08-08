package com.cth.sdm.controller;

import com.cth.sdm.model.*;
import com.cth.sdm.repository.*;
import com.cth.sdm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SdmWebController {

    @Value("${app.name:Software Development Document Environment}")
    private String appName;

    @Autowired
    private SdmDocumentRepository documentRepository;

    @Autowired
    private SdmApplicationCodeRepository applicationCodeRepository;

    @Autowired
    private SdmDocumentVersionRepository documentVersionRepository;

    @Autowired
    private SdmUserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SdmConfigService sdmConfigService;

    @Autowired
    private DocumentWorkflowService workflowService;

    @Autowired
    private DocumentParsingService parsingService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private AuditLogService auditLogService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("appName", appName);
        model.addAttribute("authStrategy", sdmConfigService.getAuthStrategy());
    }

    @GetMapping("/login")
    public String loginPage() {
        userService.initDemoUsers();
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               @RequestParam String fullName,
                               @RequestParam String role,
                               Model model) {
        try {
            userService.createUser(username, password, email, fullName, role);
            model.addAttribute("successMessage", "Registration successful! You can now log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String username,
                                       @RequestParam String email,
                                       Model model) {
        try {
            String token = userService.generatePasswordResetToken(username, email);
            model.addAttribute("resetToken", token);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token,
                                      @RequestParam String newPassword,
                                      Model model) {
        try {
            userService.resetPassword(token, newPassword);
            model.addAttribute("successMessage", "Password reset successfully! Please log in.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String appCode, Model model, Principal principal) {
        List<SdmDocument> documents = documentRepository.findAllByOrderByPhaseNumberAscIdAsc();
        List<SdmApplicationCode> allAppCodes = applicationCodeRepository.findAll();

        String selectedAppCode = appCode;
        if (selectedAppCode == null || selectedAppCode.trim().isEmpty()) {
            if (!allAppCodes.isEmpty()) {
                selectedAppCode = allAppCodes.get(0).getAppCode();
            } else {
                selectedAppCode = "PRJ";
            }
        }
        selectedAppCode = selectedAppCode.toUpperCase();

        model.addAttribute("documents", documents);
        model.addAttribute("allAppCodes", allAppCodes);
        model.addAttribute("currentAppCode", selectedAppCode);

        // Map versions for quick lookup on UI panels
        Map<String, List<SdmDocumentVersion>> versionsMap = new HashMap<>();
        for (SdmDocument doc : documents) {
            List<SdmDocumentVersion> versions = documentVersionRepository
                    .findByApplicationCode_AppCodeAndDocument_IdOrderBySubmittedAtDesc(selectedAppCode, doc.getId());
            versionsMap.put(doc.getId(), versions);
        }
        model.addAttribute("versionsMap", versionsMap);

        // Pending queues for checkers
        List<SdmDocumentVersion> pending = documentVersionRepository.findByStatus("PENDING_APPROVAL");
        model.addAttribute("pendingApprovals", pending);

        return "dashboard";
    }

    @PostMapping("/submit")
    public String submitDocument(@RequestParam String docId,
                                 @RequestParam String appCode,
                                 @RequestParam String version,
                                 @RequestParam String docCode,
                                 @RequestParam MultipartFile file,
                                 Principal principal,
                                 Model model) {
        try {
            workflowService.submitDocument(docId, appCode, version, docCode, file, principal.getName());
            return "redirect:/?appCode=" + appCode + "&success=Document submitted successfully for review!";
        } catch (Exception e) {
            return "redirect:/?appCode=" + appCode + "&error=" + e.getMessage();
        }
    }

    @GetMapping("/document/view")
    public String viewDocument(@RequestParam Long id, Model model) {
        SdmDocumentVersion version = documentVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid document version ID: " + id));

        Map<String, String> sections = parsingService.deserializeSections(version.getParsedSectionsJson());

        model.addAttribute("ver", version);
        model.addAttribute("sections", sections);
        return "view-document";
    }

    @PostMapping("/document/approve")
    public String approveDocument(@RequestParam Long id, Principal principal) {
        try {
            workflowService.approveDocument(id, principal.getName());
            return "redirect:/?success=Document successfully approved!";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }

    @PostMapping("/document/reject")
    public String rejectDocument(@RequestParam Long id, @RequestParam String remarks, Principal principal) {
        try {
            workflowService.rejectDocument(id, principal.getName(), remarks);
            return "redirect:/?success=Document rejected and returned with feedback.";
        } catch (Exception e) {
            return "redirect:/?error=" + e.getMessage();
        }
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("logs", auditLogService.getAllLogs());
        model.addAttribute("mfaEnabled", sdmConfigService.isMfaEnabled());
        model.addAttribute("notificationsEnabled", sdmConfigService.isNotificationsEnabled());
        return "admin";
    }

    @PostMapping("/admin/toggle-settings")
    public String saveGlobalSettings(@RequestParam(defaultValue = "false") boolean mfaEnabled,
                                     @RequestParam(defaultValue = "false") boolean notificationsEnabled,
                                     @RequestParam(defaultValue = "false") boolean ldapEnabled,
                                     Principal principal) {
        sdmConfigService.setMfaEnabled(mfaEnabled);
        sdmConfigService.setNotificationsEnabled(notificationsEnabled);
        sdmConfigService.setAuthStrategy(ldapEnabled ? "LDAP" : "DB");

        workflowService.setNotificationsEnabled(notificationsEnabled);

        auditLogService.logAction(principal.getName(), "SETTINGS_CHANGE",
                "MFA toggled: " + mfaEnabled + ", Alerts toggled: " + notificationsEnabled + ", Auth strategy set: " + (ldapEnabled ? "LDAP" : "DB"));
        return "redirect:/admin?success=Settings updated successfully.";
    }

    @PostMapping("/admin/users/lock")
    public String lockUser(@RequestParam String username) {
        userService.lockUser(username);
        return "redirect:/admin?success=User account locked.";
    }

    @PostMapping("/admin/users/unlock")
    public String unlockUser(@RequestParam String username) {
        userService.unlockUser(username);
        return "redirect:/admin?success=User account unlocked.";
    }

    // Reports endpoint
    @GetMapping("/reports")
    public String reportsPage(@RequestParam(required = false) String status,
                              @RequestParam(required = false) String appFilter,
                              Model model) {
        List<SdmDocumentVersion> all = documentVersionRepository.findAll();

        if (status != null && !status.trim().isEmpty()) {
            all = all.stream().filter(r -> r.getStatus().equalsIgnoreCase(status)).collect(Collectors.toList());
        }
        if (appFilter != null && !appFilter.trim().isEmpty()) {
            all = all.stream().filter(r -> r.getApplicationCode().getAppCode().equalsIgnoreCase(appFilter)).collect(Collectors.toList());
        }

        model.addAttribute("records", all);
        return "reports";
    }

    @GetMapping("/reports/download/excel")
    @ResponseBody
    public ResponseEntity<byte[]> downloadExcelReport() {
        try {
            List<SdmDocumentVersion> all = documentVersionRepository.findAll();
            byte[] bytes = reportingService.generateExcelReport(all);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sdm_deliverables_report.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/reports/download/pdf")
    @ResponseBody
    public ResponseEntity<byte[]> downloadPdfReport() {
        try {
            List<SdmDocumentVersion> all = documentVersionRepository.findAll();
            byte[] bytes = reportingService.generatePdfReport(all);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sdm_deliverables_report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
