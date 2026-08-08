package com.cth.sdm.service;

import com.cth.sdm.model.SdmDocumentVersion;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportingService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateExcelReport(List<SdmDocumentVersion> versions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Deliverables Status Report");

            // Header Style
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "App Code", "Deliverable Name", "File Name", "Version", "Document Code", "Status", "Maker", "Approver Name", "Submitted At"};

            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (SdmDocumentVersion ver : versions) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ver.getDocument().getId());
                row.createCell(1).setCellValue(ver.getApplicationCode().getAppCode());
                row.createCell(2).setCellValue(ver.getDocument().getTitle());
                row.createCell(3).setCellValue(ver.getFileName());
                row.createCell(4).setCellValue(ver.getVersionNumber());
                row.createCell(5).setCellValue(ver.getDocumentCode());
                row.createCell(6).setCellValue(ver.getStatus());
                row.createCell(7).setCellValue(ver.getMaker().getUsername());
                row.createCell(8).setCellValue(ver.getChecker() != null ? ver.getChecker().getUsername() : "N/A");
                row.createCell(9).setCellValue(ver.getSubmittedAt().format(formatter));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generatePdfReport(List<SdmDocumentVersion> versions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            // Document title
            Paragraph title = new Paragraph("Software Development Document Environment", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph subtitle = new Paragraph("Deliverables and Workflow Approval Report", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Table setup
            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2f, 4f, 4f, 1.5f, 2.5f, 3f, 2.5f, 3f, 3.5f});

            // Table headers
            String[] headers = {"ID", "App Code", "Deliverable Name", "File Name", "Version", "Doc Code", "Status", "Maker", "Approver", "Submitted At"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Table records
            for (SdmDocumentVersion ver : versions) {
                table.addCell(new Phrase(ver.getDocument().getId(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getApplicationCode().getAppCode(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getDocument().getTitle(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getFileName(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getVersionNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getDocumentCode(), FontFactory.getFont(FontFactory.HELVETICA, 8)));

                PdfPCell statusCell = new PdfPCell(new Phrase(ver.getStatus(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                if ("APPROVED".equalsIgnoreCase(ver.getStatus())) {
                    statusCell.setBackgroundColor(new java.awt.Color(200, 240, 200));
                } else if ("REJECTED".equalsIgnoreCase(ver.getStatus())) {
                    statusCell.setBackgroundColor(new java.awt.Color(240, 200, 200));
                } else {
                    statusCell.setBackgroundColor(new java.awt.Color(250, 240, 200));
                }
                table.addCell(statusCell);

                table.addCell(new Phrase(ver.getMaker().getUsername(), FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getChecker() != null ? ver.getChecker().getUsername() : "N/A", FontFactory.getFont(FontFactory.HELVETICA, 8)));
                table.addCell(new Phrase(ver.getSubmittedAt().format(formatter), FontFactory.getFont(FontFactory.HELVETICA, 8)));
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            System.err.println("Failed to write PDF Report: " + e.getMessage());
            return new byte[0];
        }
    }
}
