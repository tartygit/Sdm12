package com.cth.sdm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DocumentParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> deserializeSections(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (Exception e) {
            System.err.println("Failed to deserialize parsed document sections JSON: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
