package com.cth.sdm;

import com.cth.sdm.model.SdmDocument;
import com.cth.sdm.repository.SdmDocumentRepository;
import com.cth.sdm.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SdmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdmApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(SdmDocumentRepository documentRepository, UserService userService) {
        return args -> {
            // 1. Seed default demo users
            userService.initDemoUsers();

            // 2. Seed deliverables if empty
            if (documentRepository.count() == 0) {
                documentRepository.save(new SdmDocument("P001", 1, "Phase 1: Requirements Gathering", "Business Requirement Document (BRD)", "Defines business needs, scope, and objectives."));
                documentRepository.save(new SdmDocument("P002", 1, "Phase 1: Requirements Gathering", "Functional Specification Document (FSD)", "Detailed functional behaviors, workflows, and specifications."));
                documentRepository.save(new SdmDocument("P003", 1, "Phase 1: Requirements Gathering", "Use Case Specification (UCS)", "Actor interaction flows and step-by-step use scenarios."));

                documentRepository.save(new SdmDocument("P004", 2, "Phase 2: System Design", "High Level Design Document (HLD)", "Overall system architecture, modules, and interfaces."));
                documentRepository.save(new SdmDocument("P005", 2, "Phase 2: System Design", "Detailed Level Design Document (DLD)", "Component specifications, class designs, and sequencing."));
                documentRepository.save(new SdmDocument("P006", 2, "Phase 2: System Design", "Database Design Document (DDD)", "Data models, entity relationships, schema tables, and dictionaries."));

                documentRepository.save(new SdmDocument("P007", 3, "Phase 3: Development & Unit Testing", "Unit Test Plan (UTP)", "Strategy, setups, and scenarios for unit level validation."));
                documentRepository.save(new SdmDocument("P008", 3, "Phase 3: Development & Unit Testing", "Unit Test Report (UTR)", "Results and metrics of executed unit tests."));

                documentRepository.save(new SdmDocument("P009", 4, "Phase 4: Integration Testing", "System Integration Test Plan (SITP)", "Strategy for validating composite interfaces."));
                documentRepository.save(new SdmDocument("P010", 4, "Phase 4: Integration Testing", "System Integration Test Report (SITR)", "Logs, execution findings, and results of SIT."));

                documentRepository.save(new SdmDocument("P011", 5, "Phase 5: User Acceptance Testing", "User Acceptance Test Plan (UATP)", "Strategy and test cases for business users check."));
                documentRepository.save(new SdmDocument("P012", 5, "Phase 5: User Acceptance Testing", "User Acceptance Test Report (UATR)", "Business signoff logs, outcomes, and business clearance."));

                documentRepository.save(new SdmDocument("P013", 6, "Phase 6: Deployment & Go-Live", "Deployment Plan (DP)", "Release checklist, server configurations, rollback plans."));
                documentRepository.save(new SdmDocument("P014", 6, "Phase 6: Deployment & Go-Live", "Operations Manual (OM)", "Sysadmin running logs, backups, support paths, and diagnostics."));

                documentRepository.save(new SdmDocument("P015", 7, "Phase 7: Post Go-Live Support & Closure", "Post Implementation Review (PIR)", "Project performance check, learnings, and user response."));
                documentRepository.save(new SdmDocument("P016", 7, "Phase 7: Post Go-Live Support & Closure", "Project Closure Report (PCR)", "Formal signoff sheet, handovers, and close milestones."));
            }
        };
    }
}
