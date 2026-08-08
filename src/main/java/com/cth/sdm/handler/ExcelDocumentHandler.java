package com.cth.sdm.handler;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExcelDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "xlsx".equalsIgnoreCase(fileExtension) || "xls".equalsIgnoreCase(fileExtension);
    }

    @Override
    public Map<String, String> handle(File file) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int sheetCount = workbook.getNumberOfSheets();
            if (sheetCount == 0) {
                result.put("Sheet 1", "Empty Excel Workbook");
                return result;
            }

            for (int i = 0; i < sheetCount; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                StringBuilder contentBuilder = new StringBuilder();
                int rowCount = 0;
                for (Row row : sheet) {
                    if (rowCount++ > 200) {
                        contentBuilder.append("[Truncated for size limitation]\n");
                        break;
                    }
                    StringBuilder rowBuilder = new StringBuilder();
                    for (Cell cell : row) {
                        String cellVal = "";
                        switch (cell.getCellType()) {
                            case STRING -> cellVal = cell.getStringCellValue();
                            case NUMERIC -> {
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    cellVal = cell.getDateCellValue().toString();
                                } else {
                                    cellVal = String.valueOf(cell.getNumericCellValue());
                                }
                            }
                            case BOOLEAN -> cellVal = String.valueOf(cell.getBooleanCellValue());
                            case FORMULA -> cellVal = cell.getCellFormula();
                            default -> cellVal = "";
                        }
                        if (!cellVal.trim().isEmpty()) {
                            rowBuilder.append(cellVal).append(" | ");
                        }
                    }
                    if (rowBuilder.length() > 0) {
                        contentBuilder.append(rowBuilder).append("\n");
                    }
                }
                String content = contentBuilder.toString().trim();
                result.put(sheet.getSheetName(), content.isEmpty() ? "Empty Sheet Content" : content);
            }
        }
        return result;
    }
}
