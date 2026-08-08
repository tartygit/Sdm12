-- ====================================================================
-- Database: SQL SERVER
-- Application: Software Development Document Environment (SDM)
-- Description: Schema initialization script for SQL Server environment.
-- ====================================================================

-- 1. Configuration Settings
CREATE TABLE sdm_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(1000)
);

-- 2. Users and Security Maintenance
CREATE TABLE sdm_users (
    username VARCHAR(100) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'MAKER' NOT NULL, -- MAKER, CHECKER, ADMIN
    is_locked BIT DEFAULT 0 NOT NULL,
    failed_attempts INT DEFAULT 0 NOT NULL,
    password_reset_token VARCHAR(255),
    password_reset_expiry DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. App code submission sets (3-character tags)
CREATE TABLE sdm_applications (
    app_code VARCHAR(3) PRIMARY KEY,
    app_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_app_created_by FOREIGN KEY (created_by) REFERENCES sdm_users(username)
);

-- 4. Document Deliverables (P001 to P016 across 7 Phases)
CREATE TABLE sdm_documents (
    id VARCHAR(50) PRIMARY KEY,       -- e.g. P001, P002 ... P016
    phase_number INT NOT NULL,          -- 1 to 7
    phase_name VARCHAR(100) NOT NULL,  -- Phase 1 to Phase 7 names
    title VARCHAR(255) NOT NULL,       -- Document Deliverable Title
    description VARCHAR(1000)          -- Configurable description
);

-- 5. Document Versioning & Ingestion Metadata
CREATE TABLE sdm_document_versions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    document_id VARCHAR(50) NOT NULL,
    app_code VARCHAR(3) NOT NULL,
    version_number VARCHAR(50) NOT NULL,  -- Configurable version
    document_code VARCHAR(100) NOT NULL,  -- Configurable code
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,       -- EXCEL, WORD, POWERPOINT, XML, etc.
    status VARCHAR(50) NOT NULL,          -- PENDING_APPROVAL, APPROVED, REJECTED
    maker_username VARCHAR(100) NOT NULL,
    checker_username VARCHAR(100),
    rejection_remarks VARCHAR(1000),
    parsed_sections_json VARCHAR(MAX),     -- Extracted sections/tabs and contents
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    reviewed_at DATETIME,
    CONSTRAINT fk_doc_ver_doc_id FOREIGN KEY (document_id) REFERENCES sdm_documents(id),
    CONSTRAINT fk_doc_ver_app_code FOREIGN KEY (app_code) REFERENCES sdm_applications(app_code),
    CONSTRAINT fk_doc_ver_maker FOREIGN KEY (maker_username) REFERENCES sdm_users(username),
    CONSTRAINT fk_doc_ver_checker FOREIGN KEY (checker_username) REFERENCES sdm_users(username)
);

-- 6. User Sessions & Audit Logs
CREATE TABLE sdm_audit_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    details VARCHAR(1000) NOT NULL,
    ip_address VARCHAR(50),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed static deliverables data (P001 to P016)
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P001', 1, 'Phase 1: Requirements Gathering', 'Business Requirement Document (BRD)', 'Defines business needs, scope, and objectives.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P002', 1, 'Phase 1: Requirements Gathering', 'Functional Specification Document (FSD)', 'Detailed functional behaviors, workflows, and specifications.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P003', 1, 'Phase 1: Requirements Gathering', 'Use Case Specification (UCS)', 'Actor interaction flows and step-by-step use scenarios.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P004', 2, 'Phase 2: System Design', 'High Level Design Document (HLD)', 'Overall system architecture, modules, and interfaces.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P005', 2, 'Phase 2: System Design', 'Detailed Level Design Document (DLD)', 'Component specifications, class designs, and sequencing.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P006', 2, 'Phase 2: System Design', 'Database Design Document (DDD)', 'Data models, entity relationships, schema tables, and dictionaries.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P007', 3, 'Phase 3: Development & Unit Testing', 'Unit Test Plan (UTP)', 'Strategy, setups, and scenarios for unit level validation.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P008', 3, 'Phase 3: Development & Unit Testing', 'Unit Test Report (UTR)', 'Results and metrics of executed unit tests.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P009', 4, 'Phase 4: Integration Testing', 'System Integration Test Plan (SITP)', 'Strategy for validating composite interfaces.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P010', 4, 'Phase 4: Integration Testing', 'System Integration Test Report (SITR)', 'Logs, execution findings, and results of SIT.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P011', 5, 'Phase 5: User Acceptance Testing', 'User Acceptance Test Plan (UATP)', 'Strategy and test cases for business users check.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P012', 5, 'Phase 5: User Acceptance Testing', 'User Acceptance Test Report (UATR)', 'Business signoff logs, outcomes, and business clearance.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P013', 6, 'Phase 6: Deployment & Go-Live', 'Deployment Plan (DP)', 'Release checklist, server configurations, rollback plans.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P014', 6, 'Phase 6: Deployment & Go-Live', 'Operations Manual (OM)', 'Sysadmin running logs, backups, support paths, and diagnostics.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P015', 7, 'Phase 7: Post Go-Live Support & Closure', 'Post Implementation Review (PIR)', 'Project performance check, learnings, and user response.');
INSERT INTO sdm_documents (id, phase_number, phase_name, title, description) VALUES ('P016', 7, 'Phase 7: Post Go-Live Support & Closure', 'Project Closure Report (PCR)', 'Formal signoff sheet, handovers, and close milestones.');
