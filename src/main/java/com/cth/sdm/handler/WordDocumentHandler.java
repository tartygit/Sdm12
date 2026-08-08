package com.cth.sdm.handler;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WordDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "docx".equalsIgnoreCase(fileExtension) || "doc".equalsIgnoreCase(fileExtension);
    }

    @Override
    public Map<String, String> handle(File file) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder contentBuilder = new StringBuilder();
            int paraCount = 0;
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                if (paraCount++ > 100) {
                    contentBuilder.append("\n[Truncated for length limitations]\n");
                    break;
                }
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    contentBuilder.append(text).append("\n\n");
                }
            }

            String fullContent = contentBuilder.toString().trim();
            if (fullContent.isEmpty()) {
                fullContent = "Empty Document Content";
            }
            result.put("Document Text Content", fullContent);
        }
        return result;
    }
}
