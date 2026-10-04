package com.example.internship_project.service;

import com.example.internship_project.dto.InterviewRequest;
import com.example.internship_project.dto.InterviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        String jobDescription = normalizeJobDescription(request);
        log.info("Запрос на генерацию вопросов для вакансии: {}", jobDescription);

        Optional<InterviewResponse> savedResponse = interviewLogService.findByRequest(jobDescription);
        if (savedResponse.isPresent()) {
            log.info("Найден сохраненный ответ в базе данных для jobDescription: {}", jobDescription);
            return savedResponse.get();
        }

        String prompt = promptBuilder.buildInterviewPrompt(jobDescription);

        try {
            String rawResponse = geminiClient.askGemini(prompt);

            if (rawResponse == null || rawResponse.isBlank()) {
                log.warn("Получен пустой ответ от Gemini API");
                throw new IllegalArgumentException("Получен пустой ответ от Gemini API");
            }

            InterviewResponse response = responseParser.parse(rawResponse);
            log.info("Успешно сгенерировано {} вопросов", response.getQuestions().size());

            interviewLogService.save(jobDescription, response);
            return response;

        } catch (Exception e) {
            log.error("Ошибка при обработке ответа от Gemini API: {}. Применение fallback-ответа", e.getMessage());
            InterviewResponse fallbackResponse = fallbackResponseFactory.create(jobDescription);
            interviewLogService.save(jobDescription, fallbackResponse);
            return fallbackResponse;
        }
    }

    private String normalizeJobDescription(InterviewRequest request) {
        if (request == null || request.getJobDescription() == null) {
            return "";
        }
        return request.getJobDescription().trim();
    }
}