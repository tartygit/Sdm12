package com.cth.sdm.handler;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PowerPointDocumentHandler implements DocumentHandler {

    @Override
    public boolean supports(String fileExtension) {
        return "pptx".equalsIgnoreCase(fileExtension) || "ppt".equalsIgnoreCase(fileExtension);
    }

    @Override
    public Map<String, String> handle(File file) throws Exception {
        Map<String, String> result = new LinkedHashMap<>();

        try (FileInputStream fis = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            int slideNum = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                StringBuilder slideText = new StringBuilder();
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String txt = textShape.getText().trim();
                        if (!txt.isEmpty()) {
                            slideText.append(txt).append("\n");
                        }
                    }
                }
                String content = slideText.toString().trim();
                result.put("Slide " + slideNum++, content.isEmpty() ? "Empty Slide Content" : content);
            }
        }
        return result;
    }
}
