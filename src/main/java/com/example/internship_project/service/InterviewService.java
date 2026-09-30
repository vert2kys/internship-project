package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import com.example.internship_project.entity.InterviewLog;
import com.example.internship_project.repository.InterviewLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final GeminiClient geminiClient;
    private final InterviewPromptBuilder promptBuilder;
    private final InterviewResponseParser responseParser;
    private final InterviewFallbackResponseFactory fallbackResponseFactory;
    private final InterviewLogService interviewLogService;

    public InterviewResponse generateQuestions(InterviewRequest request) {
        log.info("Запрос на генерацию вопросов для вакансии: {}", request.getJobDescription());

        String prompt = promptBuilder.buildInterviewPrompt(request.getJobDescription());

        try {
            String rawResponse = geminiClient.askGemini(prompt);

            if (rawResponse == null || rawResponse.isBlank()) {
                log.warn("Получен пустой ответ от Gemini API");
                throw new IllegalArgumentException("Получен пустой ответ от Gemini API");
            }

            InterviewResponse response = responseParser.parse(rawResponse);
            log.info("Успешно сгенерировано {} вопросов", response.getQuestions().size());

            interviewLogService.save(request.getJobDescription(), response);
            return response;

        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от Gemini API: {}. Применение fallback-ответа", e.getMessage());
            InterviewResponse fallbackResponse = fallbackResponseFactory.create(request.getJobDescription());
            interviewLogService.save(request.getJobDescription(), fallbackResponse);
            return fallbackResponse;
        }
    }
}