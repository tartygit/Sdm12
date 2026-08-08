package com.cth.sdm.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;

@Service
public class DocumentTemplateGenerationService {

    @PostConstruct
    public void generateIndustryDocuments() {
        File folder = new File("src/main/resources/documentation_templates");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try {
            generateFunctionalSpecificationWord();
            generateWalkthroughPowerPoint();
            generateInstructionsExcel();
        } catch (Exception e) {
            System.err.println("Failed to pre-generate document templates: " + e.getMessage());
        }
    }

    private void generateFunctionalSpecificationWord() throws Exception {
        File file = new File("src/main/resources/documentation_templates/functional_specification.docx");
        if (file.exists()) return;

        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(file)) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("Functional Specification Document (FSD)");
            titleRun.setBold(true);
            titleRun.setFontSize(22);
            titleRun.setFontFamily("Calibri");

            XWPFParagraph p1 = document.createParagraph();
            XWPFRun p1Run = p1.createRun();
            p1Run.setText("System Name: Software Development Document Environment (SDM)");
            p1Run.setBold(true);
            p1Run.setFontSize(14);

            XWPFParagraph p2 = document.createParagraph();
            XWPFRun p2Run = p2.createRun();
            p2Run.setText("1. INTRODUCTION\n" +
                    "This functional specification provides details for the SDM system, offering directory polling " +
                    "automation, configurable document status displays, robust maker-checker approvals, and multi-DB support.\n\n" +
                    "2. KEY COMPONENT FLOWS\n" +
                    "- Auto Polling: Scans '/input-documents' folder, parses filename metadata, and parses structures.\n" +
                    "- Security: Configurable DB or LDAP AD strategies.\n" +
                    "- Workflow: Maker submits, checker reviews, accepts, or rejects.\n" +
                    "- Reports: Generates offline and downloadable summary PDF and Excel status tracking templates.\n\n" +
                    "3. JUNIT TESTING CASE METRICS\n" +
                    "- SdmUserAuthTest: Validates logins, AD mock switches, and locking mechanics.\n" +
                    "- ParserServiceTest: Evaluates Excel/Word/PPTX/XML structure mapping correctness.\n" +
                    "- WorkflowEngineTest: Verifies Maker-Checker validation rules and status updates.\n");
            p2Run.setFontSize(11);

            document.write(out);
        }
    }

    private void generateWalkthroughPowerPoint() throws Exception {
        File file = new File("src/main/resources/documentation_templates/workspace_walkthrough.pptx");
        if (file.exists()) return;

        try (XMLSlideShow ppt = new XMLSlideShow();
             FileOutputStream out = new FileOutputStream(file)) {

            // Slide 1: Title
            XSLFSlide slide1 = ppt.createSlide();
            XSLFTextShape title1 = slide1.createTextBox();
            title1.setAnchor(new java.awt.Rectangle(50, 100, 600, 150));
            XSLFTextParagraph p1 = title1.addNewTextParagraph();
            XSLFTextRun r1 = p1.addNewTextRun();
            r1.setText("SDM System Walkthrough");
            r1.setBold(true);
            r1.setFontSize(36.0);

            XSLFTextParagraph p1_sub = title1.addNewTextParagraph();
            XSLFTextRun r1_sub = p1_sub.addNewTextRun();
            r1_sub.setText("An overview of SDLC deliverables dashboard and Maker-Checker structures");
            r1_sub.setFontSize(18.0);

            // Slide 2: Details
            XSLFSlide slide2 = ppt.createSlide();
            XSLFTextShape title2 = slide2.createTextBox();
            title2.setAnchor(new java.awt.Rectangle(50, 50, 600, 400));
            XSLFTextParagraph p2 = title2.addNewTextParagraph();
            XSLFTextRun r2 = p2.addNewTextRun();
            r2.setText("Functional Features Checklist:\n\n" +
                    "1. Automatic polling and document ingestion.\n" +
                    "2. 7-Phase tab and frame display system (P001 to P016).\n" +
                    "3. Dual-mode authentication (DB & LDAP Active Directory).\n" +
                    "4. Audit logging and real-time activity dashboards.\n" +
                    "5. Downloadable approval reports (PDF/Excel) & Swagger API Documentation.");
            r2.setFontSize(14.0);

            ppt.write(out);
        }
    }

    private void generateInstructionsExcel() throws Exception {
        File file = new File("src/main/resources/documentation_templates/setup_instructions.xlsx");
        if (file.exists()) return;

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("User Instructions");

            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("Software Development Document Environment");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("File Drop Naming Standard");
            r1.createCell(1).setCellValue("[APPCODE]_[DELIVERABLEID]_[VERSION]_[DOCCODE].[ext]");

            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Example Drop");
            r2.createCell(1).setCellValue("PRJ_P001_V1.0_BRD-01.xlsx");

            Row r3 = sheet.createRow(4);
            r3.createCell(0).setCellValue("System Default Logins");

            Row r4 = sheet.createRow(5);
            r4.createCell(0).setCellValue("Administrator");
            r4.createCell(1).setCellValue("admin / Admin@123");

            Row r5 = sheet.createRow(6);
            r5.createCell(0).setCellValue("Maker");
            r5.createCell(1).setCellValue("maker / Maker@123");

            Row r6 = sheet.createRow(7);
            r6.createCell(0).setCellValue("Checker");
            r6.createCell(1).setCellValue("checker / Checker@123");

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
        }
    }
}
