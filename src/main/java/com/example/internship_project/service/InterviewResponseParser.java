package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewResponseParser {

    private final ObjectMapper objectMapper;

    public InterviewResponse parse(String rawResponse) {
        String cleanJson = cleanJsonResponse(rawResponse);
        try {
            return objectMapper.readValue(cleanJson, InterviewResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Получен невалидный JSON от Gemini API", e);
        }
    }

    String cleanJsonResponse(String rawResponse) {
        return rawResponse
                .replaceAll("(?s)^```(?:json)?\\s*", "")
                .replaceAll("```$", "")
                .trim();
    }
}
